package ai.nexa.core.network.inference

/** Wire-neutral DTOs for the NEXA Inference Service's Gemini adapter endpoint. */
data class GeminiGatewayRequest(
    val messages: List<Message>,
    val temperature: Double?,
    val topP: Double?,
    val maxOutputTokens: Int?,
    val stopSequences: List<String>,
    val language: String?,
    val privacyClass: String,
) {
    data class Message(
        val role: String,
        val content: String,
    )
}

sealed interface GeminiGatewayEvent {
    data class Token(val text: String) : GeminiGatewayEvent

    data class Usage(val inputTokens: Int, val outputTokens: Int) : GeminiGatewayEvent
}

interface GeminiGatewayClient {
    fun stream(request: GeminiGatewayRequest, timeoutMillis: Long): kotlinx.coroutines.flow.Flow<GeminiGatewayEvent>
}

sealed class InferenceGatewayException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Unauthorized : InferenceGatewayException("NEXA Inference Service rejected authentication")

    class RateLimited : InferenceGatewayException("NEXA Inference Service rate limit reached")

    class HttpFailure(val statusCode: Int) : InferenceGatewayException("Inference request failed with HTTP $statusCode")

    class ProtocolFailure(cause: Throwable) : InferenceGatewayException("Invalid inference stream response", cause)
}
