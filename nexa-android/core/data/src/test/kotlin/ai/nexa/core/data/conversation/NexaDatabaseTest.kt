package ai.nexa.core.data.conversation

import ai.nexa.core.data.db.NexaDatabase
import ai.nexa.core.data.db.NexaDatabaseFactory
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class NexaDatabaseTest {
    private lateinit var database: NexaDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            NexaDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun conversationAndMessagesRoundTripInStableOrder() = runTest {
        val conversation = conversation(id = "conversation-1")
        database.conversationDao().insert(conversation)
        database.messageDao().insert(message("message-2", conversation.id, 20))
        database.messageDao().insert(message("message-1", conversation.id, 10))

        assertEquals(conversation, database.conversationDao().findById(conversation.id))
        assertEquals(
            listOf("message-1", "message-2"),
            database.messageDao().listForConversation(conversation.id).map { it.id },
        )
        assertEquals(2, database.messageDao().observeForConversation(conversation.id).first().size)
    }

    @Test
    fun deletingConversationCascadesMessages() = runTest {
        val conversation = conversation(id = "conversation-1")
        database.conversationDao().insert(conversation)
        database.messageDao().insert(message("message-1", conversation.id, 10))

        database.conversationDao().deleteById(conversation.id)

        assertNull(database.conversationDao().findById(conversation.id))
        assertEquals(emptyList(), database.messageDao().listForConversation(conversation.id))
    }

    @Test
    fun foreignKeyRejectsOrphanMessage() = runTest {
        assertFailsWith<Exception> {
            database.messageDao().insert(message("orphan", "missing", 10))
        }
    }

    @Test
    fun duplicatePrimaryKeyIsRejected() = runTest {
        val conversation = conversation(id = "conversation-1")
        database.conversationDao().insert(conversation)

        assertFailsWith<Exception> { database.conversationDao().insert(conversation) }
    }

    @Test
    fun databaseStartsAtSchemaVersionOne() {
        assertEquals(1, NexaDatabase.VERSION)
    }

    @Test
    fun encryptedFactoryRejectsEmptyPassphraseBeforeOpeningStorage() {
        assertFailsWith<IllegalArgumentException> {
            NexaDatabaseFactory.create(
                ApplicationProvider.getApplicationContext(),
                byteArrayOf(),
            )
        }
    }

    private fun conversation(id: String) = ConversationEntity(
        id = id,
        title = null,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 1,
    )

    private fun message(id: String, conversationId: String, createdAt: Long) = MessageEntity(
        id = id,
        conversationId = conversationId,
        role = MessageRole.USER,
        content = "Hello",
        createdAtEpochMillis = createdAt,
    )
}
