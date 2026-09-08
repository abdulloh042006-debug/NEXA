package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.data.conversation.ConversationStore
import ai.nexa.core.data.conversation.StoredMessage
import ai.nexa.kernel.inference.InferenceEvent
import ai.nexa.kernel.inference.InferenceExecution
import ai.nexa.kernel.inference.InferenceExecutionRequest
import ai.nexa.kernel.inference.InferenceFailure
import ai.nexa.kernel.inference.InferenceOrchestratorPort
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.RoutingEnvironmentPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class DefaultChatSessionPortTest {
    @Test
    fun userAndAssistantTurnsFlowThroughStore() = runTest {
        val store = InMemoryStore()
        val orchestrator = RecordingOrchestrator(
            listOf(
                InferenceEvent.Queued(1),
                InferenceEvent.Started("model@v1", "provider", 2),
                InferenceEvent.Delta(ChatDelta.Token("Javob"), 3),
                InferenceEvent.Delta(ChatDelta.Usage(1, 1), 4),
                InferenceEvent.Completed(5),
            ),
        )
        val session = DefaultChatSessionPort(store, orchestrator, TEST_ENVIRONMENT)
        val conversationId = session.createConversation()

        val events = session.sendMessage(conversationId, "  Salom  ").toList()

        assertEquals(
            listOf(ChatSendEvent.UserStored, ChatSendEvent.ReplyToken("Javob"), ChatSendEvent.ReplyStored),
            events,
        )
        assertEquals(listOf("Salom", "Javob"), store.messages.value.map(StoredMessage::content))
        assertEquals(TEST_DEVICE_STATE, orchestrator.lastRequest?.routeRequest?.deviceState)
        assertEquals(2, orchestrator.lastRequest?.routeRequest?.estimatedInputTokens)
    }

    @Test
    fun modelFailureKeepsAlreadyStoredUserTurn() = runTest {
        val store = InMemoryStore()
        val session = DefaultChatSessionPort(
            store,
            RecordingOrchestrator(
                listOf(
                    InferenceEvent.Failed(
                        InferenceFailure(InferenceFailure.Code.BACKEND_UNAVAILABLE, true),
                        1,
                    ),
                ),
            ),
            TEST_ENVIRONMENT,
        )
        val conversationId = session.createConversation()

        val events = session.sendMessage(conversationId, "Salom").toList()

        assertEquals(listOf("Salom"), store.messages.value.map(StoredMessage::content))
        assertEquals(ChatSendEvent.Failed(ChatFailure.MODEL_UNAVAILABLE), events.last())
    }

    private class RecordingOrchestrator(
        private val scriptedEvents: List<InferenceEvent>,
    ) : InferenceOrchestratorPort {
        var lastRequest: InferenceExecutionRequest? = null

        override fun start(request: InferenceExecutionRequest): InferenceExecution {
            lastRequest = request
            return object : InferenceExecution {
                override val events = flowOf(*scriptedEvents.toTypedArray())
                override fun cancel(): Boolean = false
            }
        }
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

    private companion object {
        val TEST_DEVICE_STATE = RoutingDeviceState(
            network = NetworkState.UNMETERED,
            availableRamMb = 4_096,
            availableOnDeviceModelIds = emptySet(),
        )
        val TEST_ENVIRONMENT = RoutingEnvironmentPort { TEST_DEVICE_STATE }
    }
}
