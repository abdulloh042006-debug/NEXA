package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.CompilationCode
import ai.nexa.cognition.planning.api.PlanCorrelationId
import ai.nexa.cognition.planning.api.PlanningObserver
import ai.nexa.cognition.planning.api.PlanningRecord
import ai.nexa.cognition.planning.api.PlanningRequest
import ai.nexa.cognition.planning.api.PlanningResult
import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.kernel.inference.InferenceEvent
import ai.nexa.kernel.inference.InferenceExecution
import ai.nexa.kernel.inference.InferenceExecutionId
import ai.nexa.kernel.inference.InferenceExecutionRequest
import ai.nexa.kernel.inference.InferenceFailure
import ai.nexa.kernel.inference.InferenceOrchestratorPort
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.RoutingEnvironmentPort
import ai.nexa.router.api.TaskClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultPlanningPortTest {
    @Test
    fun `planning uses Phase 3 orchestration and returns validated plan only`() = runTest {
        val orchestrator = FakeOrchestrator(
            flowOf(
                InferenceEvent.Queued(1),
                InferenceEvent.Started("model", "provider", 2),
                InferenceEvent.Delta(ChatDelta.Token(validProposal()), 3),
                InferenceEvent.Completed(4),
            ),
        )
        val port = port(orchestrator)

        val result = assertIs<PlanningResult.Valid>(port.propose(request()))

        assertEquals("plan-1", result.plan.plan.id.value)
        assertEquals(TaskClass.PLAN, orchestrator.request?.routeRequest?.taskClass)
        assertTrue(orchestrator.request?.routeRequest?.request?.toolSchemas.orEmpty().isEmpty())
        assertEquals(30_000L, orchestrator.request?.timeoutMillis)
    }

    @Test
    fun `invalid model output remains a structured rejected proposal`() = runTest {
        val port = port(
            FakeOrchestrator(
                flowOf(
                    InferenceEvent.Delta(ChatDelta.Token("run adb shell rm"), 1),
                    InferenceEvent.Completed(2),
                ),
            ),
        )

        val result = assertIs<PlanningResult.InvalidProposal>(port.propose(request()))

        assertEquals(listOf(CompilationCode.MALFORMED_DOCUMENT), result.compilationIssues.map { it.code })
    }

    @Test
    fun `tool call output is never treated as a plan`() = runTest {
        val port = port(
            FakeOrchestrator(
                flowOf(
                    InferenceEvent.Delta(ChatDelta.ToolCall("1", "shell", "{}"), 1),
                    InferenceEvent.Completed(2),
                ),
            ),
        )

        val result = assertIs<PlanningResult.InvalidProposal>(port.propose(request()))

        assertEquals(CompilationCode.UNEXPECTED_MODEL_OUTPUT, result.compilationIssues.single().code)
    }

    @Test
    fun `inference terminal states remain distinct planning results`() = runTest {
        val failure = InferenceFailure(InferenceFailure.Code.NO_ELIGIBLE_MODEL, false)
        assertIs<PlanningResult.InferenceFailed>(
            port(FakeOrchestrator(flowOf(InferenceEvent.Failed(failure, 1)))).propose(request()),
        )
        assertIs<PlanningResult.Cancelled>(
            port(FakeOrchestrator(flowOf(InferenceEvent.Cancelled(1)))).propose(request()),
        )
        assertIs<PlanningResult.TimedOut>(
            port(FakeOrchestrator(flowOf(InferenceEvent.TimedOut(1)))).propose(request()),
        )
    }

    @Test
    fun `planning diagnostics contain no goal or generated action payload`() = runTest {
        val records = mutableListOf<PlanningRecord>()
        val secretGoal = "private goal text"
        val port = DefaultPlanningPort(
            orchestrator = FakeOrchestrator(
                flowOf(
                    InferenceEvent.Delta(ChatDelta.Token(validProposal()), 1),
                    InferenceEvent.Completed(2),
                ),
            ),
            compiler = JsonPlanCompiler(),
            environment = environment,
            observer = PlanningObserver(records::add),
            executionId = { InferenceExecutionId("execution-1") },
        )

        port.propose(request(goal = secretGoal))

        assertEquals(PlanningRecord.Outcome.VALID, records.single().outcome)
        assertFalse(records.single().toString().contains(secretGoal))
        assertFalse(records.single().toString().contains("ai.nexa.app"))
    }

    private fun port(orchestrator: FakeOrchestrator) = DefaultPlanningPort(
        orchestrator = orchestrator,
        compiler = JsonPlanCompiler(),
        environment = environment,
        executionId = { InferenceExecutionId("execution-1") },
    )

    private fun request(goal: String = "Open NEXA") = PlanningRequest(
        goal = goal,
        correlationId = PlanCorrelationId("correlation-1"),
        privacyClass = PrivacyClass.P1_PERSONAL,
    )

    private class FakeOrchestrator(private val events: Flow<InferenceEvent>) : InferenceOrchestratorPort {
        var request: InferenceExecutionRequest? = null

        override fun start(request: InferenceExecutionRequest): InferenceExecution {
            this.request = request
            return object : InferenceExecution {
                override val events: Flow<InferenceEvent> = this@FakeOrchestrator.events
                override fun cancel(): Boolean = false
            }
        }
    }

    private companion object {
        val environment = RoutingEnvironmentPort {
            RoutingDeviceState(NetworkState.UNAVAILABLE, 0, emptySet())
        }

        fun validProposal() =
            """{"schemaVersion":1,"planId":"plan-1","metadata":{"origin":"USER"},"nodes":[{"id":"open","dependsOn":[],"capabilities":["APP_LAUNCH"],"action":{"type":"OPEN_APP","packageName":"ai.nexa.app"}}]}"""
    }
}
