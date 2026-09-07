# LocalSettings v1

## Scope and ownership

Based on ARCHITECTURE section 8.4/24/27, ARCHITECTURE_V2 section 15,
ANDROID_ENGINEERING_SPECIFICATION sections 2.3/8.2/10/17, CONSTITUTION privacy
rules, MODULE_GRAPH, MIGRATION_NOTES and ENGINEERING_DECISIONS ED-9.

`:core:proto` owns the Wire schema and app default factory. `:core:data` owns the
internal typed DataStore factory and serializer. No feature or engine dependencies,
Room entities, Hilt bindings, cloud integration, UI or Router implementation are added.

Only `schema_version` (uint32, field 1, declared default 1) is justified concretely
by the current docs and code. There are no current callers requiring another local
setting. Learned preferences, permission grants, secrets and personal/business data
are not added to this store. Generated Kotlin lives under build/ and is not committed.

## Defaults and version policy

Wire 5.1.0 preserves proto2 optional absence as null, even with a declared default.
`defaultLocalSettings()` is the application default: schema_version is explicitly 1.
The generated bare `LocalSettings()` is a wire-level absent-field message, not the
application default. Serializer defaults and empty/missing-field reads materialize 1.
Unknown fields survive normalization and writes.

Version 0/invalid signed representations are rejected as corruption; a future
version is an IOException, not a request to reset the file. Writes require explicit
version 1 and reject other values before emitting bytes. This prevents DataStore's
in-memory update value differing from what the serializer writes. No fake v0 migration
exists. Before any future field change: bump the version, preserve/reserve tags and
names, and ship a tested read-path migration against the previous serialized bytes.

## Lifecycle and errors

The internal factory takes a stable app-private file, an owned application scope
with a Job, and an injected IO dispatcher. It retains that Job; it does not create
an independent lifecycle. One instance per file in the main process is required.
Task 5 will provide the core:data singleton binding and application-private path.
Cancel and join the owner scope before reopening the file.

Device IO errors propagate unchanged. Malformed Wire bytes become CorruptionException.
There is no ReplaceFileCorruptionHandler, automatic reset, catch-and-default flow,
or destructive migration. Failed reads/updates preserve the original file for guided
recovery. The seam remains internal to core:data: future domain adapters must map
errors to the architecture's DomainError rather than exposing storage exceptions.
Guided recovery UI, sync restoration and the common DomainError hierarchy do not
exist in this foundation and are not fabricated here.

## Tests and validation

18 contract tests cover application defaults, stable v1 bytes, field/unknown-field
round trips, absent vs explicit optional values, serializer default/read/write,
malformed data, invalid/future versions, device IO errors, real DataStore persistence
across lifecycle restart and preservation of the last committed file on failure.

Targeted Gradle validation passed Wire generation and `:core:proto:build`, all 5
proto tests, all 13 serializer/DataStore tests, the existing 13 AI tests and 2
Konsist architecture tests, and the app GMS debug APK. A final combined run reached
the benchmark APK and quality phase before Detekt reported only import ordering in
three Task 2 files. After correcting those imports, the same pinned Detekt 1.23.7
CLI and formatting plugin passed `core:data/src`; Spotless's configured misc
invariants and `git diff --check` also passed. The initial checkpoint bundle retains
the full Gradle results; the final bundle contains the corrected commits.

Runtime validation used a full JDK 17 and SDK 35 outside Git. The Work environment
twice produced corrupt temporary Gradle cache artifacts (binary-store EOF and an
instrumented JAR with a missing ZIP end header). Both were quarantined and rebuilt;
repository configuration was not changed to hide an environment failure.

References: [Wire generation](https://square.github.io/wire/wire_compiler/),
[DataStore lifecycle](https://developer.android.com/topic/libraries/architecture/datastore).
