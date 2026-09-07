# Phase 1 foundation recovery

Remote baseline: 945fd4590f287f436ee4a347041e3feecebe7830.
Previous local commits were removed by workspace maintenance. This is a new
recovery commit, not a reproduction of their Git identities.

Restored nine missing core module build skeletons using MODULE_GRAPH and existing
convention plugins; app manifest with backup disabled and no launcher activity;
2 GB Gradle heap, 512 MB metaspace/Kotlin daemon, two workers, parallel disabled;
root CI workflow location; executable wrapper and distribution checksum.
No Router work was recreated. Dependency/toolchain versions remain unchanged.

Static module inventory passed: 86 unique modules, all build files present. The
combined Task 2 validation configured the full project and passed the existing 13
AI tests, 2 Konsist architecture tests, `:core:proto:build`, `:core:data` compile
and tests, and the app GMS debug APK. The restored module wiring is exercised by
those tasks. See LOCAL_SETTINGS.md for the final quality results and Work-environment
cache incidents.

Push is intentionally deferred. Final validation is recorded in the Task 2 report.
