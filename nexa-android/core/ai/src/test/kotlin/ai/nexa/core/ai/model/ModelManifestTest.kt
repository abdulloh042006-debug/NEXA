package ai.nexa.core.ai.model

import ai.nexa.core.ai.testing.FakeManifests
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelManifestTest {

    @Test
    fun `mayReceive admits requests up to the privacy floor and rejects above it`() {
        val cloudScrubbed = FakeManifests.chat(privacyFloor = PrivacyClass.P1_PERSONAL)
            .copy(kind = ModelManifest.ModelKind.CLOUD, localSpec = null)

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
}
