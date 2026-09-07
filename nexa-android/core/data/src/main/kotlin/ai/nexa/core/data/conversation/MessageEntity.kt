package ai.nexa.core.data.conversation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One immutable conversation turn. Model output is stored as text, never as executable action data. */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["conversation_id", "created_at_epoch_ms", "id"]),
    ],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val role: MessageRole,
    val content: String,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMillis: Long,
) {
    init {
        require(id.isNotBlank()) { "Message id cannot be blank" }
        require(conversationId.isNotBlank()) { "Conversation id cannot be blank" }
        require(content.isNotBlank()) { "Message content cannot be blank" }
        require(createdAtEpochMillis >= 0) { "Message creation time cannot be negative" }
    }
}

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
}
