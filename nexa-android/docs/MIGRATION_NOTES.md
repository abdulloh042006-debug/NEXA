# Migration Notes

## Sprint 1 (foundation)

**No migrations required.** This is the first code drop: there is no prior codebase, database schema, or user data to migrate.

Standing notes for future sprints:

1. **Room schemas:** when the first `@Database` lands in `:core:data` (Sprint 2, over SQLCipher per SPEC §10), enable `room.schemaLocation` export in the `nexa.room` convention plugin and commit schemas under `core/data/schemas/` — migration tests are mandatory from schema v1→v2 onward (PVRD reliability NFR: zero data loss). `fallbackToDestructiveMigration` is banned (SPEC §10).
2. **Version catalog bumps:** dependency upgrades go through the catalog only, one logical group per PR, with CI green as the merge gate. AGP/Kotlin/KSP move together per the compatibility matrix noted in `libs.versions.toml`.
3. **Signing migration (pre-release):** replace ED-7's debug-signed release path with CI-secret keystore or Play App Signing; no repository change beyond the convention plugin's signing block.
4. **Namespace stability:** `ai.nexa.*` packages and the `ai.nexa.app` applicationId are final (SPEC §5.2, ED-13) — changing an applicationId after any public install is a different app on Play; treat as immutable.
5. **Proto schema evolution:** `LocalSettings.schema_version` starts at 1; every proto field change bumps it and ships a read-path migration in `:core:data` with a test against the previous serialized form.
