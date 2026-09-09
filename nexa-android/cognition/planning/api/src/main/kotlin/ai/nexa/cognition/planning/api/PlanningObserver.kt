package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.CapabilityRequirement

data class PlanningRecord(
    val correlationId: PlanCorrelationId,
    val outcome: Outcome,
    val planId: PlanId? = null,
    val schemaVersion: PlanVersion? = null,
    val nodeCount: Int? = null,
    val compilationCodes: Set<CompilationCode> = emptySet(),
    val validationCodes: Set<ValidationCode> = emptySet(),
    val capabilities: Set<CapabilityRequirement.Capability> = emptySet(),
) {
    enum class Outcome { VALID, INVALID_PROPOSAL, INFERENCE_FAILED, CANCELLED, TIMED_OUT }
}

fun interface PlanningObserver {
    fun onResult(record: PlanningRecord)

    companion object {
        val NONE = PlanningObserver { }
    }
}
