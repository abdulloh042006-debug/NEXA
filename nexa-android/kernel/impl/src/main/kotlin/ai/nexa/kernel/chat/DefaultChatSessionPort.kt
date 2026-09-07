package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.data.conversation.ConversationDao
import ai.nexa.core.data.conversation.ConversationEntity
import ai.nexa.core.data.conversation.MessageDao
import ai.nexa.core.data.conversation.MessageEntity
import ai.nexa.core.data.conversation.MessageRole
import ai.nexa.core.data.db.NexaDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class DefaultChatSessionPort @Inject constructor(
    private val database: NexaDatabase,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val chatModel: ChatModelPort,
) : ChatSessionPort {
    override suspend fun createConversation(): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        conversationDao.insert(ConversationEntity(id, null, now, now))
        return id
    }

    override fun observeMessages(conversationId: String): Flow<List<ChatTurn>> =
        messageDao.observeForConversation(conversationId).map { messages -> messages.map(MessageEntity::toChatTurn) }

    override fun sendMessage(conversationId: String, content: String): Flow<ChatSendEvent> = flow {
        val normalized = content.trim()
        require(normalized.isNotEmpty()) { "Message cannot be blank" }
        val now = System.currentTimeMillis()
        database.withTransaction {
            checkNotNull(conversationDao.findById(conversationId)) { "Conversation does not exist" }
            messageDao.insert(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = MessageRole.USER,
                    content = normalized,
                    createdAtEpochMillis = now,
                ),
            )
            conversationDao.updateTimestamp(conversationId, now)
        }
        emit(ChatSendEvent.UserStored)

        val history = messageDao.listForConversation(conversationId)
        val reply = StringBuilder()
        chatModel.streamChat(
            ChatRequest(
                messages = history.map(MessageEntity::toModelMessage),
                privacyClass = PrivacyClass.P2_SENSITIVE,
                latencyBudget = LatencyBudget.INTERACTIVE,
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
        database.withTransaction {
            messageDao.insert(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = MessageRole.ASSISTANT,
                    content = completedReply,
                    createdAtEpochMillis = completedAt,
                ),
            )
            conversationDao.updateTimestamp(conversationId, completedAt)
        }
        emit(ChatSendEvent.ReplyStored)
    }

    private fun MessageEntity.toChatTurn() = ChatTurn(
        id = id,
        role = when (role) {
            MessageRole.USER -> ChatTurn.Role.USER
            MessageRole.ASSISTANT -> ChatTurn.Role.ASSISTANT
            MessageRole.SYSTEM -> ChatTurn.Role.SYSTEM
        },
        content = content,
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun MessageEntity.toModelMessage() = ChatMessage(
        role = when (role) {
            MessageRole.USER -> ChatMessage.Role.USER
            MessageRole.ASSISTANT -> ChatMessage.Role.ASSISTANT
            MessageRole.SYSTEM -> ChatMessage.Role.SYSTEM
        },
        content = content,
    )
}
