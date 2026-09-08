package ai.nexa.router.api

/**
 * The AI Model Router seam (ARCHITECTURE §11, SPEC §12.1): which model, where,
 * at what cost, under which privacy constraint — decided deterministically and
 * well inside the 10 ms decision budget.
 *
 * Callers express intent through [ChatRouteRequest] only; there is deliberately no
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
    suspend fun resolveChat(request: ChatRouteRequest): RouteDecision

    /**
     * Resolves the embedding routing decision without executing it — same
     * inspectability contract as [resolveChat].
     */
    suspend fun resolveEmbedding(request: EmbeddingRouteRequest): RouteDecision
}
