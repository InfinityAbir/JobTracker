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
}
