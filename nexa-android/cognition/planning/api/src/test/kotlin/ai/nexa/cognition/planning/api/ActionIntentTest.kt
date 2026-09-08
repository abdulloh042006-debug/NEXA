package ai.nexa.cognition.planning.api

import ai.nexa.core.permission.CapabilityRequirement
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActionIntentTest {
    @Test
    fun `each supported intent declares its fixed capability`() {
        val intents = listOf(
            ActionIntent.OpenApp("ai.nexa.app"),
            ActionIntent.OpenUrl("https://nexa.app/help"),
            ActionIntent.CreateReminder("Call Aziz", 1L),
            ActionIntent.DraftMessage("contact-1", "Draft only"),
        )

        assertEquals(
            listOf(
                CapabilityRequirement.Capability.APP_LAUNCH,
                CapabilityRequirement.Capability.EXTERNAL_URL_OPEN,
                CapabilityRequirement.Capability.REMINDER_CREATE,
                CapabilityRequirement.Capability.MESSAGE_DRAFT,
            ),
            intents.map { it.requiredCapabilities.single().capability },
        )
    }

    @Test
    fun `malformed privileged targets fail closed`() {
        assertFailsWith<IllegalArgumentException> { ActionIntent.OpenApp("not a package") }
        assertFailsWith<IllegalArgumentException> { ActionIntent.OpenUrl("javascript:alert(1)") }
        assertFailsWith<IllegalArgumentException> { ActionIntent.OpenUrl("https://user:secret@nexa.app") }
        assertFailsWith<IllegalArgumentException> { ActionIntent.DraftMessage("", "body") }
    }
}
