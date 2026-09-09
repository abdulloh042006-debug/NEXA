package ai.nexa.cognition.planning

import ai.nexa.cognition.planning.api.ActionIntent
import ai.nexa.cognition.planning.api.ConsentRequest
import ai.nexa.cognition.planning.api.NodeAuthorizationDecision
import ai.nexa.cognition.planning.api.PlanAuthorizationPort
import ai.nexa.cognition.planning.api.PlanAuthorizationResult
import ai.nexa.cognition.planning.api.PlanAuthorizationSnapshot
import ai.nexa.cognition.planning.api.PlanAuthorizationStatus
import ai.nexa.cognition.planning.api.PlanNode
import ai.nexa.cognition.planning.api.ProposedActionKind
import ai.nexa.cognition.planning.api.ValidatedPlan
import ai.nexa.core.permission.AuthorizationClock
import ai.nexa.core.permission.AuthorizationContext
import ai.nexa.core.permission.AuthorizationDecision
import ai.nexa.core.permission.AuthorizationStatus
import ai.nexa.core.permission.CapabilityEngine
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject

class DefaultPlanAuthorizationPort @Inject constructor(
    private val engine: CapabilityEngine,
    private val clock: AuthorizationClock,
) : PlanAuthorizationPort {
    override fun authorize(plan: ValidatedPlan, context: AuthorizationContext): PlanAuthorizationResult {
        val decisions = plan.orderedNodes.map { node -> authorizeNode(node, context) }
        val status = when {
            decisions.any { it.decision.status == AuthorizationStatus.DENY } -> PlanAuthorizationStatus.DENIED
            decisions.any { it.decision.status == AuthorizationStatus.REQUIRE_CONSENT } ->
                PlanAuthorizationStatus.CONSENT_REQUIRED
            else -> PlanAuthorizationStatus.AUTHORIZED
        }
        val consentRequests = plan.orderedNodes.zip(decisions).mapNotNull { (node, decision) ->
            if (decision.decision.status == AuthorizationStatus.REQUIRE_CONSENT) node.consentRequest() else null
        }
        val snapshot = if (status == PlanAuthorizationStatus.AUTHORIZED) {
            PlanAuthorizationSnapshot(
                planId = plan.plan.id,
                planVersion = plan.plan.version,
                planFingerprint = fingerprint(plan),
                grantIds = decisions.mapNotNull { it.decision.grantId },
                decisions = decisions,
                issuedAtEpochMillis = clock.nowEpochMillis(),
            )
        } else {
            null
        }
        return PlanAuthorizationResult(plan.plan.id, status, decisions, consentRequests, snapshot)
    }

    private fun authorizeNode(node: PlanNode, context: AuthorizationContext): NodeAuthorizationDecision {
        val capabilityDecisions = node.declaredCapabilities.map { engine.authorize(it, context) }
        val decision = capabilityDecisions.minWithOrNull(DECISION_ORDER)
            ?: AuthorizationDecision(
                AuthorizationStatus.DENY,
                ai.nexa.core.permission.AuthorizationReason.INVALID_REQUIREMENT,
                node.action.requiredCapabilities.first().id,
                decidedAtEpochMillis = clock.nowEpochMillis(),
            )
        return NodeAuthorizationDecision(node.id, decision)
    }

    private fun PlanNode.consentRequest(): ConsentRequest {
        val requirement = declaredCapabilities.single()
        return ConsentRequest(
            nodeId = id,
            actionKind = action.kind(),
            capabilityId = requirement.id.value,
            targetKind = requirement.target.kind,
            requestedScope = requirement.requestedScope,
        )
    }

    private fun fingerprint(plan: ValidatedPlan): String {
        val canonical = buildString {
            append(plan.plan.id.value).append('|').append(plan.plan.version.value)
            plan.orderedNodes.forEach { node ->
                append('|').append(node.id.value)
                append(':').append(node.dependencies.joinToString(",") { it.value })
                append(':').append(node.action)
                node.declaredCapabilities.forEach { requirement ->
                    append(':').append(requirement.id.value)
                    append(':').append(requirement.target)
                    append(':').append(requirement.requestedScope)
                }
            }
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun ActionIntent.kind(): ProposedActionKind = when (this) {
        is ActionIntent.OpenApp -> ProposedActionKind.OPEN_APP
        is ActionIntent.OpenUrl -> ProposedActionKind.OPEN_URL
        is ActionIntent.CreateReminder -> ProposedActionKind.CREATE_REMINDER
        is ActionIntent.DraftMessage -> ProposedActionKind.DRAFT_MESSAGE
    }

    private companion object {
        val DECISION_ORDER = compareBy<AuthorizationDecision> {
            when (it.status) {
                AuthorizationStatus.DENY -> 0
                AuthorizationStatus.REQUIRE_CONSENT -> 1
                AuthorizationStatus.ALLOW -> 2
            }
        }.thenBy { it.capabilityId.value }
    }
}
