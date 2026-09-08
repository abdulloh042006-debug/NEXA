package ai.nexa.cognition.planning.api

@JvmInline
value class UntrustedPlanProposal(val content: String)

enum class CompilationCode {
    EMPTY_PROPOSAL,
    PROPOSAL_TOO_LARGE,
    MALFORMED_DOCUMENT,
    DUPLICATE_FIELD,
    UNKNOWN_FIELD,
    MISSING_FIELD,
    INVALID_FIELD,
    UNSUPPORTED_SCHEMA,
    UNKNOWN_ACTION,
    VALIDATION_FAILED,
}

data class CompilationIssue(val code: CompilationCode)

sealed interface PlanCompilationResult {
    data class Valid(val plan: ValidatedPlan) : PlanCompilationResult

    data class Rejected(
        val compilationIssues: List<CompilationIssue>,
        val validationIssues: List<ValidationIssue> = emptyList(),
    ) : PlanCompilationResult
}

/** Converts untrusted model data into a validated typed plan. It cannot execute actions. */
fun interface PlanCompiler {
    fun compile(proposal: UntrustedPlanProposal): PlanCompilationResult
}
