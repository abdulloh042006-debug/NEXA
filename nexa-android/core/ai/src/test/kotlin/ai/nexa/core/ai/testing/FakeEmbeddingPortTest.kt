package ai.nexa.core.ai.testing

import ai.nexa.core.ai.model.EmbeddingPurpose
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FakeEmbeddingPortTest {

    @Test
    fun `embed returns one vector per input with the configured dimensionality`() = runTest {
        val port = FakeEmbeddingPort(dimensions = 8)

        val embeddings = port.embed(listOf("olma", "anor", "uzum"), EmbeddingPurpose.MEMORY_INDEX)

        assertEquals(3, embeddings.size)
        embeddings.forEach { assertEquals(8, it.dimensions) }
    }

    @Test
    fun `embedding is deterministic - equal texts map to equal vectors`() = runTest {
        val port = FakeEmbeddingPort()

        val first = port.embed(listOf("olma"), EmbeddingPurpose.RECALL_QUERY).single()
        val again = port.embed(listOf("olma"), EmbeddingPurpose.RECALL_QUERY).single()
        val other = port.embed(listOf("anor"), EmbeddingPurpose.RECALL_QUERY).single()

        assertEquals(first, again)
        assertNotEquals(first, other)
    }

    @Test
    fun `embed records texts and purpose per call`() = runTest {
        val port = FakeEmbeddingPort()

        port.embed(listOf("olma"), EmbeddingPurpose.MEMORY_INDEX)
        port.embed(listOf("anor", "uzum"), EmbeddingPurpose.RECALL_QUERY)

        assertEquals(
            listOf(
                listOf("olma") to EmbeddingPurpose.MEMORY_INDEX,
                listOf("anor", "uzum") to EmbeddingPurpose.RECALL_QUERY,
            ),
            port.recordedCalls,
        )
    }
}
