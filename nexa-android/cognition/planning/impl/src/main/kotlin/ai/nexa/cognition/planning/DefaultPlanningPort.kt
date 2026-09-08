package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.CompilationCode
import ai.nexa.cognition.planning.api.CompilationIssue
import ai.nexa.cognition.planning.api.PlanCompilationResult
import ai.nexa.cognition.planning.api.PlanCompiler
import ai.nexa.cognition.planning.api.PlanningObserver
import ai.nexa.cognition.planning.api.PlanningPort
import ai.nexa.cognition.planning.api.PlanningRecord
import ai.nexa.cognition.planning.api.PlanningRequest
import ai.nexa.cognition.planning.api.PlanningResult
import ai.nexa.cognition.planning.api.UntrustedPlanProposal
import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.SamplingParams
import ai.nexa.kernel.inference.InferenceEvent
import ai.nexa.kernel.inference.InferenceExecutionId
import ai.nexa.kernel.inference.InferenceExecutionRequest
import ai.nexa.kernel.inference.InferenceOrchestratorPort
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.RoutingEnvironmentPort
import ai.nexa.router.api.TaskClass
import java.util.UUID

/** Explicit planning path. It stops after validation and has no action executor dependency. */
class DefaultPlanningPort(
    private val orchestrator: InferenceOrchestratorPort,
    private val compiler: PlanCompiler,
    private val environment: RoutingEnvironmentPort,
    private val observer: PlanningObserver = PlanningObserver.NONE,
    private val executionId: () -> InferenceExecutionId = {
        InferenceExecutionId(UUID.randomUUID().toString())
    },
) : PlanningPort {
    override suspend fun propose(request: PlanningRequest): PlanningResult {
        val output = StringBuilder()
        var unexpectedOutput = false
        var result: PlanningResult? = null
        orchestrator.start(request.toInferenceRequest()).events.collect { event ->
            if (result == null) {
                when (event) {
                    is InferenceEvent.Queued, is InferenceEvent.Started -> Unit
                    is InferenceEvent.Delta -> when (val delta = event.value) {
                        is ChatDelta.Token -> output.append(delta.text)
                        is ChatDelta.Usage -> Unit
                        is ChatDelta.ToolCall -> unexpectedOutput = true
                    }
                    is InferenceEvent.Completed -> result = if (unexpectedOutput) {
                        invalid(CompilationCode.UNEXPECTED_MODEL_OUTPUT)
                    } else {
                        compiler.compile(UntrustedPlanProposal(output.toString())).toPlanningResult()
                    }
                    is InferenceEvent.Failed -> result = PlanningResult.InferenceFailed(event.failure)
                    is InferenceEvent.Cancelled -> result = PlanningResult.Cancelled
                    is InferenceEvent.TimedOut -> result = PlanningResult.TimedOut
                }
            }
        }
        val finalResult = result ?: invalid(CompilationCode.UNEXPECTED_MODEL_OUTPUT)
        observer.onResult(finalResult.toRecord(request))
        return finalResult
    }

    private fun PlanningRequest.toInferenceRequest(): InferenceExecutionRequest {
        val chatRequest = ChatRequest(
            messages = listOf(
                ChatMessage(ChatMessage.Role.SYSTEM, PLANNING_SYSTEM_INSTRUCTION),
                ChatMessage(ChatMessage.Role.USER, goal),
            ),
            privacyClass = privacyClass,
            latencyBudget = LatencyBudget.BACKGROUND,
            toolSchemas = emptyList(),
            sampling = SamplingParams(temperature = 0.0, maxOutputTokens = MAX_OUTPUT_TOKENS),
        )
        return InferenceExecutionRequest(
            executionId = executionId(),
            correlationId = ai.nexa.kernel.inference.InferenceCorrelationId(correlationId.value),
            routeRequest = ChatRouteRequest(
                request = chatRequest,
                taskClass = TaskClass.PLAN,
                qualityNeed = ModelManifest.QualityTier.STRONG,
                estimatedInputTokens = maxOf(1, (goal.length + PLANNING_SYSTEM_INSTRUCTION.length) / CHARS_PER_TOKEN),
                deviceState = environment.currentDeviceState(),
            ),
            timeoutMillis = timeoutMillis,
        )
    }

    private fun PlanCompilationResult.toPlanningResult(): PlanningResult = when (this) {
        is PlanCompilationResult.Valid -> PlanningResult.Valid(plan)
        is PlanCompilationResult.Rejected -> PlanningResult.InvalidProposal(
            compilationIssues = compilationIssues,
            validationIssues = validationIssues,
        )
    }

    private fun invalid(code: CompilationCode) = PlanningResult.InvalidProposal(
        compilationIssues = listOf(CompilationIssue(code)),
        validationIssues = emptyList(),
    )

    private fun PlanningResult.toRecord(request: PlanningRequest): PlanningRecord = when (this) {
        is PlanningResult.Valid -> PlanningRecord(
            correlationId = request.correlationId,
            outcome = PlanningRecord.Outcome.VALID,
            planId = plan.plan.id,
            schemaVersion = plan.plan.version,
            nodeCount = plan.orderedNodes.size,
            capabilities = plan.requiredCapabilities.mapTo(mutableSetOf()) { it.capability },
        )
        is PlanningResult.InvalidProposal -> PlanningRecord(
            correlationId = request.correlationId,
            outcome = PlanningRecord.Outcome.INVALID_PROPOSAL,
            compilationCodes = compilationIssues.mapTo(mutableSetOf()) { it.code },
            validationCodes = validationIssues.mapTo(mutableSetOf()) { it.code },
        )
        is PlanningResult.InferenceFailed -> PlanningRecord(
            correlationId = request.correlationId,
            outcome = PlanningRecord.Outcome.INFERENCE_FAILED,
        )
        PlanningResult.Cancelled -> PlanningRecord(
            correlationId = request.correlationId,
            outcome = PlanningRecord.Outcome.CANCELLED,
        )
        PlanningResult.TimedOut -> PlanningRecord(
            correlationId = request.correlationId,
            outcome = PlanningRecord.Outcome.TIMED_OUT,
        )
    }

    private companion object {
        const val MAX_OUTPUT_TOKENS = 4_096
        const val CHARS_PER_TOKEN = 4
        const val PLANNING_SYSTEM_INSTRUCTION =
            "Return only a schemaVersion 1 NEXA plan JSON object. Do not include prose or reasoning. " +
                "Allowed action types: OPEN_APP, OPEN_URL, CREATE_REMINDER, DRAFT_MESSAGE. " +
                "Every node must declare dependsOn, capabilities, and one closed action object."
    }
}
