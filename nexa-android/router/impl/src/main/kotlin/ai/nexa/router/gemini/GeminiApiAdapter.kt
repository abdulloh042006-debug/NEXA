package ai.nexa.router.gemini

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.ai.port.ModelInvocationException
import ai.nexa.core.network.inference.GeminiGatewayClient
import ai.nexa.core.network.inference.GeminiGatewayEvent
import ai.nexa.core.network.inference.GeminiGatewayRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Gemini model adapter routed through the NEXA Inference Service.
 * Vendor credentials never enter the Android process (ARCHITECTURE section 9.2).
 */
class GeminiApiAdapter(
    override val manifest: ModelManifest,
    private val gatewayClient: GeminiGatewayClient,
) : ChatModelPort {
    init {
        require(manifest.kind == ModelManifest.ModelKind.CLOUD) { "Gemini adapter requires a cloud manifest" }
        require(ModelManifest.ModelCapability.CHAT in manifest.capabilities) {
            "Gemini adapter requires chat capability"
        }
    }

    override fun streamChat(request: ChatRequest): Flow<ChatDelta> {
        require(request.toolSchemas.isEmpty()) { "Phase 1 Gemini adapter does not support tool calls" }
        require(manifest.mayReceive(request.privacyClass)) { "Request exceeds Gemini manifest privacy clearance" }
        return gatewayClient.stream(request.toGatewayRequest(), request.latencyBudget.budgetMs)
            .map { event ->
                when (event) {
                    is GeminiGatewayEvent.Token -> ChatDelta.Token(event.text)
                    is GeminiGatewayEvent.Usage -> ChatDelta.Usage(event.inputTokens, event.outputTokens)
                }
            }
            .catch { failure ->
                if (failure is kotlinx.coroutines.CancellationException) throw failure
                throw ModelInvocationException.ProviderFailure(failure)
            }
    }

    private fun ChatRequest.toGatewayRequest() = GeminiGatewayRequest(
        messages = messages.map {
            GeminiGatewayRequest.Message(
                role = it.role.toGatewayRole(),
                content = it.content,
            )
        },
        temperature = sampling.temperature,
        topP = sampling.topP,
        maxOutputTokens = sampling.maxOutputTokens,
        stopSequences = sampling.stopSequences,
        language = languageHint?.name?.lowercase(),
        privacyClass = privacyClass.name,
    )

    private fun ChatMessage.Role.toGatewayRole(): String = when (this) {
        ChatMessage.Role.SYSTEM -> "system"
        ChatMessage.Role.USER -> "user"
        ChatMessage.Role.ASSISTANT -> "assistant"
        ChatMessage.Role.TOOL -> "tool"
    }
}
