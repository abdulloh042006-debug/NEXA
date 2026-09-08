package ai.nexa.core.proto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalSettingsTest {
    @Test
    fun `application defaults start at version one`() {
        assertEquals(1, defaultLocalSettings().schema_version)
        assertEquals(1, LocalSettings.DEFAULT_SCHEMA_VERSION)
    }

    @Test
    fun `v1 encoding matches stable field one fixture`() {
        val bytes = byteArrayOf(0x08, 0x01)
        assertArrayEquals(bytes, LocalSettings.ADAPTER.encode(defaultLocalSettings()))
        assertEquals(defaultLocalSettings(), LocalSettings.ADAPTER.decode(bytes))
    }

    @Test
    fun `explicit field values survive raw wire round trips`() {
        for (version in listOf(0, 1, 2, Int.MAX_VALUE)) {
            val value = LocalSettings(schema_version = version)
            assertEquals(value, LocalSettings.ADAPTER.decode(LocalSettings.ADAPTER.encode(value)))
        }
    }

    @Test
    fun `wire absence stays distinct from an explicit default`() {
        val absent = LocalSettings.ADAPTER.decode(byteArrayOf())
        assertNull(absent.schema_version)
        assertNull(LocalSettings().schema_version)
        assertArrayEquals(byteArrayOf(), LocalSettings.ADAPTER.encode(absent))
        assertEquals(1, absent.schema_version ?: LocalSettings.DEFAULT_SCHEMA_VERSION)
    }

    @Test
    fun `unknown fields survive decode copy and encode`() {
        // Field 100 is deliberately unknown to this schema; it is not a new application setting.
        val bytes = byteArrayOf(0x08, 0x01, 0xa0.toByte(), 0x06, 0x07)
        val decoded = LocalSettings.ADAPTER.decode(bytes)
        assertArrayEquals(bytes, LocalSettings.ADAPTER.encode(decoded.copy(schema_version = 1)))
    }
}
