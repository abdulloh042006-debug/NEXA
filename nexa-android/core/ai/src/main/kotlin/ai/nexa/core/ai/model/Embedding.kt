package ai.nexa.core.ai.model

/**
 * One embedding vector (ARCHITECTURE §10.1 `EmbeddingPort`). Vectors for memory
 * recall live in `sqlite-vec` inside the encrypted store; this type is only the
 * in-flight representation.
 */
class Embedding(val values: FloatArray) {
    init {
        require(values.isNotEmpty()) { "an embedding must have at least one dimension" }
    }

    val dimensions: Int get() = values.size

    override fun equals(other: Any?): Boolean =
        this === other || (other is Embedding && values.contentEquals(other.values))

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = "Embedding(dimensions=$dimensions)"
}
