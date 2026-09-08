package ai.nexa.core.ai.model

import ai.nexa.core.ai.testing.FakeManifests
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelManifestTest {

    @Test
    fun `mayReceive admits requests up to the privacy floor and rejects above it`() {
        val cloudScrubbed = FakeManifests.cloudChat(privacyFloor = PrivacyClass.P1_PERSONAL)

        assertTrue(cloudScrubbed.mayReceive(PrivacyClass.P0_PUBLIC))
        assertTrue(cloudScrubbed.mayReceive(PrivacyClass.P1_PERSONAL))
        assertFalse(cloudScrubbed.mayReceive(PrivacyClass.P2_SENSITIVE))
    }

    @Test
    fun `local models are cleared for P2`() {
        assertTrue(FakeManifests.chat().mayReceive(PrivacyClass.P2_SENSITIVE))
    }

    @Test
    fun `a LOCAL manifest without a localSpec is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(localSpec = null)
        }
    }

    @Test
    fun `a CLOUD manifest with a localSpec is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(kind = ModelManifest.ModelKind.CLOUD)
        }
    }

    @Test
    fun `versioned identity and provider are stable configuration`() {
        val manifest = FakeManifests.chat(id = "qwen2.5-3b@r2")

        assertEquals("qwen2.5-3b@r2", manifest.id)
        assertEquals(ModelProviderId("test-local"), manifest.providerId)
        assertEquals(ModelManifest.Cost.ZERO, ModelManifest.Cost.ZERO)
    }

    @Test
    fun `manifest rejects an unversioned id`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(id = "unversioned")
        }
    }

    @Test
    fun `manifest rejects empty capabilities and tools without chat`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(capabilities = emptySet())
        }
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(capabilities = setOf(ModelManifest.ModelCapability.TOOLS))
        }
    }

    @Test
    fun `cloud cannot claim sensitive clearance`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.cloudChat().copy(privacyFloor = PrivacyClass.P2_SENSITIVE)
        }
    }

    @Test
    fun `on-device model cannot silently lower its privacy clearance`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(privacyFloor = PrivacyClass.P1_PERSONAL)
        }
    }

    @Test
    fun `manifest rejects invalid scores rate and costs`() {
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(languageScores = mapOf(Language.EN to Double.NaN))
        }
        assertThrows(IllegalArgumentException::class.java) {
            FakeManifests.chat().copy(maxRpmPerUser = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ModelManifest.Cost(Double.POSITIVE_INFINITY, 0.0)
        }
    }

    @Test
    fun `local spec rejects insecure credentials malformed hash and invalid resources`() {
        val valid = requireNotNull(FakeManifests.chat().localSpec)

        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(artifactUrl = "http://cdn.test/model.gguf")
        }
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(artifactUrl = "https://token@cdn.test/model.gguf")
        }
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(sha256 = "abc")
        }
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(minRamMb = 0)
        }
    }
}
