package ai.nexa.core.ai.port

import ai.nexa.core.ai.model.Embedding
import ai.nexa.core.ai.model.EmbeddingPurpose

/**
 * Text embedding (ARCHITECTURE §10.1). Memory-recall embeddings always run
 * on-device (ARCHITECTURE §6.5 placement table) — cloud implementations exist
 * only for P0 content.
 */
interface EmbeddingPort : ModelPort {

    /**
     * Embeds [texts] in order; the result has exactly one [Embedding] per input,
     * all with the same dimensionality for a given model.
     */
    suspend fun embed(texts: List<String>, purpose: EmbeddingPurpose): List<Embedding>
}
