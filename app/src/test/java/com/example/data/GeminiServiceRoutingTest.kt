package com.example.data

import com.example.data.remote.GeminiService
import com.example.data.remote.SharedLimitException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiServiceRoutingTest {

    private val seen = mutableListOf<Request>()

    private fun client(code: Int, body: String) = OkHttpClient.Builder()
        .addInterceptor { chain ->
            seen += chain.request()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("test")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
        .build()

    private val geminiOk = """{"candidates":[{"content":{"parts":[{"text":"direct answer"}]}}]}"""
    private val proxyOk = """{"text":"proxy answer"}"""

    @Test
    fun ownKey_callsGoogleDirectly_withKeyInHeader_notInUrl() = runBlocking {
        val service = GeminiService(
            apiKeyProvider = { "user-key" },
            proxyUrl = "https://proxy.test",
            deviceIdProvider = { "device-1" },
            client = client(200, geminiOk)
        )
        val result = service.generateCoverLetter("Acme", "Dev", "notes")

        assertEquals("direct answer", result.getOrNull())
        assertEquals("generativelanguage.googleapis.com", seen.single().url.host)
        assertEquals("user-key", seen.single().header("x-goog-api-key"))
        assertFalse(seen.single().url.toString().contains("user-key"))
    }

    @Test
    fun noKey_withProxy_usesProxy_withStructuredFieldsAndDeviceId() = runBlocking {
        val service = GeminiService(
            apiKeyProvider = { "" },
            proxyUrl = "https://proxy.test/",
            deviceIdProvider = { "device-1" },
            client = client(200, proxyOk)
        )
        val result = service.generateCoverLetter("Acme", "Dev", "some notes")

        assertEquals("proxy answer", result.getOrNull())
        val request = seen.single()
        assertEquals("https://proxy.test/v1/ai", request.url.toString())
        assertEquals("device-1", request.header("x-device-id"))
        val buffer = okio.Buffer().also { request.body!!.writeTo(it) }
        val sent = JSONObject(buffer.readUtf8())
        assertEquals("cover", sent.getString("tool"))
        assertEquals("Acme", sent.getJSONObject("fields").getString("companyName"))
        assertFalse(sent.has("prompt"))
    }

    @Test
    fun proxyRateLimit_isSharedLimitException() = runBlocking {
        val service = GeminiService(
            proxyUrl = "https://proxy.test",
            deviceIdProvider = { "device-1" },
            client = client(429, """{"error":{"code":"rate_limited"}}""")
        )
        val result = service.parseJobPosting("Android dev at Acme")

        assertTrue(result.exceptionOrNull() is SharedLimitException)
    }

    @Test
    fun noKey_noProxy_failsWithoutNetworkCall() = runBlocking {
        val service = GeminiService(client = client(200, geminiOk))
        val result = service.generateCoverLetter("Acme", "Dev", "notes")

        assertTrue(result.isFailure)
        assertTrue(seen.isEmpty())
    }

    // ---- Users must never see status codes, error codes or raw API text ----

    private fun assertFriendly(message: String?) {
        assertTrue("message was: $message", !message.isNullOrBlank())
        val text = message!!
        for (raw in listOf("bad_tool", "bad_fields", "bad_json", "upstream", "not_configured", "Gemini API error", "(400)", "(500)", "(0)", "{", "}")) {
            assertFalse("'$raw' leaked into: $text", text.contains(raw))
        }
    }

    @Test
    fun proxyUnknownTool_showsPlainMessage() = runBlocking {
        val service = GeminiService(
            proxyUrl = "https://proxy.test",
            deviceIdProvider = { "device-1" },
            client = client(400, """{"error":{"code":"bad_tool"}}""")
        )
        val result = service.analyzeMatch("job", "resume")

        assertFriendly(result.exceptionOrNull()?.message)
        assertEquals(com.example.localization.AppStrings.current().aiErrToolUnavailable, result.exceptionOrNull()?.message)
    }

    @Test
    fun proxyOtherErrors_showPlainMessage() = runBlocking {
        for ((code, reason) in listOf(400 to "bad_fields", 400 to "bad_json", 500 to "not_configured", 503 to "not_configured", 418 to "weird")) {
            val service = GeminiService(
                proxyUrl = "https://proxy.test",
                deviceIdProvider = { "device-1" },
                client = client(code, """{"error":{"code":"$reason"}}""")
            )
            val result = service.parseJobPosting("Android dev at Acme")
            assertFriendly(result.exceptionOrNull()?.message)
        }
    }

    @Test
    fun proxyGarbageBody_showsPlainMessage() = runBlocking {
        val service = GeminiService(
            proxyUrl = "https://proxy.test",
            deviceIdProvider = { "device-1" },
            client = client(200, "<html>not json</html>")
        )
        val result = service.generateCoverLetter("Acme", "Dev", "notes")
        assertFriendly(result.exceptionOrNull()?.message)
    }

    @Test
    fun ownKeyRejected_showsPlainMessageWithoutCodeOrApiText() = runBlocking {
        val service = GeminiService(
            apiKeyProvider = { "bad-key" },
            client = client(400, """{"error":{"code":400,"message":"API key not valid. Please pass a valid API key.","status":"INVALID_ARGUMENT"}}""")
        )
        val result = service.generateCoverLetter("Acme", "Dev", "notes")

        assertFriendly(result.exceptionOrNull()?.message)
        assertFalse(result.exceptionOrNull()!!.message!!.contains("INVALID_ARGUMENT"))
        assertEquals(com.example.localization.AppStrings.current().aiErrKeyRejected, result.exceptionOrNull()?.message)
    }

    @Test
    fun ownKeyServerError_showsPlainMessage() = runBlocking {
        val service = GeminiService(
            apiKeyProvider = { "key" },
            client = client(418, """{"error":{"message":"I am a teapot"}}""")
        )
        val result = service.generateCoverLetter("Acme", "Dev", "notes")
        assertFriendly(result.exceptionOrNull()?.message)
        assertFalse(result.exceptionOrNull()!!.message!!.contains("teapot"))
    }
}
