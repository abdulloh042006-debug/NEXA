package ai.nexa.kernel.chat

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.Embedding
import ai.nexa.core.ai.testing.FakeChatModelPort
import ai.nexa.core.data.conversation.ConversationStore
import ai.nexa.core.data.conversation.StoredMessage
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.EmbeddingRouteRequest
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RouteDecision
import ai.nexa.router.api.RouterPort
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.RoutingEnvironmentPort
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
        val router = RecordingRouter(model)
        val session = DefaultChatSessionPort(store, router, TEST_ENVIRONMENT)
        val conversationId = session.createConversation()

        val events = session.sendMessage(conversationId, "  Salom  ").toList()

        assertEquals(
            listOf(ChatSendEvent.UserStored, ChatSendEvent.ReplyToken("Javob"), ChatSendEvent.ReplyStored),
            events,
        )
        assertEquals(listOf("Salom", "Javob"), store.messages.value.map(StoredMessage::content))
        assertEquals(TEST_DEVICE_STATE, router.lastRequest?.deviceState)
        assertEquals(2, router.lastRequest?.estimatedInputTokens)
    }

    @Test
    fun modelFailureKeepsAlreadyStoredUserTurn() = runTest {
        val store = InMemoryStore()
        val session = DefaultChatSessionPort(
            store,
            RecordingRouter(FakeChatModelPort(failure = IllegalStateException("offline"))),
            TEST_ENVIRONMENT,
        )
        val conversationId = session.createConversation()

        assertFailsWith<IllegalStateException> {
            session.sendMessage(conversationId, "Salom").toList()
        }

        assertEquals(listOf("Salom"), store.messages.value.map(StoredMessage::content))
    }

    private class RecordingRouter(
        private val model: FakeChatModelPort,
    ) : RouterPort {
        var lastRequest: ChatRouteRequest? = null

        override suspend fun resolveChat(request: ChatRouteRequest): RouteDecision = error("not used")

        override fun streamChat(request: ChatRouteRequest): Flow<ChatDelta> {
            lastRequest = request
            return model.streamChat(request.request)
        }

        override suspend fun resolveEmbedding(request: EmbeddingRouteRequest): RouteDecision = error("not used")

        override suspend fun routeEmbedding(
            texts: List<String>,
            request: EmbeddingRouteRequest,
        ): List<Embedding> = error("not used")
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
