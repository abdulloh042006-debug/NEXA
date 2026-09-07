package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.testing.FakeChatModelPort
import ai.nexa.core.data.db.NexaDatabase
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class DefaultChatSessionPortTest {
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
    fun userAndAssistantTurnsFlowThroughRoom() = runTest {
        val model = FakeChatModelPort(
            script = { listOf(ChatDelta.Token("Javob"), ChatDelta.Usage(1, 1)) },
        )
        val session = session(model)
        val conversationId = session.createConversation()

        val events = session.sendMessage(conversationId, "  Salom  ").toList()

        assertEquals(
            listOf(ChatSendEvent.UserStored, ChatSendEvent.ReplyToken("Javob"), ChatSendEvent.ReplyStored),
            events,
        )
        assertEquals(
            listOf("Salom", "Javob"),
            session.observeMessages(conversationId).first().map(ChatTurn::content),
        )
    }

    @Test
    fun modelFailureKeepsAlreadyStoredUserTurn() = runTest {
        val session = session(FakeChatModelPort(failure = IllegalStateException("offline")))
        val conversationId = session.createConversation()

        assertFailsWith<IllegalStateException> {
            session.sendMessage(conversationId, "Salom").toList()
        }

        assertEquals(
            listOf("Salom"),
            session.observeMessages(conversationId).first().map(ChatTurn::content),
        )
    }

    private fun session(model: FakeChatModelPort) = DefaultChatSessionPort(
        database = database,
        conversationDao = database.conversationDao(),
        messageDao = database.messageDao(),
        chatModel = model,
    )
}
