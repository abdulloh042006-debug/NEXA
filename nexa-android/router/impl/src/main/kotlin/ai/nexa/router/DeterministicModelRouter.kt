package ai.nexa.router

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.Embedding
import ai.nexa.core.ai.model.LatencyBudget
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.model.PrivacyClass
import ai.nexa.core.ai.port.ChatModelPort
import ai.nexa.core.ai.port.EmbeddingPort
import ai.nexa.core.ai.port.ModelInvocationException
import ai.nexa.core.ai.port.ModelPort
import ai.nexa.router.api.CandidateDecisionRecord
import ai.nexa.router.api.ChatRouteRequest
import ai.nexa.router.api.EmbeddingRouteRequest
import ai.nexa.router.api.NoEligibleModelException
import ai.nexa.router.api.RouteDecision
import ai.nexa.router.api.RouterPort
import ai.nexa.router.api.RoutingDecisionObserver
import ai.nexa.router.api.RoutingDecisionRecord
import ai.nexa.router.api.SelectionReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

/** Rule-filtered, scored routing from ARCHITECTURE §11.2. */
class DeterministicModelRouter(
    chatModels: Collection<ChatModelPort>,
    embeddingModels: Collection<EmbeddingPort> = emptyList(),
    private val observer: RoutingDecisionObserver = RoutingDecisionObserver.NONE,
) : RouterPort {
    private val chatModels = chatModels.associateByUniqueId("chat")
    private val embeddingModels = embeddingModels.associateByUniqueId("embedding")
    private val evaluator = ManifestEvaluator()

    override suspend fun resolveChat(request: ChatRouteRequest): RouteDecision {
        val outputReserve = request.request.sampling.maxOutputTokens ?: DEFAULT_OUTPUT_RESERVE_TOKENS
        return resolve(
            chatModels.values,
            RouteContext(
                requiredCapabilities = request.requiredCapabilities,
                privacyClass = request.request.privacyClass,
                contextTokens = request.estimatedInputTokens + outputReserve,
                language = request.request.languageHint,
                latencyBudget = request.request.latencyBudget,
                qualityNeed = request.qualityNeed,
                deviceState = request.deviceState,
                policy = request.policy,
            ),
        )
    }

    override fun streamChat(request: ChatRouteRequest): Flow<ChatDelta> = flow {
        val decision = resolveChat(request)
        if (decision.ranked.isEmpty()) throw NoEligibleModelException(request, decision)

        var lastFailure: ModelInvocationException? = null
        decision.ranked.forEach { manifest ->
            val model = checkNotNull(chatModels[manifest.id])
            var emitted = false
            try {
                model.streamChat(request.request).collect { delta ->
                    emitted = true
                    emit(delta)
                }
                return@flow
            } catch (failure: ModelInvocationException) {
                if (emitted) throw failure
                lastFailure = failure
            }
        }
        throw checkNotNull(lastFailure)
    }

    override suspend fun resolveEmbedding(request: EmbeddingRouteRequest): RouteDecision = resolve(
        embeddingModels.values,
        RouteContext(
            requiredCapabilities = setOf(ModelManifest.ModelCapability.EMBEDDING),
            privacyClass = PrivacyClass.P2_SENSITIVE,
            contextTokens = 1,
            language = null,
            latencyBudget = LatencyBudget.BACKGROUND,
            qualityNeed = ModelManifest.QualityTier.NANO,
            deviceState = request.deviceState,
            policy = request.policy,
        ),
    )

    override suspend fun routeEmbedding(
        texts: List<String>,
        request: EmbeddingRouteRequest,
    ): List<Embedding> {
        require(texts.isNotEmpty()) { "embedding input must not be empty" }
        val decision = resolveEmbedding(request)
        val chosen = decision.chosen ?: throw NoEligibleModelException(
            "no embedding model is eligible for purpose=${request.purpose}",
            decision,
        )
        return checkNotNull(embeddingModels[chosen.id]).embed(texts, request.purpose)
    }

    private fun resolve(models: Collection<ModelPort>, context: RouteContext): RouteDecision {
        val evaluations = models.map { evaluator.evaluate(it.manifest, context) }.sortedBy { it.manifest.id }
        val ranked = evaluations.filter { it.score != null }.sortedWith(
            compareByDescending<Candidate> { checkNotNull(it.score) }.thenBy { it.manifest.id },
        )
        val selected = ranked.firstOrNull()
        val record = RoutingDecisionRecord(
            selectedModelId = selected?.manifest?.id,
            candidates = evaluations.map { it.toRecord() },
            selectionReasons = selectionReasons(ranked),
        )
        observer.onDecision(record)
        return RouteDecision(
            ranked = ranked.map(Candidate::manifest),
            scores = ranked.associate { it.manifest.id to checkNotNull(it.score) },
            excluded = evaluations.filter { it.exclusions.isNotEmpty() }
                .associate { it.manifest.id to it.exclusions },
            record = record,
        )
    }

    private fun selectionReasons(ranked: List<Candidate>): Set<SelectionReason> = when {
        ranked.isEmpty() -> setOf(SelectionReason.NO_ELIGIBLE_MODEL)
        ranked.size == 1 -> setOf(SelectionReason.ONLY_ELIGIBLE_MODEL)
        ranked[0].score == ranked[1].score -> setOf(
            SelectionReason.HIGHEST_POLICY_SCORE,
            SelectionReason.DETERMINISTIC_ID_TIE_BREAK,
        )
        else -> setOf(SelectionReason.HIGHEST_POLICY_SCORE)
    }

    private fun Candidate.toRecord() = CandidateDecisionRecord(
        modelId = manifest.id,
        providerId = manifest.providerId.value,
        eligible = exclusions.isEmpty(),
        score = score,
        exclusionReasons = exclusions,
    )

    private fun <T : ModelPort> Collection<T>.associateByUniqueId(kind: String): Map<String, T> {
        val grouped = groupBy { it.manifest.id }
        require(grouped.values.none { it.size > 1 }) { "duplicate $kind model manifest id" }
        return grouped.mapValues { it.value.single() }
    }

    private companion object {
        const val DEFAULT_OUTPUT_RESERVE_TOKENS = 1_024
    }
}
