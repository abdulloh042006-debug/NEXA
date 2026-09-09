package ai.nexa.core.permission

import java.net.URI

@JvmInline
value class CapabilityId(val value: String) {
    init {
        require(value.matches(ID_PATTERN)) { "invalid capability id" }
    }

    private companion object {
        val ID_PATTERN = Regex("[a-z][a-z0-9_.-]{2,63}")
    }
}

enum class CapabilityRiskClass { LOW, MODERATE, SENSITIVE }

enum class ConsentMode { GRANT_REQUIRED, EXPLICIT_EACH_TIME }

enum class RequestedGrantScope { ONCE, SESSION, TIME_BOUNDED }

enum class RuntimePermissionPrerequisite { NONE, CALENDAR_WRITE }

enum class CapabilityTargetKind { APPLICATION, WEB_ORIGIN, REMINDER_STORE, RECIPIENT_REFERENCE }

sealed interface CapabilityTarget {
    val kind: CapabilityTargetKind

    data class Application(val packageName: String) : CapabilityTarget {
        init {
            require(packageName.matches(PACKAGE_NAME)) { "invalid application package" }
        }

        override val kind = CapabilityTargetKind.APPLICATION
    }

    data class WebOrigin(val scheme: String, val host: String) : CapabilityTarget {
        init {
            require(scheme in ALLOWED_SCHEMES && host.isNotBlank()) { "invalid web origin" }
            val parsed = URI("$scheme://$host")
            require(parsed.host == host && parsed.userInfo == null) { "invalid web origin" }
        }

        override val kind = CapabilityTargetKind.WEB_ORIGIN
    }

    data object ReminderStore : CapabilityTarget {
        override val kind = CapabilityTargetKind.REMINDER_STORE
    }

    data class RecipientReference(val reference: String) : CapabilityTarget {
        init {
            require(reference.matches(REFERENCE_ID)) { "invalid recipient reference" }
        }

        override val kind = CapabilityTargetKind.RECIPIENT_REFERENCE
    }

    private companion object {
        val PACKAGE_NAME = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
        val REFERENCE_ID = Regex("[A-Za-z0-9._-]{1,64}")
        val ALLOWED_SCHEMES = setOf("http", "https")
    }
}

data class CapabilityMetadata(
    val riskClass: CapabilityRiskClass,
    val consentMode: ConsentMode,
    val runtimePrerequisite: RuntimePermissionPrerequisite,
)

/** A typed capability ask. It is data only and cannot grant authority. */
data class CapabilityRequirement(
    val capability: Capability,
    val target: CapabilityTarget = capability.defaultTarget,
    val requestedScope: RequestedGrantScope = RequestedGrantScope.ONCE,
) {
    val id: CapabilityId get() = capability.id
    val metadata: CapabilityMetadata get() = capability.metadata

    fun isValid(): Boolean = capability.targetKind == target.kind

    enum class Capability(
        val id: CapabilityId,
        val targetKind: CapabilityTargetKind,
        val metadata: CapabilityMetadata,
        internal val defaultTarget: CapabilityTarget,
    ) {
        APP_LAUNCH(
            CapabilityId("app.launch"),
            CapabilityTargetKind.APPLICATION,
            CapabilityMetadata(
                CapabilityRiskClass.LOW,
                ConsentMode.GRANT_REQUIRED,
                RuntimePermissionPrerequisite.NONE,
            ),
            CapabilityTarget.Application("ai.nexa.unknown"),
        ),
        EXTERNAL_URL_OPEN(
            CapabilityId("url.open.external"),
            CapabilityTargetKind.WEB_ORIGIN,
            CapabilityMetadata(
                CapabilityRiskClass.MODERATE,
                ConsentMode.GRANT_REQUIRED,
                RuntimePermissionPrerequisite.NONE,
            ),
            CapabilityTarget.WebOrigin("https", "invalid.local"),
        ),
        REMINDER_CREATE(
            CapabilityId("reminder.create"),
            CapabilityTargetKind.REMINDER_STORE,
            CapabilityMetadata(
                CapabilityRiskClass.SENSITIVE,
                ConsentMode.EXPLICIT_EACH_TIME,
                RuntimePermissionPrerequisite.CALENDAR_WRITE,
            ),
            CapabilityTarget.ReminderStore,
        ),
        MESSAGE_DRAFT(
            CapabilityId("message.draft"),
            CapabilityTargetKind.RECIPIENT_REFERENCE,
            CapabilityMetadata(
                CapabilityRiskClass.SENSITIVE,
                ConsentMode.EXPLICIT_EACH_TIME,
                RuntimePermissionPrerequisite.NONE,
            ),
            CapabilityTarget.RecipientReference("unknown"),
        ),
    }
}
