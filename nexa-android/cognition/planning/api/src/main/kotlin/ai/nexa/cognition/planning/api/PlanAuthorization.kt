package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.AuthorizationContext
import ai.nexa.core.permission.AuthorizationDecision
import ai.nexa.core.permission.CapabilityTargetKind
import ai.nexa.core.permission.GrantId
import ai.nexa.core.permission.RequestedGrantScope
import java.util.Collections

enum class PlanAuthorizationStatus { AUTHORIZED, DENIED, CONSENT_REQUIRED }

data class NodeAuthorizationDecision(
    val nodeId: PlanNodeId,
    val decision: AuthorizationDecision,
)

data class ConsentRequest(
    val nodeId: PlanNodeId,
    val actionKind: ProposedActionKind,
    val capabilityId: String,
    val targetKind: CapabilityTargetKind,
    val requestedScope: RequestedGrantScope,
)

/**
 * Immutable evidence from one evaluation. It is not a bearer token: Phase 6 must revalidate every
 * grant and runtime prerequisite immediately before a side effect.
 */
class PlanAuthorizationSnapshot(
    val planId: PlanId,
    val planVersion: PlanVersion,
    val planFingerprint: String,
    grantIds: Collection<GrantId>,
    decisions: Collection<NodeAuthorizationDecision>,
    val issuedAtEpochMillis: Long,
) {
    val grantIds: List<GrantId> = immutableCopy(grantIds.sorted())
    val decisions: List<NodeAuthorizationDecision> = immutableCopy(decisions)
    val requiresLiveRevalidation: Boolean = true

    init {
        require(planFingerprint.matches(FINGERPRINT)) { "invalid plan fingerprint" }
    }

    private companion object {
        val FINGERPRINT = Regex("[a-f0-9]{64}")
    }
}

class PlanAuthorizationResult(
    val planId: PlanId,
    val status: PlanAuthorizationStatus,
    nodeDecisions: Collection<NodeAuthorizationDecision>,
    consentRequests: Collection<ConsentRequest>,
    val snapshot: PlanAuthorizationSnapshot?,
) {
    val nodeDecisions: List<NodeAuthorizationDecision> = immutableCopy(nodeDecisions)
    val allowedNodes: List<PlanNodeId> = nodesWith(ai.nexa.core.permission.AuthorizationStatus.ALLOW)
    val deniedNodes: List<PlanNodeId> = nodesWith(ai.nexa.core.permission.AuthorizationStatus.DENY)
    val consentRequiredNodes: List<PlanNodeId> =
        nodesWith(ai.nexa.core.permission.AuthorizationStatus.REQUIRE_CONSENT)
    val consentRequests: List<ConsentRequest> = immutableCopy(consentRequests)

    init {
        require((status == PlanAuthorizationStatus.AUTHORIZED) == (snapshot != null)) {
            "only fully authorized plans may carry a snapshot"
        }
    }

    private fun nodesWith(status: ai.nexa.core.permission.AuthorizationStatus): List<PlanNodeId> =
        immutableCopy(nodeDecisions.filter { it.decision.status == status }.map { it.nodeId })
}

fun interface PlanAuthorizationPort {
    fun authorize(plan: ValidatedPlan, context: AuthorizationContext): PlanAuthorizationResult
}

private fun <T> immutableCopy(values: Collection<T>): List<T> =
    Collections.unmodifiableList(values.toList())
