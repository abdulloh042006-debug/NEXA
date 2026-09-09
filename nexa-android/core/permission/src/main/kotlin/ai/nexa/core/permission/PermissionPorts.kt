package ai.nexa.core.permission

fun interface AuthorizationClock {
    fun nowEpochMillis(): Long
}

interface GrantStore {
    fun grantsFor(capability: CapabilityRequirement.Capability): List<CapabilityGrant>
    fun put(grant: CapabilityGrant)
    fun revoke(id: GrantId, revokedAtEpochMillis: Long): Boolean
}

enum class RuntimePermissionState { GRANTED, DENIED, UNAVAILABLE }

fun interface RuntimePermissionStatePort {
    fun state(prerequisite: RuntimePermissionPrerequisite): RuntimePermissionState
}

data class AuthorizationAuditEvent(
    val authorizationRequestId: String,
    val capabilityId: CapabilityId,
    val decision: AuthorizationReason,
    val grantId: GrantId?,
    val targetKind: CapabilityTargetKind,
    val timestampEpochMillis: Long,
)

fun interface AuthorizationAuditObserver {
    fun record(event: AuthorizationAuditEvent)
}
