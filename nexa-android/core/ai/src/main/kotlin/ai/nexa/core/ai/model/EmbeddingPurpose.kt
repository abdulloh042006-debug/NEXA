package ai.nexa.core.ai.model

/**
 * Why an embedding is being computed (ARCHITECTURE §10.1 `embed(texts, purpose)`).
 *
 * Purpose selects the encoding side of asymmetric retrieval models and lets
 * adapters apply per-purpose instruction prefixes without callers knowing.
 */
enum class EmbeddingPurpose {
    /** Indexing a memory/document for later recall. */
    MEMORY_INDEX,

    /** Encoding a recall query against the index. */
    RECALL_QUERY,
}
