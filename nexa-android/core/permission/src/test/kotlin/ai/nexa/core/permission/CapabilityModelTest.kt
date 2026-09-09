package ai.nexa.core.permission

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CapabilityModelTest {
    @Test
    fun `capability ids and targets are closed and validated`() {
        assertFailsWith<IllegalArgumentException> { CapabilityId("SHELL") }
        assertFailsWith<IllegalArgumentException> { CapabilityTarget.Application("not-a-package") }
        assertFailsWith<IllegalArgumentException> { CapabilityTarget.WebOrigin("javascript", "example.com") }
        assertEquals("app.launch", CapabilityRequirement.Capability.APP_LAUNCH.id.value)
    }

    @Test
    fun `requirement validates capability target relationship`() {
        val valid = CapabilityRequirement(
            CapabilityRequirement.Capability.APP_LAUNCH,
            CapabilityTarget.Application("ai.nexa.app"),
        )
        val invalid = valid.copy(target = CapabilityTarget.RecipientReference("contact-1"))

        assertTrue(valid.isValid())
        assertFalse(invalid.isValid())
    }

    @Test
    fun `authorization decisions are explicit typed states`() {
        val decision = AuthorizationDecision(
            AuthorizationStatus.DENY,
            AuthorizationReason.NO_MATCHING_GRANT,
            CapabilityRequirement.Capability.APP_LAUNCH.id,
            decidedAtEpochMillis = 1,
        )

        assertEquals(AuthorizationStatus.DENY, decision.status)
        assertEquals(AuthorizationReason.NO_MATCHING_GRANT, decision.reason)
    }
}
