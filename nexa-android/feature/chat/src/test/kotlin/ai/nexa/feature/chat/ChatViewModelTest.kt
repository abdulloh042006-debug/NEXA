package ai.nexa.feature.chat

import ai.nexa.kernel.chat.ChatSendEvent
import ai.nexa.kernel.chat.ChatSessionPort
import ai.nexa.kernel.chat.ChatTurn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun sendsTrimmedDraftAndPublishesPersistedConversation() = runTest(dispatcher) {
        val session = FakeSession()
        val viewModel = ChatViewModel(session)
        advanceUntilIdle()

        viewModel.updateDraft("  Salom  ")
        viewModel.send()
        advanceUntilIdle()

        assertEquals("Salom", session.sentContent)
        assertEquals(listOf("Salom", "Javob"), viewModel.state.value.messages.map(ChatTurn::content))
        assertEquals("", viewModel.state.value.draft)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun modelFailureLeavesUiUsableAndReportsError() = runTest(dispatcher) {
        val session = FakeSession(fail = true)
        val viewModel = ChatViewModel(session)
        advanceUntilIdle()

        viewModel.updateDraft("Salom")
        viewModel.send()
        advanceUntilIdle()

        assertEquals(ChatUiError.MODEL_UNAVAILABLE, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf("Salom"), viewModel.state.value.messages.map(ChatTurn::content))
    }

    private class FakeSession(private val fail: Boolean = false) : ChatSessionPort {
        private val messages = MutableStateFlow<List<ChatTurn>>(emptyList())
        var sentContent: String? = null

        override suspend fun createConversation(): String = "conversation"

        override fun observeMessages(conversationId: String): Flow<List<ChatTurn>> = messages

        override fun sendMessage(conversationId: String, content: String): Flow<ChatSendEvent> = flow {
            sentContent = content
            messages.value = listOf(ChatTurn("user", ChatTurn.Role.USER, content, 1))
            emit(ChatSendEvent.UserStored)
            if (fail) error("offline")
            emit(ChatSendEvent.ReplyToken("Javob"))
            messages.value = messages.value + ChatTurn("assistant", ChatTurn.Role.ASSISTANT, "Javob", 2)
            emit(ChatSendEvent.ReplyStored)
        }
    }
}
