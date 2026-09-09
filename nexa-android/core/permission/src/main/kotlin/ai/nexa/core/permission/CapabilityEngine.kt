package ai.nexa.core.permission

class CapabilityEngine(
    private val grantStore: GrantStore,
    private val runtimePermissions: RuntimePermissionStatePort,
    private val clock: AuthorizationClock,
    private val audit: AuthorizationAuditObserver,
    private val prohibitedCapabilities: Set<CapabilityRequirement.Capability> = emptySet(),
) {
    fun authorize(
        requirement: CapabilityRequirement,
        context: AuthorizationContext,
    ): AuthorizationDecision {
        val now = clock.nowEpochMillis()
        val decision = evaluate(requirement, context, now)
        audit.record(
            AuthorizationAuditEvent(
                authorizationRequestId = context.authorizationRequestId,
                capabilityId = requirement.id,
                decision = decision.reason,
                grantId = decision.grantId,
                targetKind = requirement.target.kind,
                timestampEpochMillis = now,
            ),
        )
        return decision
    }

    private fun evaluate(
        requirement: CapabilityRequirement,
        context: AuthorizationContext,
        now: Long,
    ): AuthorizationDecision = when {
        !requirement.isValid() -> deny(requirement, AuthorizationReason.INVALID_REQUIREMENT, now)
        requirement.capability in prohibitedCapabilities -> {
            deny(requirement, AuthorizationReason.POLICY_DENIED, now)
        }
        else -> evaluateGrants(requirement, context, now)
    }

    private fun evaluateGrants(
        requirement: CapabilityRequirement,
        context: AuthorizationContext,
        now: Long,
    ): AuthorizationDecision {
        val capabilityGrants = grantStore.grantsFor(requirement.capability)
        val targeted = capabilityGrants.filter { it.target == requirement.target }
        val scoped = targeted.filter { it.scope.matches(requirement.requestedScope, context) }
        val active = scoped.filterNot { it.isRevoked(now) || it.isExpired(now) }
        return when {
            capabilityGrants.isEmpty() -> missingGrant(requirement, now)
            targeted.isEmpty() -> deny(requirement, AuthorizationReason.TARGET_MISMATCH, now)
            scoped.isEmpty() -> deny(requirement, AuthorizationReason.SCOPE_MISMATCH, now)
            active.isEmpty() -> deny(requirement, inactiveReason(scoped, now), now)
            else -> checkRuntimePrerequisite(requirement, active.minBy { it.id }, now)
        }
    }

    private fun inactiveReason(grants: List<CapabilityGrant>, now: Long): AuthorizationReason =
        if (grants.any { it.isRevoked(now) }) AuthorizationReason.REVOKED_GRANT else AuthorizationReason.EXPIRED_GRANT

    private fun checkRuntimePrerequisite(
        requirement: CapabilityRequirement,
        grant: CapabilityGrant,
        now: Long,
    ): AuthorizationDecision {
        val prerequisite = requirement.metadata.runtimePrerequisite
        if (prerequisite == RuntimePermissionPrerequisite.NONE) return allow(requirement, grant.id, now)
        return when (runtimePermissions.state(prerequisite)) {
            RuntimePermissionState.GRANTED -> allow(requirement, grant.id, now)
            RuntimePermissionState.DENIED ->
                deny(requirement, AuthorizationReason.ANDROID_PERMISSION_MISSING, now)
            RuntimePermissionState.UNAVAILABLE ->
                deny(requirement, AuthorizationReason.RUNTIME_CONTEXT_UNAVAILABLE, now)
        }
    }

    private fun missingGrant(requirement: CapabilityRequirement, now: Long): AuthorizationDecision =
        if (requirement.metadata.consentMode == ConsentMode.EXPLICIT_EACH_TIME) {
            AuthorizationDecision(
                AuthorizationStatus.REQUIRE_CONSENT,
                AuthorizationReason.CONSENT_REQUIRED,
                requirement.id,
                decidedAtEpochMillis = now,
            )
        } else {
            deny(requirement, AuthorizationReason.NO_MATCHING_GRANT, now)
        }

    private fun allow(requirement: CapabilityRequirement, grantId: GrantId, now: Long) =
        AuthorizationDecision(
            AuthorizationStatus.ALLOW,
            AuthorizationReason.GRANTED,
            requirement.id,
            grantId,
            now,
        )

    private fun deny(requirement: CapabilityRequirement, reason: AuthorizationReason, now: Long) =
        AuthorizationDecision(
            AuthorizationStatus.DENY,
            reason,
            requirement.id,
            decidedAtEpochMillis = now,
        )

    private fun GrantScope.matches(
        requested: RequestedGrantScope,
        context: AuthorizationContext,
    ): Boolean = when (this) {
        is GrantScope.Once ->
            requested == RequestedGrantScope.ONCE && authorizationRequestId == context.authorizationRequestId
        is GrantScope.Session ->
            requested != RequestedGrantScope.TIME_BOUNDED && sessionId == context.sessionId
        GrantScope.TimeBounded -> true
    }
}
