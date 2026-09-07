package ai.nexa.core.data.settings

import ai.nexa.core.proto.LocalSettings
import ai.nexa.core.proto.defaultLocalSettings
import androidx.datastore.core.CorruptionException
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalSettingsStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `fresh store supplies v1 and persisted values survive lifecycle restart`() = runTest {
        val file = folder.root.resolve("settings.pb")
        val dispatcher = StandardTestDispatcher(testScheduler)
        val firstJob = SupervisorJob()
        val store = createLocalSettingsStore(file, CoroutineScope(firstJob), dispatcher)
        val value = LocalSettings.ADAPTER.decode(byteArrayOf(0x08, 0x01, 0xa0.toByte(), 0x06, 0x07))
        try {
            assertEquals(defaultLocalSettings(), store.data.first())
            assertEquals(value, store.updateData { value })
        } finally {
            firstJob.cancelAndJoin()
        }
        val secondJob = SupervisorJob()
        try {
            val reopened = createLocalSettingsStore(file, CoroutineScope(secondJob), dispatcher)
            assertEquals(value, reopened.data.first())
        } finally {
            secondJob.cancelAndJoin()
        }
    }

    @Test
    fun `corrupt file stays byte identical after failed read`() = runTest {
        val file = folder.newFile("corrupt.pb")
        val bytes = byteArrayOf(0x08)
        file.writeBytes(bytes)
        val job = SupervisorJob()
        try {
            val store = createLocalSettingsStore(file, CoroutineScope(job), StandardTestDispatcher(testScheduler))
            assertFailsWith<CorruptionException> { store.data.first() }
            assertArrayEquals(bytes, file.readBytes())
        } finally {
            job.cancelAndJoin()
        }
    }

    @Test
    fun `future version cannot be overwritten by an older store`() = runTest {
        val file = folder.newFile("future.pb")
        val bytes = byteArrayOf(0x08, 0x02)
        file.writeBytes(bytes)
        val job = SupervisorJob()
        try {
            val store = createLocalSettingsStore(file, CoroutineScope(job), StandardTestDispatcher(testScheduler))
            assertFailsWith<IOException> { store.updateData { defaultLocalSettings() } }
            assertArrayEquals(bytes, file.readBytes())
        } finally {
            job.cancelAndJoin()
        }
    }

    @Test
    fun `invalid update preserves the last committed value and file`() = runTest {
        val file = folder.root.resolve("updates.pb")
        val job = SupervisorJob()
        try {
            val store = createLocalSettingsStore(file, CoroutineScope(job), StandardTestDispatcher(testScheduler))
            val value = LocalSettings.ADAPTER.decode(byteArrayOf(0x08, 0x01, 0xa0.toByte(), 0x06, 0x07))
            store.updateData { value }
            val bytes = file.readBytes()
            assertFailsWith<IOException> { store.updateData { LocalSettings(schema_version = 2) } }
            assertEquals(value, store.data.first())
            assertArrayEquals(bytes, file.readBytes())
        } finally {
            job.cancelAndJoin()
        }
    }
}
