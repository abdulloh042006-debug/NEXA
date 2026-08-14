package ai.nexa.router.api

import ai.nexa.core.ai.model.ModelManifest

/**
 * One routing decision, fully explainable (ARCHITECTURE §11.4): every
 * registered manifest appears either in [scores] (survived the hard filters)
 * or in [excluded] (with the first filter that rejected it). "Why this model?"
 * is answerable from this object alone.
 */
data class RouteDecision(
    /** Surviving manifests, best first — the fallback chain (ARCHITECTURE §11.2 step 3). */
    val ranked: List<ModelManifest>,
    /** Transparent score per surviving manifest id, in [ranked] order. */
    val scores: Map<String, Double>,
    /** Hard-filtered manifest ids and the first reason each was rejected. */
    val excluded: Map<String, ExclusionReason>,
) {
    /** The winning manifest, or null when nothing survived the hard filters. */
    val chosen: ModelManifest? get() = ranked.firstOrNull()
}
