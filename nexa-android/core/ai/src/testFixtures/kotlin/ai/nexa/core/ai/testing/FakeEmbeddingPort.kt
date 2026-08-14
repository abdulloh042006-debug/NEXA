package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.Embedding
import ai.nexa.core.ai.model.EmbeddingPurpose
import ai.nexa.core.ai.model.ModelManifest
import ai.nexa.core.ai.port.EmbeddingPort

/**
 * Deterministic [EmbeddingPort] fake: the same text always embeds to the same
 * vector, and equal texts are the only texts with equal vectors — enough to
 * test recall ranking logic without a real model.
 */
class FakeEmbeddingPort(
    override val manifest: ModelManifest = FakeManifests.embedding(),
    private val dimensions: Int = 8,
) : EmbeddingPort {

    private val recorded = mutableListOf<Pair<List<String>, EmbeddingPurpose>>()

    /** Every embed call that has been made, in order. */
    val recordedCalls: List<Pair<List<String>, EmbeddingPurpose>> get() = recorded

    override suspend fun embed(texts: List<String>, purpose: EmbeddingPurpose): List<Embedding> {
        recorded += texts to purpose
        return texts.map { text ->
            val seed = text.hashCode()
            Embedding(FloatArray(dimensions) { i -> ((seed * (i + 1)).toFloat() / Int.MAX_VALUE) })
        }
    }
}
