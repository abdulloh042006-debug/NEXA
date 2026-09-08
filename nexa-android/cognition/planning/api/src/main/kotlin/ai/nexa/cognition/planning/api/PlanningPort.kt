package ai.nexa.cognition.planning.api

import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.kernel.inference.InferenceFailure

data class PlanningRequest(
    val goal: String,
    val correlationId: PlanCorrelationId,
    val privacyClass: PrivacyClass,
    val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) {
    init {
        require(goal.isNotBlank() && goal.length <= MAX_GOAL_LENGTH) { "invalid planning goal" }
        require(timeoutMillis in 1..MAX_TIMEOUT_MILLIS) { "invalid planning timeout" }
    }

    private companion object {
        const val MAX_GOAL_LENGTH = 4_096
        const val DEFAULT_TIMEOUT_MILLIS = 30_000L
        const val MAX_TIMEOUT_MILLIS = 300_000L
    }
}

sealed interface PlanningResult {
    data class Valid(val plan: ValidatedPlan) : PlanningResult

    data class InvalidProposal(
        val compilationIssues: List<CompilationIssue>,
        val validationIssues: List<ValidationIssue>,
    ) : PlanningResult

    data class InferenceFailed(val failure: InferenceFailure) : PlanningResult

    data object Cancelled : PlanningResult

    data object TimedOut : PlanningResult
}

/** Explicit kernel-facing capability for plan proposals. It returns data and never performs actions. */
fun interface PlanningPort {
    suspend fun propose(request: PlanningRequest): PlanningResult
}
