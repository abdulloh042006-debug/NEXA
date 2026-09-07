# Phase 1 foundation recovery

Remote baseline: 945fd4590f287f436ee4a347041e3feecebe7830.
Previous local commits were removed by workspace maintenance. This is a new
recovery commit, not a reproduction of their Git identities.

Restored nine missing core module build skeletons using MODULE_GRAPH and existing
convention plugins; app manifest with backup disabled and no launcher activity;
2 GB Gradle heap, 512 MB metaspace/Kotlin daemon, two workers, parallel disabled;
root CI workflow location; executable wrapper and distribution checksum.
No Router work was recreated. Dependency/toolchain versions remain unchanged.

Static module inventory: 86 unique modules, all build files present.
Build validation is pending in this recovery checkpoint. The Work environment
previously failed reading a temporary Gradle binary cache; local validation uses
--no-configuration-cache, without changing the repository default.

Push is intentionally deferred. Final validation is recorded in the Task 2 report.
