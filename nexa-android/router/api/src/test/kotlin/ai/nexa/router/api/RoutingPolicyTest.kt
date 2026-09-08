package ai.nexa.router.api

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RoutingPolicyTest {
    @Test
    fun `balanced policy is the stable default`() {
        val policy = RoutingPolicy()

        assertEquals(false, policy.offlineOnly)
        assertEquals(true, policy.cloudAllowed)
        assertEquals(0.5, policy.minimumLanguageScore)
        assertEquals(RoutingWeights.BALANCED, policy.weights)
    }

    @Test
    fun `invalid thresholds costs and empty weights are rejected`() {
        assertFailsWith<IllegalArgumentException> { RoutingPolicy(minimumLanguageScore = 1.1) }
        assertFailsWith<IllegalArgumentException> { RoutingPolicy(maximumAverageCostPerMtok = -1.0) }
        assertFailsWith<IllegalArgumentException> { RoutingWeights(0.0, 0.0, 0.0) }
    }
}
