package ai.nexa.kernel.blackboard

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class InMemoryBlackboardTest {
    private val owner = BlackboardOwner("reasoning")
    private val conclusion = BlackboardKey.of<String>(owner, "conclusion")

    @Test
    fun `typed entry metadata is preserved in a snapshot`() = runTest {
        val board = InMemoryBlackboard(BlackboardClock { 1_000 })

        val snapshot = board.apply(
            listOf(
                BlackboardMutation.Put(
                    key = conclusion,
                    source = owner,
                    value = "grounded answer",
                    confidence = 0.8,
                    privacy = BlackboardPrivacy.SENSITIVE,
                    ttlMillis = 500,
                    provenance = listOf(BlackboardProvenance("memory:42")),
                ),
            ),
        )

        assertEquals(1, snapshot.revision)
        assertEquals("grounded answer", snapshot.get(conclusion)?.value)
        assertEquals(1_500, snapshot.get(conclusion)?.expiresAtEpochMillis)
        assertEquals(BlackboardPrivacy.SENSITIVE, snapshot.get(conclusion)?.privacy)
    }

    @Test
    fun `a mutation batch is one atomic revision`() = runTest {
        val other = BlackboardKey.of<Int>(owner, "confidence-band")
        val snapshot = InMemoryBlackboard().apply(
            listOf(
                BlackboardMutation.Put(conclusion, owner, "answer"),
                BlackboardMutation.Put(other, owner, 3),
            ),
        )

        assertEquals(1, snapshot.revision)
        assertEquals(setOf(1L), snapshot.entries.map { it.revision }.toSet())
    }

    @Test
    fun `expired working state is omitted without changing revision`() = runTest {
        var now = 100L
        val board = InMemoryBlackboard(BlackboardClock { now })
        board.apply(listOf(BlackboardMutation.Put(conclusion, owner, "temporary", ttlMillis = 10)))

        now = 110L
        val snapshot = board.snapshot()

        assertNull(snapshot.get(conclusion))
        assertEquals(1, snapshot.revision)
    }

    @Test
    fun `only the declared owner can mutate a key`() {
        assertFailsWith<IllegalArgumentException> {
            BlackboardMutation.Put(conclusion, BlackboardOwner("other"), "value")
        }
    }

    @Test
    fun `concurrent writes are serialized into unique revisions`() = runTest {
        val board = InMemoryBlackboard()

        val revisions = (1..20).map { value ->
            async {
                board.apply(listOf(BlackboardMutation.Put(conclusion, owner, value.toString()))).revision
            }
        }.awaitAll()

        assertEquals((1L..20L).toSet(), revisions.toSet())
        assertEquals(20, board.snapshot().revision)
    }
}
