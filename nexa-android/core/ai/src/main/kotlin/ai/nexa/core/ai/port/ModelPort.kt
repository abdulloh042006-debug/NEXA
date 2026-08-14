package ai.nexa.core.ai.port

import ai.nexa.core.ai.model.ModelManifest

/**
 * Root of the ModelPort family (ARCHITECTURE §10.1) — the one seam through
 * which anything in NEXA talks to any model.
 *
 * Implementations are adapters over a concrete runtime or endpoint (llama.cpp,
 * ONNX, AICore, cloud gateway) and live in `:core:inference-local` or router
 * impl modules — only `:core:ai` and `:router` know models exist (SPEC §12.1).
 */
interface ModelPort {
    /** The manifest describing this model — the router routes on manifests only. */
    val manifest: ModelManifest
}
