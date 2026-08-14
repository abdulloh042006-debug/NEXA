package ai.nexa.router.api

import ai.nexa.core.ai.model.ChatDelta
import ai.nexa.core.ai.model.ChatRequest
import ai.nexa.core.ai.model.Embedding
import ai.nexa.core.ai.model.EmbeddingPurpose
import kotlinx.coroutines.flow.Flow

/**
 * The AI Model Router seam (ARCHITECTURE §11, SPEC §12.1): which model, where,
 * at what cost, under which privacy constraint — decided deterministically and
 * well inside the 10 ms decision budget.
 *
 * Callers express intent through [ChatRequest] only; there is deliberately no
 * way to name a model or vendor (AF-04 works both directions). Only `:core:ai`
 * and `:router` know models exist — the router is the single client-side
 * authority over model placement.
 */
interface RouterPort {

    /**
     * Resolves the routing decision without executing it — the inspectable
     * "why this model?" answer (ARCHITECTURE §11.4). Deterministic: the same
     * request against the same registered ports yields the same decision.
     */
    suspend fun resolveChat(request: ChatRequest): RouteDecision

    /**
     * Routes [request] and streams from the winning chat model.
     *
     * The flow is cold and cancellable exactly like the port contract it
     * delegates to (SPEC §12.2). A provider failure before the first delta
     * falls back to the next model in [RouteDecision.ranked] (ARCHITECTURE
     * §11.2 escalation ladder); a failure after deltas were emitted propagates
     * — the router never silently replays partial output. When no registered
     * model survives the hard filters the flow fails with
     * [NoEligibleModelException].
     */
    fun streamChat(request: ChatRequest): Flow<ChatDelta>

    /**
     * Resolves the embedding routing decision without executing it — same
     * inspectability contract as [resolveChat].
     */
    suspend fun resolveEmbedding(purpose: EmbeddingPurpose): RouteDecision

    /**
     * Embeds [texts] on the winning embedding model, one [Embedding] per input
     * in order. Every [EmbeddingPurpose] today is memory content, which is
     * personal by construction — the hard privacy filter therefore requires
     * P2_SENSITIVE clearance, so embeddings never leave the device
     * (ARCHITECTURE §6.5 placement table). Selection is deterministic over the
     * registered ports, keeping index and query vectors comparable; swapping
     * the embedding model is a re-index event owned by `:memory`, never a
     * silent router choice. Throws [NoEligibleModelException] when no
     * registered embedder survives the filters.
     */
    suspend fun routeEmbedding(texts: List<String>, purpose: EmbeddingPurpose): List<Embedding>
}
