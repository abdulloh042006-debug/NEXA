package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.CapabilityRequirement
import ai.nexa.core.permission.CapabilityTarget
import java.net.URI

/** Closed data-only proposal. No member can execute an Android or system action. */
sealed interface ActionIntent {
    val requiredCapabilities: Set<CapabilityRequirement>

    data class OpenApp(val packageName: String) : ActionIntent {
        init {
            require(packageName.matches(PACKAGE_NAME)) { "invalid application package name" }
        }

        override val requiredCapabilities = capability(
            CapabilityRequirement.Capability.APP_LAUNCH,
            CapabilityTarget.Application(packageName),
        )
    }

    data class OpenUrl(val url: String) : ActionIntent {
        init {
            val target = runCatching { URI(url) }.getOrNull()
            require(
                target != null &&
                    target.scheme in ALLOWED_URL_SCHEMES &&
                    !target.host.isNullOrBlank() &&
                    target.userInfo == null,
            ) { "URL must be an absolute http(s) target without user information" }
        }

        override val requiredCapabilities = capability(
            CapabilityRequirement.Capability.EXTERNAL_URL_OPEN,
            URI(url).let { CapabilityTarget.WebOrigin(it.scheme.lowercase(), it.host.lowercase()) },
        )
    }

    data class CreateReminder(
        val title: String,
        val dueAtEpochMillis: Long,
    ) : ActionIntent {
        init {
            require(title.isNotBlank() && title.length <= MAX_TEXT_LENGTH) { "invalid reminder title" }
            require(dueAtEpochMillis > 0) { "reminder due time must be positive" }
        }

        override val requiredCapabilities = capability(
            CapabilityRequirement.Capability.REMINDER_CREATE,
            CapabilityTarget.ReminderStore,
        )
    }

    data class DraftMessage(
        val recipientReference: String,
        val body: String,
    ) : ActionIntent {
        init {
            require(recipientReference.matches(REFERENCE_ID)) { "invalid recipient reference" }
            require(body.isNotBlank() && body.length <= MAX_TEXT_LENGTH) { "invalid message body" }
        }

        override val requiredCapabilities = capability(
            CapabilityRequirement.Capability.MESSAGE_DRAFT,
            CapabilityTarget.RecipientReference(recipientReference),
        )
    }

    private companion object {
        val PACKAGE_NAME = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
        val REFERENCE_ID = Regex("[A-Za-z0-9._-]{1,64}")
        val ALLOWED_URL_SCHEMES = setOf("http", "https")
        const val MAX_TEXT_LENGTH = 4_096

        fun capability(
            value: CapabilityRequirement.Capability,
            target: CapabilityTarget,
        ) = setOf(CapabilityRequirement(value, target))
    }
}
