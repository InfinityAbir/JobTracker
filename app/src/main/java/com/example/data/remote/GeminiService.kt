package com.example.data.remote

import com.example.domain.model.JobApplication
import com.example.localization.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedJobData(
    val companyName: String = "",
    val jobTitle: String = "",
    val salary: String = "",
    val keyPoints: String = "",
    val detectedStatus: String = "Applied"
)

/** The shared (proxy) free quota is exhausted or this device is rate limited. */
class SharedLimitException(message: String) : Exception(message)

/**
 * Talks to Gemini. If the user saved their own key it calls Google directly; otherwise, when a
 * proxy URL is configured, it uses the shared free proxy (which holds the developer's key).
 */
open class GeminiService(
    private val apiKeyProvider: () -> String = { "" },
    private val proxyUrl: String = "",
    private val deviceIdProvider: () -> String = { "" },
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    // Stable "-latest" aliases first so the app keeps working when Google retires a version;
    // pinned models are the fallback. 404/429/5xx automatically moves to the next candidate.
    private val candidateModels = listOf(
        "gemini-flash-latest",
        "gemini-flash-lite-latest",
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite"
    )

    private companion object {
        const val TAG = "GeminiService"
    }

    private suspend fun dispatch(tool: String, fields: Map<String, String>, prompt: String): Result<String> {
        val apiKey = apiKeyProvider().trim()
        return when {
            apiKey.isNotBlank() -> callGemini(prompt, apiKey)
            proxyUrl.isNotBlank() -> callProxy(tool, fields)
            else -> Result.failure(IllegalStateException(AppStrings.current().aiErrNoKey))
        }
    }

    private suspend fun callProxy(tool: String, fields: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        val strings = AppStrings.current()
        try {
            val payload = JSONObject().put("tool", tool).put("fields", JSONObject(fields))
            val request = Request.Builder()
                .url(proxyUrl.trimEnd('/') + "/v1/ai")
                .header("x-device-id", deviceIdProvider())
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val (code, isSuccessful, body) = client.newCall(request).execute().use { response ->
                Triple(response.code, response.isSuccessful, response.body.string())
            }

            when {
                isSuccessful -> {
                    val text = JSONObject(body).optString("text", "")
                    if (text.isNotBlank()) Result.success(text) else Result.failure(Exception(strings.aiErrEmpty))
                }
                code == 429 -> Result.failure(SharedLimitException(strings.aiErrSharedLimit))
                code in 502..504 -> Result.failure(Exception(strings.aiErrBusy))
                else -> {
                    val reason = runCatching { JSONObject(body).getJSONObject("error").optString("code") }.getOrDefault("")
                    Result.failure(Exception(strings.aiErrGeneric(code, reason)))
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: java.io.IOException) {
            android.util.Log.w(TAG, "Proxy request failed: $e")
            Result.failure(Exception(strings.aiErrNetwork))
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Proxy request error: $e")
            Result.failure(Exception(strings.aiErrGeneric(0, e.message.orEmpty())))
        }
    }

    private suspend fun callGemini(prompt: String, apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val jsonPayload = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }
        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
                val request = Request.Builder()
                    .url(url)
                    .header("x-goog-api-key", apiKey)
                    .post(requestBody)
                    .build()

                val (responseCode, isSuccessful, responseBody) = client.newCall(request).execute().use { response ->
                    Triple(response.code, response.isSuccessful, response.body.string())
                }

                if (isSuccessful) {
                    val rootJson = JSONObject(responseBody)
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text", "")
                            if (text.isNotBlank()) {
                                return@withContext Result.success(text)
                            }
                        }
                    }
                    // Empty content response from this model, try fallback
                    lastError = Exception(AppStrings.current().aiErrEmpty)
                    continue
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBody).optJSONObject("error")
                        errObj?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody
                    }
                    lastError = Exception(friendlyError(responseCode, errorMsg))

                    // On rate limit (429), high demand / server error (500, 502, 503, 504), or not found (404),
                    // automatically failover to the next candidate model
                    if (responseCode in listOf(404, 429, 500, 502, 503, 504)) {
                        kotlinx.coroutines.delay(350)
                        continue
                    } else {
                        return@withContext Result.failure(lastError)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Gemini request failed: $e")
                lastError = if (e is java.io.IOException) Exception(AppStrings.current().aiErrNetwork) else e
                kotlinx.coroutines.delay(200)
            }
        }

        Result.failure(lastError ?: Exception(AppStrings.current().aiErrNetwork))
    }

    private fun friendlyError(code: Int, apiMessage: String): String {
        val strings = AppStrings.current()
        return when (code) {
            400, 401, 403 -> strings.aiErrKeyRejected(code)
            429 -> strings.aiErrRateLimit
            else -> strings.aiErrGeneric(code, apiMessage)
        }
    }

    // Feature 3: Job Description Auto-Parser
    open suspend fun parseJobPosting(rawText: String): Result<ParsedJobData> {
        val prompt = """
You are an expert ATS (Applicant Tracking System) parser. Extract structured job application details from the following job posting text.
Output MUST BE ONLY a raw valid JSON object without markdown fences or extra explanations.
JSON schema:
{
  "companyName": "Exact Company Name",
  "jobTitle": "Job Title / Role",
  "salary": "Compensation or Salary Range if specified (otherwise empty string)",
  "keyPoints": "Concise 2-3 bullet summary of top qualifications or highlights",
  "detectedStatus": "Applied"
}

Job Posting Text:
$rawText
""".trimIndent()

        val result = dispatch("parse", mapOf("rawText" to rawText), prompt)
        return result.mapCatching { responseText ->
            val cleanText = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val startIndex = cleanText.indexOf('{')
            val endIndex = cleanText.lastIndexOf('}')

            var companyName = ""
            var jobTitle = ""
            var salary = ""
            var keyPoints = ""
            var detectedStatus = "Applied"

            if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
                try {
                    val json = JSONObject(cleanText.substring(startIndex, endIndex + 1))
                    companyName = json.optString("companyName", "").trim()
                    jobTitle = json.optString("jobTitle", "").trim()
                    salary = json.optString("salary", "").trim()
                    keyPoints = json.optString("keyPoints", "").trim()
                    detectedStatus = json.optString("detectedStatus", "Applied").trim()
                } catch (ignored: Exception) {}
            }

            // Fallback regex if JSON parsing missed any key fields
            if (companyName.isBlank()) {
                val match = Regex("""(?:Company|companyName)["':\s]+([^\n,}]+)""").find(cleanText)
                companyName = match?.groupValues?.get(1)?.replace("\"", "")?.trim() ?: ""
            }
            if (jobTitle.isBlank()) {
                val match = Regex("""(?:Job Title|Role|jobTitle)["':\s]+([^\n,}]+)""").find(cleanText)
                jobTitle = match?.groupValues?.get(1)?.replace("\"", "")?.trim() ?: ""
            }
            if (salary.isBlank()) {
                val match = Regex("""(?:Salary|salary)["':\s]+([^\n,}]+)""").find(cleanText)
                salary = match?.groupValues?.get(1)?.replace("\"", "")?.trim() ?: ""
            }
            if (keyPoints.isBlank()) {
                val match = Regex("""(?:Key Points|keyPoints|Highlights)["':\s]+([^\n}]+)""").find(cleanText)
                keyPoints = match?.groupValues?.get(1)?.replace("\"", "")?.trim() ?: ""
            }

            ParsedJobData(
                companyName = companyName,
                jobTitle = jobTitle,
                salary = salary,
                keyPoints = keyPoints,
                detectedStatus = detectedStatus
            )
        }
    }

    // Feature 2: Tailored Cover Letter & Talking Points Generator
    open suspend fun generateCoverLetter(
        companyName: String,
        jobTitle: String,
        notesOrDescription: String,
        userCandidateHighlights: String = ""
    ): Result<String> {
        val prompt = """
Write a compelling, professional, modern, and concise tailored cover letter and key interview talking points for the following job:
- Company: $companyName
- Role / Job Title: $jobTitle
- Context / Job Details: $notesOrDescription
- Candidate Background Highlights: ${userCandidateHighlights.ifBlank { "Experienced software engineer passionate about modern product development, clean architecture, and high performance systems." }}

Format output cleanly with:
1. Tailored Cover Letter (3 short impactful paragraphs)
2. 3 Key Talking Points for Why I'm a Great Match
""".trimIndent()

        return dispatch(
            "cover",
            mapOf(
                "companyName" to companyName,
                "jobTitle" to jobTitle,
                "notes" to notesOrDescription,
                "candidateHighlights" to userCandidateHighlights
            ),
            prompt
        )
    }

    // Feature 4: Follow-up & Thank You Email Drafter
    open suspend fun generateFollowUpEmail(
        application: JobApplication,
        emailType: String = "Post-Interview Thank You"
    ): Result<String> {
        val prompt = """
Draft a polished, professional email for the following job application stage:
- Email Scenario / Intent: $emailType
- Company: ${application.companyName}
- Role: ${application.jobTitle}
- Current Application Status: ${application.status.displayName}
- Application Notes / Context: ${application.notes}

Provide:
- Subject Line
- Professional Email Body (polite, confident, expressing clear value and gratitude)
""".trimIndent()

        return dispatch(
            "followup",
            mapOf(
                "companyName" to application.companyName,
                "jobTitle" to application.jobTitle,
                "status" to application.status.displayName,
                "notes" to application.notes,
                "emailType" to emailType
            ),
            prompt
        )
    }
}
