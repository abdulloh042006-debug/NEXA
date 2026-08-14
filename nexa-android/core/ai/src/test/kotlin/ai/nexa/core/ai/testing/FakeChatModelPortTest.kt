package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeChatModelPortTest {

    private val request = ChatRequest(
        messages = listOf(ChatMessage(ChatMessage.Role.USER, "salom dunyo")),
        privacyClass = PrivacyClass.P1_PERSONAL,
        latencyBudget = LatencyBudget.INTERACTIVE,
    )

    @Test
    fun `streamChat emits scripted deltas in order and ends with usage`() = runTest {
        val port = FakeChatModelPort()

        val deltas = port.streamChat(request).toList()

        assertEquals(
            listOf<ChatDelta>(
                ChatDelta.Token("salom "),
                ChatDelta.Token("dunyo "),
                ChatDelta.Usage(inputTokens = 2, outputTokens = 2),
            ),
            deltas,
        )
    }

    @Test
    fun `flow is cold - nothing is recorded until collection and each collection replays`() = runTest {
        val port = FakeChatModelPort()

        val stream = port.streamChat(request)
        assertTrue(port.recordedRequests.isEmpty())

        stream.toList()
        stream.toList()
        assertEquals(listOf(request, request), port.recordedRequests)
    }

    @Test
    fun `early collector cancellation stops the stream mid-script`() = runTest {
        val port = FakeChatModelPort()

        val first = port.streamChat(request).take(1).toList()

        assertEquals(listOf<ChatDelta>(ChatDelta.Token("salom ")), first)
    }

    @Test
    fun `scripted failure propagates to the collector after the deltas`() = runTest {
        val port = FakeChatModelPort(
            script = { listOf(ChatDelta.Token("partial ")) },
            failure = IllegalStateException("provider timeout"),
        )

        val collected = mutableListOf<ChatDelta>()
        var thrown: IllegalStateException? = null
        try {
            port.streamChat(request).collect { collected += it }
        } catch (expected: IllegalStateException) {
            thrown = expected
        }

        assertEquals(listOf<ChatDelta>(ChatDelta.Token("partial ")), collected)
        assertEquals("provider timeout", thrown?.message)
    }

    @Test
    fun `function calling capability is derived from the manifest`() {
        assertTrue(FakeChatModelPort().supportsFunctionCalling)
    }
}
