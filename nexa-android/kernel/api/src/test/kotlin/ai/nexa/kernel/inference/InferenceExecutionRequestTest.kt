package ai.nexa.kernel.inference

import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RoutingDeviceState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InferenceExecutionRequestTest {
    @Test
    fun `valid request defaults to declared latency budget`() {
        val request = request()

        assertEquals(LatencyBudget.INTERACTIVE.budgetMs, request.timeoutMillis)
    }

    @Test
    fun `identifiers and timeout reject invalid values`() {
        assertFailsWith<IllegalArgumentException> { InferenceExecutionId("private text!") }
        assertFailsWith<IllegalArgumentException> { request(timeoutMillis = 0) }
        assertFailsWith<IllegalArgumentException> { request(timeoutMillis = 300_001) }
    }

    private fun request(timeoutMillis: Long = LatencyBudget.INTERACTIVE.budgetMs) =
        InferenceExecutionRequest(
            executionId = InferenceExecutionId("execution-1"),
            correlationId = InferenceCorrelationId("conversation-1"),
            routeRequest = ChatRouteRequest(
                request = ChatRequest(
                    messages = listOf(ChatMessage(ChatMessage.Role.USER, "hello")),
                    privacyClass = PrivacyClass.P2_SENSITIVE,
                    latencyBudget = LatencyBudget.INTERACTIVE,
                ),
                estimatedInputTokens = 2,
                deviceState = RoutingDeviceState(NetworkState.UNAVAILABLE, 0, emptySet()),
            ),
            timeoutMillis = timeoutMillis,
        )
}
