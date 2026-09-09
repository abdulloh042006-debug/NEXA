package ai.nexa.core.permission

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryGrantStore @Inject constructor() : GrantStore {
    private val grants = ConcurrentHashMap<GrantId, CapabilityGrant>()

    override fun grantsFor(capability: CapabilityRequirement.Capability): List<CapabilityGrant> =
        grants.values.filter { it.capability == capability }.sortedBy { it.id }

    override fun put(grant: CapabilityGrant) {
        grants[grant.id] = grant
    }

    override fun revoke(id: GrantId, revokedAtEpochMillis: Long): Boolean {
        var changed = false
        grants.computeIfPresent(id) { _, current ->
            if (current.revokedAtEpochMillis == null) {
                changed = true
                current.copy(revokedAtEpochMillis = revokedAtEpochMillis)
            } else {
                current
            }
        }
        return changed
    }
}
