package ai.nexa.kernel.chat

import kotlinx.coroutines.flow.Flow

/** Feature-facing chat contract. Model and persistence types never cross this boundary. */
interface ChatSessionPort {
    suspend fun createConversation(): String

    fun observeMessages(conversationId: String): Flow<List<ChatTurn>>

    fun sendMessage(conversationId: String, content: String): Flow<ChatSendEvent>
}

data class ChatTurn(
    val id: String,
    val role: Role,
    val content: String,
    val createdAtEpochMillis: Long,
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

sealed interface ChatSendEvent {
    data object UserStored : ChatSendEvent

    data class ReplyToken(val text: String) : ChatSendEvent

    data object ReplyStored : ChatSendEvent

    data class Failed(val reason: ChatFailure) : ChatSendEvent
}

enum class ChatFailure { MODEL_UNAVAILABLE, TIMEOUT, CANCELLED, INVALID_RESPONSE }
