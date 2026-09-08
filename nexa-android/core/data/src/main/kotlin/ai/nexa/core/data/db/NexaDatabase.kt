package ai.nexa.core.data.db

import ai.nexa.core.data.conversation.ConversationConverters
import ai.nexa.core.data.conversation.ConversationDao
import ai.nexa.core.data.conversation.ConversationEntity
import ai.nexa.core.data.conversation.MessageDao
import ai.nexa.core.data.conversation.MessageEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = NexaDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(ConversationConverters::class)
abstract class NexaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    companion object {
        const val VERSION: Int = 1
        const val FILE_NAME: String = "nexa.db"
    }
}
