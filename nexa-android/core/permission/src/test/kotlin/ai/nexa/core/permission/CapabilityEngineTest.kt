package ai.nexa.core.permission

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CapabilityEngineTest {
    private var now = 100L
    private val store = InMemoryGrantStore()
    private var runtimeState = RuntimePermissionState.GRANTED
    private val audits = mutableListOf<AuthorizationAuditEvent>()
    private val engine = CapabilityEngine(
        store,
        RuntimePermissionStatePort { runtimeState },
        AuthorizationClock { now },
        AuthorizationAuditObserver(audits::add),
    )
    private val context = AuthorizationContext("request-1", "session-1")
    private val app = CapabilityRequirement(
        CapabilityRequirement.Capability.APP_LAUNCH,
        CapabilityTarget.Application("ai.nexa.app"),
    )

    @Test
    fun `default deny and explicit consent states are deterministic`() {
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.NO_MATCHING_GRANT)
        val reminder = CapabilityRequirement(
            CapabilityRequirement.Capability.REMINDER_CREATE,
            CapabilityTarget.ReminderStore,
        )
        assertDecision(reminder, AuthorizationStatus.REQUIRE_CONSENT, AuthorizationReason.CONSENT_REQUIRED)
    }

    @Test
    fun `matching active grant allows and duplicate grants select lowest id`() {
        store.put(grant("z-grant"))
        store.put(grant("a-grant"))

        val decision = engine.authorize(app, context)

        assertEquals(AuthorizationStatus.ALLOW, decision.status)
        assertEquals(GrantId("a-grant"), decision.grantId)
    }

    @Test
    fun `wrong target and wrong scope deny`() {
        store.put(grant("target", target = CapabilityTarget.Application("ai.other.app")))
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.TARGET_MISMATCH)

        store.put(grant("scope", scope = GrantScope.Once("another-request")))
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.SCOPE_MISMATCH)
    }

    @Test
    fun `expired and revoked grants deny including exact expiry boundary`() {
        store.put(grant("expired", expiresAt = now))
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.EXPIRED_GRANT)

        val active = grant("revoked")
        store.put(active)
        assertTrue(store.revoke(active.id, now))
        assertFalse(store.revoke(active.id, now + 1))
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.REVOKED_GRANT)
    }

    @Test
    fun `clock rollback and broad grant cannot bypass explicit each-time consent`() {
        store.put(grant("future", issuedAt = now + 1))
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.GRANT_NOT_YET_ACTIVE)

        val reminder = reminder()
        store.put(reminderGrant(scope = GrantScope.Session(context.sessionId!!)))
        assertDecision(reminder, AuthorizationStatus.DENY, AuthorizationReason.SCOPE_MISMATCH)
    }

    @Test
    fun `Android permission never replaces a NEXA grant`() {
        runtimeState = RuntimePermissionState.GRANTED
        val reminder = reminder()
        assertDecision(reminder, AuthorizationStatus.REQUIRE_CONSENT, AuthorizationReason.CONSENT_REQUIRED)

        store.put(reminderGrant())
        runtimeState = RuntimePermissionState.DENIED
        assertDecision(reminder, AuthorizationStatus.DENY, AuthorizationReason.ANDROID_PERMISSION_MISSING)
        runtimeState = RuntimePermissionState.UNAVAILABLE
        assertDecision(reminder, AuthorizationStatus.DENY, AuthorizationReason.RUNTIME_CONTEXT_UNAVAILABLE)
    }

    @Test
    fun `revocation immediately invalidates future evaluation`() {
        val grant = grant("once")
        store.put(grant)
        assertEquals(AuthorizationStatus.ALLOW, engine.authorize(app, context).status)

        store.revoke(grant.id, now)

        assertEquals(AuthorizationStatus.DENY, engine.authorize(app, context).status)
    }

    @Test
    fun `invalid target relationship and another capability fail closed`() {
        val malformed = app.copy(target = CapabilityTarget.RecipientReference("contact-1"))
        assertDecision(malformed, AuthorizationStatus.DENY, AuthorizationReason.INVALID_REQUIREMENT)
        store.put(reminderGrant())
        assertDecision(app, AuthorizationStatus.DENY, AuthorizationReason.NO_MATCHING_GRANT)
    }

    @Test
    fun `audit record contains categories and ids but no target payload`() {
        val secretTarget = "ai.private.secret"
        val requirement = app.copy(target = CapabilityTarget.Application(secretTarget))

        engine.authorize(requirement, context)

        val audit = audits.last()
        assertEquals(CapabilityTargetKind.APPLICATION, audit.targetKind)
        assertFalse(audit.toString().contains(secretTarget))
    }

    private fun assertDecision(
        requirement: CapabilityRequirement,
        status: AuthorizationStatus,
        reason: AuthorizationReason,
    ) {
        val decision = engine.authorize(requirement, context)
        assertEquals(status, decision.status)
        assertEquals(reason, decision.reason)
    }

    private fun grant(
        id: String,
        target: CapabilityTarget = app.target,
        scope: GrantScope = GrantScope.Once(context.authorizationRequestId),
        issuedAt: Long = 1,
        expiresAt: Long = 1_000,
    ) = CapabilityGrant(
        GrantId(id),
        CapabilityRequirement.Capability.APP_LAUNCH,
        target,
        scope,
        issuedAtEpochMillis = issuedAt,
        expiresAtEpochMillis = expiresAt,
        consent = ConsentProvenance(GrantSource.USER_CONSENT, 1),
    )

    private fun reminder() = CapabilityRequirement(
        CapabilityRequirement.Capability.REMINDER_CREATE,
        CapabilityTarget.ReminderStore,
    )

    private fun reminderGrant(
        scope: GrantScope = GrantScope.Once(context.authorizationRequestId),
    ) = CapabilityGrant(
        GrantId("reminder-grant"),
        CapabilityRequirement.Capability.REMINDER_CREATE,
        CapabilityTarget.ReminderStore,
        scope,
        issuedAtEpochMillis = 1,
        expiresAtEpochMillis = 1_000,
        consent = ConsentProvenance(GrantSource.USER_CONSENT, 1),
    )
}
