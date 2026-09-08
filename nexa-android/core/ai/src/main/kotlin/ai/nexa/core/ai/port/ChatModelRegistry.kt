package ai.nexa.core.ai.port

/** Immutable, typed registry of executable chat backends keyed by manifest id. */
class ChatModelRegistry(models: Collection<ChatModelPort>) {
    private val modelsById = models.associateBy { it.manifest.id }

    init {
        require(modelsById.size == models.size) { "duplicate chat model manifest id" }
    }

    val models: Collection<ChatModelPort> get() = modelsById.values

    fun resolve(modelId: String): ChatModelPort? = modelsById[modelId]
}
