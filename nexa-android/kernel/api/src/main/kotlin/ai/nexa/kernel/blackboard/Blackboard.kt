package ai.nexa.kernel.blackboard

import kotlin.reflect.KClass

/** Stable identifier of the engine that owns a blackboard key. */
@JvmInline
value class BlackboardOwner(val value: String) {
    init {
        require(value.isNotBlank()) { "blackboard owner must not be blank" }
    }
}

/**
 * A typed address in working memory. Ownership is part of the address so an
 * unrelated engine cannot overwrite another engine's state by reusing a name.
 */
data class BlackboardKey<T : Any>(
    val owner: BlackboardOwner,
    val name: String,
    val type: KClass<T>,
) {
    init {
        require(name.isNotBlank()) { "blackboard key name must not be blank" }
    }

    companion object {
        inline fun <reified T : Any> of(owner: BlackboardOwner, name: String): BlackboardKey<T> =
            BlackboardKey(owner, name, T::class)
    }
}

/** Blackboard privacy is independent of model placement and never implies egress permission. */
enum class BlackboardPrivacy {
    PUBLIC,
    PERSONAL,
    SENSITIVE,
}

/** A non-content reference explaining where an entry came from. */
@JvmInline
value class BlackboardProvenance(val reference: String) {
    init {
        require(reference.isNotBlank()) { "provenance reference must not be blank" }
    }
}

/** One immutable value in a [BlackboardSnapshot]. */
data class BlackboardEntry<T : Any>(
    val key: BlackboardKey<T>,
    val value: T,
    val revision: Long,
    val writtenAtEpochMillis: Long,
    val expiresAtEpochMillis: Long?,
    val confidence: Double,
    val privacy: BlackboardPrivacy,
    val provenance: List<BlackboardProvenance>,
) {
    init {
        require(revision > 0) { "entry revision must be positive" }
        require(confidence in 0.0..1.0) { "confidence must be in [0, 1]" }
        require(expiresAtEpochMillis == null || expiresAtEpochMillis > writtenAtEpochMillis) {
            "entry expiry must be after its write time"
        }
    }
}

/** Atomic blackboard update. Writes are legal only for the key's declared owner. */
sealed interface BlackboardMutation {
    val key: BlackboardKey<*>
    val source: BlackboardOwner

    data class Put<T : Any>(
        override val key: BlackboardKey<T>,
        override val source: BlackboardOwner,
        val value: T,
        val confidence: Double = 1.0,
        val privacy: BlackboardPrivacy = BlackboardPrivacy.PERSONAL,
        val ttlMillis: Long? = null,
        val provenance: List<BlackboardProvenance> = emptyList(),
    ) : BlackboardMutation {
        init {
            require(source == key.owner) { "only a key's owner may write it" }
            require(key.type.isInstance(value)) { "value does not match key type" }
            require(confidence in 0.0..1.0) { "confidence must be in [0, 1]" }
            require(ttlMillis == null || ttlMillis > 0) { "ttlMillis must be positive" }
        }
    }

    data class Remove(
        override val key: BlackboardKey<*>,
        override val source: BlackboardOwner,
    ) : BlackboardMutation {
        init {
            require(source == key.owner) { "only a key's owner may remove it" }
        }
    }
}

/** Immutable, revisioned view of working memory. */
data class BlackboardSnapshot(
    val revision: Long,
    val entries: List<BlackboardEntry<*>>,
) {
    fun <T : Any> get(key: BlackboardKey<T>): BlackboardEntry<T>? {
        val entry = entries.firstOrNull { it.key == key } ?: return null
        require(key.type.isInstance(entry.value)) { "blackboard entry type mismatch for ${key.name}" }
        @Suppress("UNCHECKED_CAST")
        return entry as BlackboardEntry<T>
    }
}

/**
 * Typed, transactional working memory for kernel coordination.
 *
 * Implementations must apply a mutation list atomically and return a snapshot
 * from the resulting revision. Durable product state does not belong here.
 */
interface Blackboard {
    suspend fun snapshot(): BlackboardSnapshot

    suspend fun apply(mutations: List<BlackboardMutation>): BlackboardSnapshot
}
