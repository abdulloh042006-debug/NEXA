package ai.nexa.kernel.inference

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.ai.port.ChatModelRegistry
import ai.nexa.core.ai.port.ModelInvocationException
import ai.nexa.core.ai.testing.FakeChatModelPort
import ai.nexa.core.ai.testing.FakeManifests
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.EmbeddingRouteRequest
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RouteDecision
import ai.nexa.router.api.RouterPort
import ai.nexa.router.api.RoutingDecisionRecord
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.SelectionReason
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultInferenceOrchestratorTest {
    @Test
    fun `normal stream preserves order and completes once`() = runTest {
        val model = FakeChatModelPort(script = { listOf(ChatDelta.Token("a"), ChatDelta.Token("b")) })

        val events = orchestrator(listOf(model), decision(model)).start(request()).events.toList()

        assertEquals(listOf("a", "b"), events.filterIsInstance<InferenceEvent.Delta>().map { (it.value as ChatDelta.Token).text })
        assertEquals(1, events.count { it is InferenceEvent.Completed })
    }

    @Test
    fun `zero delta backend still completes exactly once`() = runTest {
        val model = FakeChatModelPort(script = { emptyList() })

        val events = orchestrator(listOf(model), decision(model)).start(request()).events.toList()

        assertEquals(1, events.count { it is InferenceEvent.Completed })
        assertTrue(events.none { it is InferenceEvent.Delta })
    }

    @Test
    fun `provider failure before first delta falls back`() = runTest {
        val first = FakeChatModelPort(
            manifest = FakeManifests.cloudChat(id = "first@v1"),
            script = { emptyList() },
            failure = ModelInvocationException.ProviderFailure(IllegalStateException()),
        )
        val second = FakeChatModelPort(manifest = FakeManifests.chat(id = "second@v1"), script = { listOf(ChatDelta.Token("ok")) })

        val events = orchestrator(listOf(first, second), decision(first, second)).start(request()).events.toList()

        assertEquals(listOf("first@v1", "second@v1"), events.filterIsInstance<InferenceEvent.Started>().map { it.modelId })
        assertEquals("ok", (events.filterIsInstance<InferenceEvent.Delta>().single().value as ChatDelta.Token).text)
    }

    @Test
    fun `failure after first delta is terminal and never falls back`() = runTest {
        val first = FakeChatModelPort(
            manifest = FakeManifests.cloudChat(id = "first@v1"),
            script = { listOf(ChatDelta.Token("partial")) },
            failure = ModelInvocationException.ProviderFailure(IllegalStateException()),
        )
        val second = FakeChatModelPort(manifest = FakeManifests.chat(id = "second@v1"))

        val events = orchestrator(listOf(first, second), decision(first, second)).start(request()).events.toList()

        assertIs<InferenceEvent.Failed>(events.last())
        assertTrue(second.recordedRequests.isEmpty())
    }

    @Test
    fun `no route and missing backend have distinct terminal failures`() = runTest {
        val noRoute = orchestrator(emptyList(), decision()).start(request()).events.toList()
        val manifestOnly = FakeChatModelPort()
        val missing = orchestrator(emptyList(), decision(manifestOnly)).start(request()).events.toList()

        assertEquals(InferenceFailure.Code.NO_ELIGIBLE_MODEL, (noRoute.last() as InferenceEvent.Failed).failure.code)
        assertEquals(InferenceFailure.Code.BACKEND_UNAVAILABLE, (missing.last() as InferenceEvent.Failed).failure.code)
    }

    @Test
    fun `cancellation before collection is terminal and idempotent`() = runTest {
        val model = FakeChatModelPort()
        val execution = orchestrator(listOf(model), decision(model)).start(request())

        assertTrue(execution.cancel())
        assertFalse(execution.cancel())
        assertIs<InferenceEvent.Cancelled>(execution.events.toList().single())
        assertTrue(model.recordedRequests.isEmpty())
    }

    @Test
    fun `cancellation during stream stops backend and emits one terminal`() = runTest {
        val model = suspendingModel(emitFirst = true)
        val execution = orchestrator(listOf(model), decision(model)).start(request(timeoutMillis = 1_000))
        val events = mutableListOf<InferenceEvent>()
        val collector = launch { events += execution.events.toList() }
        runCurrent()

        execution.cancel()
        advanceUntilIdle()
        collector.join()

        assertEquals(1, events.count { it is InferenceEvent.Cancelled })
        assertTrue(events.none { it is InferenceEvent.Completed })
    }

    @Test
    fun `timeout before a delta is distinct and terminal`() = runTest {
        val model = suspendingModel(emitFirst = false)

        val events = orchestrator(listOf(model), decision(model)).start(request(timeoutMillis = 100)).events.toList()

        assertIs<InferenceEvent.TimedOut>(events.last())
        assertEquals(1, events.count { it is InferenceEvent.TimedOut })
    }

    @Test
    fun `observer records lifecycle without content`() = runTest {
        val records = mutableListOf<InferenceExecutionRecord>()
        val model = FakeChatModelPort(script = { listOf(ChatDelta.Token("private answer")) })
        val orchestrator = DefaultInferenceOrchestrator(
            router = FixedRouter(decision(model)),
            registry = ChatModelRegistry(listOf(model)),
            observer = { records += it },
            now = { 1 },
        )

        orchestrator.start(request(content = "private prompt")).events.toList()

        assertTrue(records.any { it.state == InferenceExecutionRecord.State.COMPLETED })
        assertFalse(records.toString().contains("private prompt"))
        assertFalse(records.toString().contains("private answer"))
    }

    @Test
    fun `state machine rejects duplicate terminal signals`() {
        val machine = ExecutionStateMachine()
        assertTrue(machine.accept(InferenceEvent.Cancelled(1)))
        assertFalse(machine.accept(InferenceEvent.Completed(2)))
        assertFalse(machine.accept(InferenceEvent.Failed(InferenceFailure(InferenceFailure.Code.PROVIDER_INTERNAL, true), 3)))
    }

    private fun orchestrator(models: List<ChatModelPort>, decision: RouteDecision) =
        DefaultInferenceOrchestrator(FixedRouter(decision), ChatModelRegistry(models), now = { 1 })

    private fun request(timeoutMillis: Long = 10_000, content: String = "hello") = InferenceExecutionRequest(
        executionId = InferenceExecutionId("execution-1"),
        correlationId = InferenceCorrelationId("correlation-1"),
        routeRequest = ChatRouteRequest(
            request = ChatRequest(
                messages = listOf(ChatMessage(ChatMessage.Role.USER, content)),
                privacyClass = PrivacyClass.P1_PERSONAL,
                latencyBudget = LatencyBudget.INTERACTIVE,
            ),
            estimatedInputTokens = 2,
            deviceState = RoutingDeviceState(NetworkState.UNMETERED, 4_096, emptySet()),
        ),
        timeoutMillis = timeoutMillis,
    )

    private fun decision(vararg models: ChatModelPort) = RouteDecision(
        ranked = models.map { it.manifest },
        scores = models.associate { it.manifest.id to 1.0 },
        excluded = emptyMap(),
        record = RoutingDecisionRecord(
            selectedModelId = models.firstOrNull()?.manifest?.id,
            candidates = emptyList(),
            selectionReasons = if (models.isEmpty()) setOf(SelectionReason.NO_ELIGIBLE_MODEL) else setOf(SelectionReason.ONLY_ELIGIBLE_MODEL),
        ),
    )

    private fun suspendingModel(emitFirst: Boolean): ChatModelPort = object : ChatModelPort {
        override val manifest = FakeManifests.chat()
        override fun streamChat(request: ChatRequest): Flow<ChatDelta> = flow {
            if (emitFirst) emit(ChatDelta.Token("partial"))
            awaitCancellation()
        }
    }

    private class FixedRouter(private val decision: RouteDecision) : RouterPort {
        override suspend fun resolveChat(request: ChatRouteRequest): RouteDecision = decision
        override suspend fun resolveEmbedding(request: EmbeddingRouteRequest): RouteDecision = error("not used")
    }
}
