package ai.nexa.core.permission

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GrantStoreTest {
    @Test
    fun `reads are deterministic and revocation is immutable and idempotent`() {
        val store = InMemoryGrantStore()
        store.put(grant("z"))
        store.put(grant("a"))

        assertEquals(listOf("a", "z"), store.grantsFor(CAPABILITY).map { it.id.value })
        assertTrue(store.revoke(GrantId("a"), 50))
        assertFalse(store.revoke(GrantId("a"), 60))
        assertEquals(50, store.grantsFor(CAPABILITY).first().revokedAtEpochMillis)
    }

    private fun grant(id: String) = CapabilityGrant(
        GrantId(id),
        CAPABILITY,
        CapabilityTarget.Application("ai.nexa.app"),
        GrantScope.TimeBounded,
        issuedAtEpochMillis = 1,
        expiresAtEpochMillis = 100,
        consent = ConsentProvenance(GrantSource.USER_CONSENT, 1),
    )

    private companion object {
        val CAPABILITY = CapabilityRequirement.Capability.APP_LAUNCH
    }
}
