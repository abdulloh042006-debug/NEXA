package ai.nexa.core.data.settings

import ai.nexa.core.proto.LocalSettings
import ai.nexa.core.proto.defaultLocalSettings
import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.test.assertFailsWith

class LocalSettingsSerializerTest {
    private val serializer = LocalSettingsSerializer

    @Test
    fun `missing-file default and empty wire message yield v1`() = runTest {
        assertEquals(1, serializer.defaultValue.schema_version)
        assertEquals(defaultLocalSettings(), serializer.readFrom(ByteArrayInputStream(byteArrayOf())))
    }

    @Test
    fun `serializer preserves current fields and unknown bytes`() = runTest {
        val bytes = byteArrayOf(0x08, 0x01, 0xa0.toByte(), 0x06, 0x07)
        val settings = serializer.readFrom(ByteArrayInputStream(bytes))
        val output = ByteArrayOutputStream()
        serializer.writeTo(settings, output)
        assertEquals(1, settings.schema_version)
        assertArrayEquals(bytes, output.toByteArray())
    }

    @Test
    fun `absent version is materialized without discarding unknown fields`() = runTest {
        val unknown = byteArrayOf(0xa0.toByte(), 0x06, 0x07)
        val settings = serializer.readFrom(ByteArrayInputStream(unknown))
        assertEquals(1, settings.schema_version)
        assertArrayEquals(unknown, settings.unknownFields.toByteArray())
    }

    @Test
    fun `malformed protobuf is corruption and never a default`() = runTest {
        for (bytes in listOf(byteArrayOf(0), byteArrayOf(0x08), byteArrayOf(0x0f))) {
            assertFailsWith<CorruptionException> { serializer.readFrom(ByteArrayInputStream(bytes)) }
        }
    }

    @Test
    fun `unsupported future version is preserved as a recoverable read failure`() = runTest {
        val error = assertFailsWith<IOException> {
            serializer.readFrom(ByteArrayInputStream(byteArrayOf(0x08, 0x02)))
        }
        assertFalse(error is CorruptionException)
    }

    @Test
    fun `invalid version zero is not silently upgraded`() = runTest {
        assertFailsWith<CorruptionException> {
            serializer.readFrom(ByteArrayInputStream(byteArrayOf(0x08, 0x00)))
        }
    }

    @Test
    fun `writes reject absent invalid and future versions before emitting bytes`() = runTest {
        for (version in listOf(null, 0, -1, 2)) {
            val output = ByteArrayOutputStream()
            assertFailsWith<IOException> { serializer.writeTo(LocalSettings(schema_version = version), output) }
            assertEquals(0, output.size())
        }
    }

    @Test
    fun `device read errors propagate unchanged`() = runTest {
        val failure = IOException("device read failure")
        val input = object : InputStream() {
            override fun read(): Int = throw failure
        }
        assertSame(failure, assertFailsWith<IOException> { serializer.readFrom(input) })
    }

    @Test
    fun `device write errors propagate unchanged`() = runTest {
        val failure = IOException("device write failure")
        val output = object : OutputStream() {
            override fun write(value: Int): Unit = throw failure
        }
        assertSame(failure, assertFailsWith<IOException> { serializer.writeTo(defaultLocalSettings(), output) })
    }
}
