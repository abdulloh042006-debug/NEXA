package ai.nexa.core.proto

/** App defaults explicitly materialize the proto default; Wire's bare constructor preserves absence. */
fun defaultLocalSettings(): LocalSettings = LocalSettings(
    schema_version = LocalSettings.DEFAULT_SCHEMA_VERSION,
)
