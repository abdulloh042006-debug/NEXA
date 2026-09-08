package ai.nexa.router

import ai.nexa.core.ai.model.ChatMessage
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.EmbeddingPurpose
import ai.nexa.core.ai.model.Language
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.ModelProviderId
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.model.ToolSchema
import ai.nexa.core.ai.testing.FakeChatModelPort
import ai.nexa.core.ai.testing.FakeEmbeddingPort
import ai.nexa.core.ai.testing.FakeManifests
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.EmbeddingRouteRequest
import ai.nexa.router.api.ExclusionReason
import ai.nexa.router.api.NetworkState
import ai.nexa.router.api.RoutingDecisionRecord
import ai.nexa.router.api.RoutingDeviceState
import ai.nexa.router.api.RoutingPolicy
import ai.nexa.router.api.RoutingWeights
import ai.nexa.router.api.SelectionReason
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeterministicModelRouterTest {
    @Test
    fun `only eligible model is selected`() = runTest {
        val local = FakeChatModelPort(manifest = local())
        val cloud = FakeChatModelPort(manifest = cloud())

        val decision = router(local, cloud).resolveChat(route(privacy = PrivacyClass.P2_SENSITIVE))

        assertEquals(local.manifest.id, decision.chosen?.id)
        assertEquals(setOf(SelectionReason.ONLY_ELIGIBLE_MODEL), decision.record.selectionReasons)
        assertEquals(
            listOf(ExclusionReason.PRIVACY_FLOOR_EXCEEDED),
            decision.excluded.getValue(cloud.manifest.id),
        )
    }

    @Test
    fun `quality ranks multiple eligible models`() = runTest {
        val local = FakeChatModelPort(manifest = local())
        val cloud = FakeChatModelPort(manifest = cloud())

        val decision = router(local, cloud).resolveChat(route())

        assertEquals(listOf(cloud.manifest.id, local.manifest.id), decision.ranked.map { it.id })
        assertEquals(setOf(SelectionReason.HIGHEST_POLICY_SCORE), decision.record.selectionReasons)
    }

    @Test
    fun `missing required tool capability is a hard rejection`() = runTest {
        val model = FakeChatModelPort(
            manifest = local(capabilities = setOf(ModelManifest.ModelCapability.CHAT)),
        )
        val request = route().copy(
            request = chatRequest().copy(toolSchemas = listOf(ToolSchema("search", "search", "{}"))),
        )

        val decision = router(model).resolveChat(request)

        assertEquals(listOf(ExclusionReason.TOOLS_UNSUPPORTED), decision.excluded.getValue(model.manifest.id))
    }

    @Test
    fun `network unavailable rejects cloud`() = runTest {
        val cloud = FakeChatModelPort(manifest = cloud())

        val decision = router(cloud).resolveChat(
            route(device = device(network = NetworkState.UNAVAILABLE)),
        )

        assertTrue(ExclusionReason.NETWORK_UNAVAILABLE in decision.excluded.getValue(cloud.manifest.id))
    }

    @Test
    fun `privacy policy cannot be outweighed by cloud quality`() = runTest {
        val cloud = FakeChatModelPort(
            manifest = cloud().copy(qualityTier = ModelManifest.QualityTier.FRONTIER),
        )

        val decision = router(cloud).resolveChat(
            route(
                privacy = PrivacyClass.P2_SENSITIVE,
                policy = RoutingPolicy(weights = RoutingWeights.QUALITY_FIRST),
            ),
        )

        assertNull(decision.chosen)
        assertTrue(ExclusionReason.PRIVACY_FLOOR_EXCEEDED in decision.excluded.getValue(cloud.manifest.id))
    }

    @Test
    fun `offline only and cloud forbidden are explicit hard reasons`() = runTest {
        val cloud = FakeChatModelPort(manifest = cloud())

        val decision = router(cloud).resolveChat(
            route(policy = RoutingPolicy(offlineOnly = true, cloudAllowed = false)),
        )

        assertEquals(
            listOf(ExclusionReason.OFFLINE_ONLY, ExclusionReason.CLOUD_FORBIDDEN),
            decision.excluded.getValue(cloud.manifest.id),
        )
    }

    @Test
    fun `unavailable local model and insufficient RAM are explicit`() = runTest {
        val local = FakeChatModelPort(manifest = local())

        val unavailable = router(local).resolveChat(
            route(device = device(available = emptySet())),
        )
        val lowRam = router(local).resolveChat(
            route(device = device(ramMb = 1_024)),
        )

        assertTrue(ExclusionReason.LOCAL_MODEL_UNAVAILABLE in unavailable.excluded.getValue(local.manifest.id))
        assertTrue(ExclusionReason.INSUFFICIENT_RAM in lowRam.excluded.getValue(local.manifest.id))
    }

    @Test
    fun `cost sensitive policy prefers the free local model`() = runTest {
        val local = FakeChatModelPort(manifest = local())
        val cloud = FakeChatModelPort(manifest = cloud())

        val decision = router(local, cloud).resolveChat(
            route(policy = RoutingPolicy(weights = RoutingWeights.COST_SENSITIVE)),
        )

        assertEquals(local.manifest.id, decision.chosen?.id)
    }

    @Test
    fun `latency sensitive policy prefers the fast model`() = runTest {
        val fast = FakeChatModelPort(manifest = local(id = "a-fast@v1", latencyMs = 20))
        val slow = FakeChatModelPort(
            manifest = local(id = "z-slow@v1", latencyMs = 9_000)
                .copy(qualityTier = ModelManifest.QualityTier.STRONG),
        )

        val decision = router(fast, slow).resolveChat(
            route(policy = RoutingPolicy(weights = RoutingWeights.LATENCY_SENSITIVE)),
        )

        assertEquals(fast.manifest.id, decision.chosen?.id)
    }

    @Test
    fun `ties are broken by canonical model id`() = runTest {
        val first = FakeChatModelPort(manifest = local(id = "a-model@v1"))
        val second = FakeChatModelPort(manifest = local(id = "b-model@v1"))

        val decision = router(second, first).resolveChat(route())

        assertEquals(first.manifest.id, decision.chosen?.id)
        assertTrue(SelectionReason.DETERMINISTIC_ID_TIE_BREAK in decision.record.selectionReasons)
    }

    @Test
    fun `empty registry and no eligible model are represented without fallback`() = runTest {
        val emptyDecision = router().resolveChat(route())
        val unavailable = FakeChatModelPort(manifest = local())
        val unavailableDecision = router(unavailable).resolveChat(
            route(device = device(available = emptySet())),
        )

        assertNull(emptyDecision.chosen)
        assertEquals(setOf(SelectionReason.NO_ELIGIBLE_MODEL), emptyDecision.record.selectionReasons)
        assertNull(unavailableDecision.chosen)
    }

    @Test
    fun `explicit model and provider restrictions are hard filters`() = runTest {
        val allowed = FakeChatModelPort(manifest = local(id = "allowed@v1"))
        val blocked = FakeChatModelPort(manifest = local(id = "blocked@v1"))
        val modelDecision = router(allowed, blocked).resolveChat(
            route(policy = RoutingPolicy(allowedModelIds = setOf(allowed.manifest.id))),
        )
        val providerDecision = router(allowed).resolveChat(
            route(
                policy = RoutingPolicy(
                    allowedProviderIds = setOf(ModelProviderId("different-provider")),
                ),
            ),
        )

        assertEquals(allowed.manifest.id, modelDecision.chosen?.id)
        assertTrue(ExclusionReason.MODEL_RESTRICTED in modelDecision.excluded.getValue(blocked.manifest.id))
        assertTrue(ExclusionReason.PROVIDER_RESTRICTED in providerDecision.excluded.getValue(allowed.manifest.id))
    }

    @Test
    fun `context and language thresholds reject incompatible models`() = runTest {
        val model = FakeChatModelPort(
            manifest = local(contextWindow = 1_000).copy(
                languageScores = mapOf(Language.EN to 0.2),
            ),
        )

        val decision = router(model).resolveChat(
            route(estimatedTokens = 1_000, language = Language.EN),
        )

        assertEquals(
            listOf(ExclusionReason.CONTEXT_OVERFLOW, ExclusionReason.LANGUAGE_BELOW_THRESHOLD),
            decision.excluded.getValue(model.manifest.id),
        )
    }

    @Test
    fun `cost ceiling is hard while score weights are soft`() = runTest {
        val cloud = FakeChatModelPort(manifest = cloud())

        val decision = router(cloud).resolveChat(
            route(
                policy = RoutingPolicy(
                    maximumAverageCostPerMtok = 1.0,
                    weights = RoutingWeights.QUALITY_FIRST,
                ),
            ),
        )

        assertTrue(ExclusionReason.COST_LIMIT_EXCEEDED in decision.excluded.getValue(cloud.manifest.id))
    }

    @Test
    fun `decision record contains identifiers and codes but no request content`() = runTest {
        var observed: RoutingDecisionRecord? = null
        val local = FakeChatModelPort(manifest = local())
        val request = route().copy(
            request = chatRequest(content = "private conversation text"),
        )
        val router = DeterministicModelRouter(listOf(local), observer = { observed = it })

        router.resolveChat(request)

        val serializedShape = observed.toString()
        assertFalse(serializedShape.contains("private conversation text"))
        assertEquals(local.manifest.id, observed?.selectedModelId)
    }

    @Test
    fun `embedding routing remains local and deterministic`() = runTest {
        val embedding = FakeEmbeddingPort()
        val router = DeterministicModelRouter(
            chatModels = emptyList(),
            embeddingModels = listOf(embedding),
        )
        val request = EmbeddingRouteRequest(
            purpose = EmbeddingPurpose.RECALL_QUERY,
            deviceState = device(available = setOf(embedding.manifest.id)),
        )

        assertEquals(embedding.manifest.id, router.resolveEmbedding(request).chosen?.id)
    }

    @Test
    fun `duplicate manifest ids are rejected at registry construction`() {
        val manifest = local()

        assertFailsWith<IllegalArgumentException> {
            router(FakeChatModelPort(manifest), FakeChatModelPort(manifest))
        }
    }

    private fun router(vararg models: FakeChatModelPort) = DeterministicModelRouter(models.toList())

    private fun route(
        privacy: PrivacyClass = PrivacyClass.P1_PERSONAL,
        policy: RoutingPolicy = RoutingPolicy(),
        device: RoutingDeviceState = device(),
        estimatedTokens: Int = 100,
        language: Language? = Language.EN,
    ) = ChatRouteRequest(
        request = chatRequest(privacy, language),
        estimatedInputTokens = estimatedTokens,
        deviceState = device,
        policy = policy,
    )

    private fun chatRequest(
        privacy: PrivacyClass = PrivacyClass.P1_PERSONAL,
        language: Language? = Language.EN,
        content: String = "hello",
    ) = ChatRequest(
        messages = listOf(ChatMessage(ChatMessage.Role.USER, content)),
        privacyClass = privacy,
        latencyBudget = LatencyBudget.INTERACTIVE,
        languageHint = language,
    )

    private fun device(
        network: NetworkState = NetworkState.UNMETERED,
        ramMb: Int = 4_096,
        available: Set<String> = setOf(
            "local-chat@v1",
            "a-fast@v1",
            "z-slow@v1",
            "a-model@v1",
            "b-model@v1",
            "allowed@v1",
            "blocked@v1",
        ),
    ) = RoutingDeviceState(network, ramMb, available)

    private fun local(
        id: String = "local-chat@v1",
        capabilities: Set<ModelManifest.ModelCapability> = setOf(ModelManifest.ModelCapability.CHAT),
        latencyMs: Long = 50,
        contextWindow: Int = 8_192,
    ) = FakeManifests.chat(
        id = id,
        capabilities = capabilities,
        latencyP50Ms = latencyMs,
        contextWindow = contextWindow,
    )

    private fun cloud() = FakeManifests.cloudChat(id = "cloud-chat@v1")
}
