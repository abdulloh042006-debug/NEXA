package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.CapabilityRequirement
import java.util.Collections

enum class ProposedActionKind {
    OPEN_APP,
    OPEN_URL,
    CREATE_REMINDER,
    DRAFT_MESSAGE,
}

data class ProposedActionInspection(
    val nodeId: PlanNodeId,
    val kind: ProposedActionKind,
    val dependencies: List<PlanNodeId>,
    val requiredCapabilities: Set<CapabilityRequirement>,
)

/** Deterministic description of requested effects. This is not an execution simulation. */
data class PlanInspection(
    val planId: PlanId,
    val schemaVersion: PlanVersion,
    val nodeCount: Int,
    val proposedActions: List<ProposedActionInspection>,
    val authorizationRequired: Boolean = true,
)

class PlanInspector {
    fun inspect(plan: ValidatedPlan): PlanInspection = PlanInspection(
        planId = plan.plan.id,
        schemaVersion = plan.plan.version,
        nodeCount = plan.orderedNodes.size,
        proposedActions = Collections.unmodifiableList(
            plan.orderedNodes.map { node ->
                ProposedActionInspection(
                    nodeId = node.id,
                    kind = node.action.kind(),
                    dependencies = node.dependencies,
                    requiredCapabilities = node.action.requiredCapabilities,
                )
            },
        ),
    )

    private fun ActionIntent.kind(): ProposedActionKind = when (this) {
        is ActionIntent.OpenApp -> ProposedActionKind.OPEN_APP
        is ActionIntent.OpenUrl -> ProposedActionKind.OPEN_URL
        is ActionIntent.CreateReminder -> ProposedActionKind.CREATE_REMINDER
        is ActionIntent.DraftMessage -> ProposedActionKind.DRAFT_MESSAGE
    }
}
