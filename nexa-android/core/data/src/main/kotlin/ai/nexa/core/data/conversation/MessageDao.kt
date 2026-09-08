package ai.nexa.core.data.conversation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: MessageEntity)

    @Query(
        "SELECT * FROM messages WHERE conversation_id = :conversationId " +
            "ORDER BY created_at_epoch_ms ASC, id ASC",
    )
    fun observeForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query(
        "SELECT * FROM messages WHERE conversation_id = :conversationId " +
            "ORDER BY created_at_epoch_ms ASC, id ASC",
    )
    suspend fun listForConversation(conversationId: String): List<MessageEntity>
}
