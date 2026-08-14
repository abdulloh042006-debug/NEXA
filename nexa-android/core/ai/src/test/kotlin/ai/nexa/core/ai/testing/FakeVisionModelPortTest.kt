package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.model.VisionRequest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeVisionModelPortTest {

    private val request = VisionRequest(
        images = listOf(VisionRequest.ImageInput(byteArrayOf(1, 2, 3), "image/jpeg")),
        prompt = "What is on this receipt?",
        privacyClass = PrivacyClass.P2_SENSITIVE,
        latencyBudget = LatencyBudget.INTERACTIVE,
    )

    @Test
    fun `describe streams the scripted deltas and records the request`() = runTest {
        val port = FakeVisionModelPort()

        val deltas = port.describe(request).toList()

        assertEquals(
            listOf(
                ChatDelta.Token("A description of 1 image(s)."),
                ChatDelta.Usage(inputTokens = 1, outputTokens = 1),
            ),
            deltas,
        )
        assertEquals(listOf(request), port.recordedRequests)
    }
}
