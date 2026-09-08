package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.data.conversation.ConversationStore
import ai.nexa.core.data.conversation.StoredMessage
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.RouterPort
import ai.nexa.router.api.RoutingEnvironmentPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultChatSessionPort @Inject constructor(
    private val conversationStore: ConversationStore,
    private val router: RouterPort,
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
        router.streamChat(
            ChatRouteRequest(
                request = request,
                estimatedInputTokens = request.estimateInputTokens(),
                deviceState = routingEnvironment.currentDeviceState(),
            ),
        ).collect { delta ->
            when (delta) {
                is ChatDelta.Token -> {
                    reply.append(delta.text)
                    emit(ChatSendEvent.ReplyToken(delta.text))
                }
                is ChatDelta.Usage -> Unit
                is ChatDelta.ToolCall -> error("Unvalidated tool calls cannot enter the chat pipeline")
            }
        }

        val completedReply = reply.toString().trim()
        check(completedReply.isNotEmpty()) { "Model returned an empty response" }
        val completedAt = System.currentTimeMillis()
        conversationStore.appendMessage(
            conversationId,
            StoredMessage.Role.ASSISTANT,
            completedReply,
            completedAt,
        )
        emit(ChatSendEvent.ReplyStored)
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
