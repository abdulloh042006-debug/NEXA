package ai.nexa.core.permission

enum class AuthorizationStatus { ALLOW, DENY, REQUIRE_CONSENT }

enum class AuthorizationReason {
    GRANTED,
    INVALID_REQUIREMENT,
    NO_MATCHING_GRANT,
    EXPIRED_GRANT,
    REVOKED_GRANT,
    SCOPE_MISMATCH,
    TARGET_MISMATCH,
    CONSENT_REQUIRED,
    POLICY_DENIED,
    ANDROID_PERMISSION_MISSING,
    RUNTIME_CONTEXT_UNAVAILABLE,
}

data class AuthorizationDecision(
    val status: AuthorizationStatus,
    val reason: AuthorizationReason,
    val capabilityId: CapabilityId,
    val grantId: GrantId? = null,
    val decidedAtEpochMillis: Long,
)
