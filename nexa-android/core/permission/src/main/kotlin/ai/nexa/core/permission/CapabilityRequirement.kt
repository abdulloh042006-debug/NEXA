package ai.nexa.core.permission

/** A capability ask carried as data. Phase 5 is solely responsible for authorization. */
data class CapabilityRequirement(
    val capability: Capability,
) {
    enum class Capability {
        APP_LAUNCH,
        EXTERNAL_URL_OPEN,
        REMINDER_CREATE,
        MESSAGE_DRAFT,
    }
}
