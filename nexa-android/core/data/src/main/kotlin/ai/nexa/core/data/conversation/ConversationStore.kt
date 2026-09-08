package ai.nexa.core.data.conversation

import ai.nexa.core.data.db.NexaDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/** Persistence boundary: callers never receive Room entities or transaction APIs. */
interface ConversationStore {
    suspend fun createConversation(createdAtEpochMillis: Long): String

    fun observeMessages(conversationId: String): Flow<List<StoredMessage>>

    suspend fun listMessages(conversationId: String): List<StoredMessage>

    suspend fun appendMessage(
        conversationId: String,
        role: StoredMessage.Role,
        content: String,
        createdAtEpochMillis: Long,
    )
}

data class StoredMessage(
    val id: String,
    val role: Role,
    val content: String,
    val createdAtEpochMillis: Long,
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

class RoomConversationStore @Inject constructor(
    private val database: NexaDatabase,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) : ConversationStore {
    override suspend fun createConversation(createdAtEpochMillis: Long): String {
        val id = UUID.randomUUID().toString()
        conversationDao.insert(ConversationEntity(id, null, createdAtEpochMillis, createdAtEpochMillis))
        return id
    }

    override fun observeMessages(conversationId: String): Flow<List<StoredMessage>> =
        messageDao.observeForConversation(conversationId).map { messages -> messages.map { it.toStoredMessage() } }

    override suspend fun listMessages(conversationId: String): List<StoredMessage> =
        messageDao.listForConversation(conversationId).map { it.toStoredMessage() }

    override suspend fun appendMessage(
        conversationId: String,
        role: StoredMessage.Role,
        content: String,
        createdAtEpochMillis: Long,
    ) {
        database.withTransaction {
            checkNotNull(conversationDao.findById(conversationId)) { "Conversation does not exist" }
            messageDao.insert(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    role = role.toEntityRole(),
                    content = content,
                    createdAtEpochMillis = createdAtEpochMillis,
                ),
            )
            conversationDao.updateTimestamp(conversationId, createdAtEpochMillis)
        }
    }

    private fun MessageEntity.toStoredMessage() = StoredMessage(
        id = id,
        role = when (role) {
            MessageRole.USER -> StoredMessage.Role.USER
            MessageRole.ASSISTANT -> StoredMessage.Role.ASSISTANT
            MessageRole.SYSTEM -> StoredMessage.Role.SYSTEM
        },
        content = content,
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun StoredMessage.Role.toEntityRole() = when (this) {
        StoredMessage.Role.USER -> MessageRole.USER
        StoredMessage.Role.ASSISTANT -> MessageRole.ASSISTANT
        StoredMessage.Role.SYSTEM -> MessageRole.SYSTEM
    }
}
