package ai.nexa.core.permission

@JvmInline
value class GrantId(val value: String) : Comparable<GrantId> {
    init {
        require(value.matches(ID_PATTERN)) { "invalid grant id" }
    }

    override fun compareTo(other: GrantId): Int = value.compareTo(other.value)

    private companion object {
        val ID_PATTERN = Regex("[A-Za-z0-9._-]{1,64}")
    }
}

sealed interface GrantScope {
    data class Once(val authorizationRequestId: String) : GrantScope {
        init {
            require(authorizationRequestId.matches(REFERENCE_ID)) { "invalid authorization request id" }
        }
    }

    data class Session(val sessionId: String) : GrantScope {
        init {
            require(sessionId.matches(REFERENCE_ID)) { "invalid session id" }
        }
    }

    data object TimeBounded : GrantScope

    private companion object {
        val REFERENCE_ID = Regex("[A-Za-z0-9._-]{1,64}")
    }
}

enum class GrantSource { USER_CONSENT, ADMIN_POLICY }

data class ConsentProvenance(
    val source: GrantSource,
    val recordedAtEpochMillis: Long,
) {
    init {
        require(recordedAtEpochMillis >= 0) { "invalid consent timestamp" }
    }
}

data class CapabilityGrant(
    val id: GrantId,
    val capability: CapabilityRequirement.Capability,
    val target: CapabilityTarget,
    val scope: GrantScope,
    val issuedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val consent: ConsentProvenance,
    val revokedAtEpochMillis: Long? = null,
) {
    init {
        require(capability.targetKind == target.kind) { "grant target does not match capability" }
        require(issuedAtEpochMillis >= 0 && expiresAtEpochMillis > issuedAtEpochMillis) { "invalid grant lifetime" }
        require(revokedAtEpochMillis == null || revokedAtEpochMillis >= issuedAtEpochMillis) {
            "invalid revocation timestamp"
        }
    }

    fun isExpired(atEpochMillis: Long): Boolean = atEpochMillis >= expiresAtEpochMillis
    fun isRevoked(atEpochMillis: Long): Boolean = revokedAtEpochMillis?.let { it <= atEpochMillis } == true
}

data class AuthorizationContext(
    val authorizationRequestId: String,
    val sessionId: String?,
) {
    init {
        require(authorizationRequestId.matches(REFERENCE_ID)) { "invalid authorization request id" }
        require(sessionId == null || sessionId.matches(REFERENCE_ID)) { "invalid session id" }
    }

    private companion object {
        val REFERENCE_ID = Regex("[A-Za-z0-9._-]{1,64}")
    }
}
