package com.example.data.remote

import com.example.BuildConfig
import com.example.domain.model.JobApplication
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

open class GeminiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    // Primary model is gemini-3.5-flash with fallback candidate models
    private val candidateModels = listOf(
        "gemini-3.8-flash",
        "gemini-3.6-flash",
        "gemini-flash-latest",
        "gemini-3.5-flash-lite"
    )

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.ifBlank {
                System.getenv("GEMINI_API_KEY") ?: ""
            }
        } catch (e: Throwable) {
            System.getenv("GEMINI_API_KEY") ?: ""
        }
    }

    private suspend fun callGemini(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Add GEMINI_API_KEY in Secrets.")
            )
        }

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
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
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
                    lastError = Exception("Empty response from model $model")
                    continue
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBody).optJSONObject("error")
                        errObj?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody
                    }
                    lastError = Exception("Gemini API error (${response.code}): $errorMsg")

                    // On rate limit (429), high demand / server error (500, 502, 503, 504), or not found (404),
                    // automatically failover to the next candidate model
                    if (response.code in listOf(404, 429, 500, 502, 503, 504)) {
                        kotlinx.coroutines.delay(350)
                        continue
                    } else {
                        return@withContext Result.failure(lastError)
                    }
                }
            } catch (e: Exception) {
                lastError = e
                kotlinx.coroutines.delay(200)
            }
        }

        Result.failure(lastError ?: Exception("Failed to execute request with Gemini API"))
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

        val result = callGemini(prompt)
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

        return callGemini(prompt)
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

        return callGemini(prompt)
    }
}
