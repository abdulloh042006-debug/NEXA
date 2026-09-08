package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.data.conversation.ConversationStore
import ai.nexa.core.data.conversation.StoredMessage
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.RoutingEnvironmentPort
import ai.nexa.kernel.inference.InferenceCorrelationId
import ai.nexa.kernel.inference.InferenceEvent
import ai.nexa.kernel.inference.InferenceExecutionId
import ai.nexa.kernel.inference.InferenceExecutionRequest
import ai.nexa.kernel.inference.InferenceFailure
import ai.nexa.kernel.inference.InferenceOrchestratorPort
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultChatSessionPort @Inject constructor(
    private val conversationStore: ConversationStore,
    private val orchestrator: InferenceOrchestratorPort,
    private val routingEnvironment: RoutingEnvironmentPort,
) : ChatSessionPort {
    override suspend fun createConversation(): String {
        return conversationStore.createConversation(System.currentTimeMillis())
    }

    override fun observeMessages(conversationId: String): Flow<List<ChatTurn>> =
        conversationStore.observeMessages(conversationId).map { messages -> messages.map { it.toChatTurn() } }

    override fun sendMessage(conversationId: String, content: String): Flow<ChatSendEvent> = flow {
        val normalized = content.trim()
        require(normalized.isNotEmpty()) { "Message cannot be blank" }
        val now = System.currentTimeMillis()
        conversationStore.appendMessage(conversationId, StoredMessage.Role.USER, normalized, now)
        emit(ChatSendEvent.UserStored)

        val history = conversationStore.listMessages(conversationId)
        val reply = StringBuilder()
        val request = ChatRequest(
            messages = history.map { it.toModelMessage() },
            privacyClass = PrivacyClass.P2_SENSITIVE,
            latencyBudget = LatencyBudget.INTERACTIVE,
        )
        val execution = orchestrator.start(
            InferenceExecutionRequest(
                executionId = InferenceExecutionId(UUID.randomUUID().toString()),
                correlationId = InferenceCorrelationId(
                    UUID.nameUUIDFromBytes(conversationId.toByteArray(StandardCharsets.UTF_8)).toString(),
                ),
                routeRequest = ChatRouteRequest(
                    request = request,
                    estimatedInputTokens = request.estimateInputTokens(),
                    deviceState = routingEnvironment.currentDeviceState(),
                ),
            ),
        )
        execution.events.collect { event ->
            when (event) {
                is InferenceEvent.Delta -> when (val delta = event.value) {
                    is ChatDelta.Token -> {
                        reply.append(delta.text)
                        emit(ChatSendEvent.ReplyToken(delta.text))
                    }
                    is ChatDelta.Usage -> Unit
                    is ChatDelta.ToolCall -> emit(ChatSendEvent.Failed(ChatFailure.INVALID_RESPONSE))
                }
                is InferenceEvent.Failed -> emit(ChatSendEvent.Failed(event.failure.toChatFailure()))
                is InferenceEvent.TimedOut -> emit(ChatSendEvent.Failed(ChatFailure.TIMEOUT))
                is InferenceEvent.Cancelled -> emit(ChatSendEvent.Failed(ChatFailure.CANCELLED))
                is InferenceEvent.Completed -> storeReply(conversationId, reply.toString())
                is InferenceEvent.Queued, is InferenceEvent.Started -> Unit
            }
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<ChatSendEvent>.storeReply(
        conversationId: String,
        value: String,
    ) {
        val completedReply = value.trim()
        if (completedReply.isEmpty()) {
            emit(ChatSendEvent.Failed(ChatFailure.INVALID_RESPONSE))
            return
        }
        val completedAt = System.currentTimeMillis()
        conversationStore.appendMessage(
            conversationId,
            StoredMessage.Role.ASSISTANT,
            completedReply,
            completedAt,
        )
        emit(ChatSendEvent.ReplyStored)
    }

    private fun InferenceFailure.toChatFailure(): ChatFailure = when (code) {
        InferenceFailure.Code.PROTOCOL_FAILURE, InferenceFailure.Code.INVALID_REQUEST -> ChatFailure.INVALID_RESPONSE
        else -> ChatFailure.MODEL_UNAVAILABLE
    }

    private fun StoredMessage.toChatTurn() = ChatTurn(
        id = id,
        role = when (role) {
            StoredMessage.Role.USER -> ChatTurn.Role.USER
            StoredMessage.Role.ASSISTANT -> ChatTurn.Role.ASSISTANT
            StoredMessage.Role.SYSTEM -> ChatTurn.Role.SYSTEM
        },
        content = content,
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun StoredMessage.toModelMessage() = ChatMessage(
        role = when (role) {
            StoredMessage.Role.USER -> ChatMessage.Role.USER
            StoredMessage.Role.ASSISTANT -> ChatMessage.Role.ASSISTANT
            StoredMessage.Role.SYSTEM -> ChatMessage.Role.SYSTEM
        },
        content = content,
    )

    private fun ChatRequest.estimateInputTokens(): Int =
        messages.sumOf { it.content.length }
            .plus(CHARACTERS_PER_ESTIMATED_TOKEN - 1)
            .div(CHARACTERS_PER_ESTIMATED_TOKEN)
            .coerceAtLeast(1)

    private companion object {
        const val CHARACTERS_PER_ESTIMATED_TOKEN = 4
    }
}
