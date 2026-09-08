package ai.nexa.kernel.blackboard

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun interface BlackboardClock {
    fun nowEpochMillis(): Long
}

/** Concurrency-safe in-process working memory. Durable state belongs in engine stores. */
class InMemoryBlackboard(
    private val clock: BlackboardClock = BlackboardClock(System::currentTimeMillis),
) : Blackboard {
    private val mutex = Mutex()
    private var revision = 0L
    private var entries: Map<BlackboardKey<*>, BlackboardEntry<*>> = emptyMap()

    override suspend fun snapshot(): BlackboardSnapshot = mutex.withLock {
        removeExpired(clock.nowEpochMillis())
        currentSnapshot()
    }

    override suspend fun apply(mutations: List<BlackboardMutation>): BlackboardSnapshot = mutex.withLock {
        val now = clock.nowEpochMillis()
        removeExpired(now)
        if (mutations.isEmpty()) return@withLock currentSnapshot()

        val nextRevision = revision + 1
        val updated = entries.toMutableMap()
        mutations.forEach { mutation ->
            when (mutation) {
                is BlackboardMutation.Put<*> -> updated[mutation.key] = mutation.toEntry(nextRevision, now)
                is BlackboardMutation.Remove -> updated.remove(mutation.key)
            }
        }
        entries = updated.toMap()
        revision = nextRevision
        currentSnapshot()
    }

    private fun BlackboardMutation.Put<*>.toEntry(
        nextRevision: Long,
        now: Long,
    ): BlackboardEntry<*> = BlackboardEntry(
        key = key,
        value = value,
        revision = nextRevision,
        writtenAtEpochMillis = now,
        expiresAtEpochMillis = ttlMillis?.let(now::plus),
        confidence = confidence,
        privacy = privacy,
        provenance = provenance.toList(),
    )

    private fun removeExpired(now: Long) {
        entries = entries.filterValues { entry ->
            entry.expiresAtEpochMillis?.let { it > now } ?: true
        }
    }

    private fun currentSnapshot() = BlackboardSnapshot(
        revision = revision,
        entries = entries.values.sortedWith(
            compareBy<BlackboardEntry<*>>({ it.key.owner.value }, { it.key.name }),
        ),
    )
}
