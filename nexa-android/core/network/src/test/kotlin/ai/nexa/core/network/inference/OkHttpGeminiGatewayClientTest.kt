package ai.nexa.core.network.inference

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OkHttpGeminiGatewayClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun constructsRequestAndMapsNdjsonStream() = runTest {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/x-ndjson").setBody(
                """{"type":"token","text":"Salom"}
                    |{"type":"usage","input_tokens":3,"output_tokens":1}
                    |
                """.trimMargin(),
            ),
        )
        val client = client(token = "session-token")

        val events = client.stream(request(), timeoutMillis = 10_000).toList()

        assertEquals(
            listOf(GeminiGatewayEvent.Token("Salom"), GeminiGatewayEvent.Usage(3, 1)),
            events,
        )
        val recorded = server.takeRequest()
        assertEquals("/v1/inference/gemini:stream", recorded.path)
        assertEquals("Bearer session-token", recorded.getHeader("Authorization"))
        assertEquals("no-store", recorded.getHeader("Cache-Control"))
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"privacy_class\":\"P1_PERSONAL\""))
        assertTrue(body.contains("\"content\":\"Salom\""))
        assertFalse(body.contains("session-token"))
    }

    @Test
    fun mapsAuthenticationAndRateLimitFailures() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        assertFailsWith<InferenceGatewayException.Unauthorized> {
            client(null).stream(request(), 10_000).toList()
        }
        server.enqueue(MockResponse().setResponseCode(429))
        assertFailsWith<InferenceGatewayException.RateLimited> {
            client(null).stream(request(), 10_000).toList()
        }
    }

    @Test
    fun rejectsMalformedStreamWithoutReturningPartialSuccess() = runTest {
        server.enqueue(MockResponse().setBody("{not-json}\n"))

        assertFailsWith<InferenceGatewayException.ProtocolFailure> {
            client(null).stream(request(), 10_000).toList()
        }
    }

    private fun client(token: String?) = OkHttpGeminiGatewayClient(
        baseUrl = server.url("/").toString(),
        sessionTokenProvider = { token },
        client = OkHttpClient(),
    )

    private fun request() = GeminiGatewayRequest(
        messages = listOf(GeminiGatewayRequest.Message("user", "Salom")),
        temperature = 0.2,
        topP = null,
        maxOutputTokens = 32,
        stopSequences = emptyList(),
        language = "uz",
        privacyClass = "P1_PERSONAL",
    )
}
