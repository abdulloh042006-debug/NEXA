package ai.nexa.core.data.settings

import ai.nexa.core.proto.LocalSettings
import ai.nexa.core.proto.defaultLocalSettings
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Internal persistence adapter: errors remain inside core:data until a domain adapter maps them. */
internal object LocalSettingsSerializer : Serializer<LocalSettings> {
    override val defaultValue: LocalSettings = defaultLocalSettings()

    override suspend fun readFrom(input: InputStream): LocalSettings {
        // Read outside the decode catch: device IO failures are not malformed protobuf.
        val bytes = input.readBytes()
        val settings = try {
            LocalSettings.ADAPTER.decode(bytes)
        } catch (error: IOException) {
            throw CorruptionException("Cannot decode LocalSettings", error)
        }
        val version = settings.schema_version ?: LocalSettings.DEFAULT_SCHEMA_VERSION
        if (version <= 0) throw CorruptionException("Invalid LocalSettings schema version")
        if (version != LocalSettings.DEFAULT_SCHEMA_VERSION) {
            throw IOException("Unsupported LocalSettings schema version: $version")
        }
        // Missing optional field means the declared v1 default, not a historical v0 migration.
        return if (settings.schema_version == null) settings.copy(schema_version = version) else settings
    }

    override suspend fun writeTo(t: LocalSettings, output: OutputStream) {
        // Reject before touching output. Do not silently downgrade or normalize an update's value:
        // DataStore retains the supplied value in memory, so its persisted representation must agree.
        if (t.schema_version != LocalSettings.DEFAULT_SCHEMA_VERSION) {
            throw IOException("Only explicit LocalSettings schema version 1 can be written")
        }
        LocalSettings.ADAPTER.encode(output, t)
    }
}
