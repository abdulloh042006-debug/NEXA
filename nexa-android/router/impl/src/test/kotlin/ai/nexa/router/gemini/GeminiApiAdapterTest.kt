package ai.nexa.router.gemini

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.Language
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.ModelProviderId
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.model.SamplingParams
import ai.nexa.core.network.inference.GeminiGatewayClient
import ai.nexa.core.network.inference.GeminiGatewayEvent
import ai.nexa.core.network.inference.GeminiGatewayRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeminiApiAdapterTest {
    @Test
    fun mapsVendorNeutralRequestAndStream() = runTest {
        val client = RecordingClient(
            flowOf(GeminiGatewayEvent.Token("Javob"), GeminiGatewayEvent.Usage(4, 2)),
        )
        val adapter = GeminiApiAdapter(manifest(), client)

        val deltas = adapter.streamChat(request()).toList()

        assertEquals(listOf(ChatDelta.Token("Javob"), ChatDelta.Usage(4, 2)), deltas)
        assertEquals(10_000L, client.timeoutMillis)
        assertEquals("uz", client.request?.language)
        assertEquals("user", client.request?.messages?.single()?.role)
        assertEquals("P1_PERSONAL", client.request?.privacyClass)
        assertEquals(0.3, client.request?.temperature)
    }

    @Test
    fun blocksRequestsAboveManifestPrivacyClearance() {
        val adapter = GeminiApiAdapter(manifest(PrivacyClass.P0_PUBLIC), RecordingClient(flowOf()))

        assertFailsWith<IllegalArgumentException> {
            adapter.streamChat(request(privacyClass = PrivacyClass.P1_PERSONAL))
        }
    }

    @Test
    fun rejectsToolsUntilValidatedToolMappingExists() {
        val adapter = GeminiApiAdapter(manifest(), RecordingClient(flowOf()))
        val request = request().copy(
            toolSchemas = listOf(ai.nexa.core.ai.model.ToolSchema("x", "x", "{}")),
        )

        assertFailsWith<IllegalArgumentException> { adapter.streamChat(request) }
    }

    private fun request(privacyClass: PrivacyClass = PrivacyClass.P1_PERSONAL) = ChatRequest(
        messages = listOf(ChatMessage(ChatMessage.Role.USER, "Salom")),
        privacyClass = privacyClass,
        latencyBudget = LatencyBudget.INTERACTIVE,
        sampling = SamplingParams(temperature = 0.3, maxOutputTokens = 64),
        languageHint = Language.UZ,
    )

    private fun manifest(privacyFloor: PrivacyClass = PrivacyClass.P1_PERSONAL) = ModelManifest(
        id = "gemini@test",
        providerId = ModelProviderId("google"),
        kind = ModelManifest.ModelKind.CLOUD,
        capabilities = setOf(ModelManifest.ModelCapability.CHAT),
        contextWindow = 1_000,
        qualityTier = ModelManifest.QualityTier.FAST,
        cost = ModelManifest.Cost(0.0, 0.0),
        latencyP50Ms = 100,
        languageScores = Language.entries.associateWith { 1.0 },
        privacyFloor = privacyFloor,
        maxRpmPerUser = 10,
    )

    private class RecordingClient(
        private val events: Flow<GeminiGatewayEvent>,
    ) : GeminiGatewayClient {
        var request: GeminiGatewayRequest? = null
        var timeoutMillis: Long? = null

        override fun stream(
            request: GeminiGatewayRequest,
            timeoutMillis: Long,
        ): Flow<GeminiGatewayEvent> {
            this.request = request
            this.timeoutMillis = timeoutMillis
            return events
        }
    }
}
