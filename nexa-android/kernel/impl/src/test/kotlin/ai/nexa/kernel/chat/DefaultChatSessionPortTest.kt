package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.testing.FakeChatModelPort
import ai.nexa.core.data.conversation.ConversationStore
import ai.nexa.core.data.conversation.StoredMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultChatSessionPortTest {
    @Test
    fun userAndAssistantTurnsFlowThroughStore() = runTest {
        val store = InMemoryStore()
        val model = FakeChatModelPort(
            script = { listOf(ChatDelta.Token("Javob"), ChatDelta.Usage(1, 1)) },
        )
        val session = DefaultChatSessionPort(store, model)
        val conversationId = session.createConversation()

        val events = session.sendMessage(conversationId, "  Salom  ").toList()

        assertEquals(
            listOf(ChatSendEvent.UserStored, ChatSendEvent.ReplyToken("Javob"), ChatSendEvent.ReplyStored),
            events,
        )
        assertEquals(listOf("Salom", "Javob"), store.messages.value.map(StoredMessage::content))
    }

    @Test
    fun modelFailureKeepsAlreadyStoredUserTurn() = runTest {
        val store = InMemoryStore()
        val session = DefaultChatSessionPort(
            store,
            FakeChatModelPort(failure = IllegalStateException("offline")),
        )
        val conversationId = session.createConversation()

        assertFailsWith<IllegalStateException> {
            session.sendMessage(conversationId, "Salom").toList()
        }

        assertEquals(listOf("Salom"), store.messages.value.map(StoredMessage::content))
    }

    private class InMemoryStore : ConversationStore {
        val messages = MutableStateFlow<List<StoredMessage>>(emptyList())

        override suspend fun createConversation(createdAtEpochMillis: Long): String = "conversation"

        override fun observeMessages(conversationId: String): Flow<List<StoredMessage>> = messages

        override suspend fun listMessages(conversationId: String): List<StoredMessage> = messages.value

        override suspend fun appendMessage(
            conversationId: String,
            role: StoredMessage.Role,
            content: String,
            createdAtEpochMillis: Long,
        ) {
            messages.value = messages.value + StoredMessage(
                id = messages.value.size.toString(),
                role = role,
                content = content,
                createdAtEpochMillis = createdAtEpochMillis,
            )
        }
    }
}
