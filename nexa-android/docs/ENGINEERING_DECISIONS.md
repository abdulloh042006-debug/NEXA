# Engineering Decisions — Sprint 1

Each decision records what was chosen, what was rejected, and why. These are implementation decisions *inside* the approved ANDROID_ENGINEERING_SPECIFICATION — they do not amend it. Where the spec dictates the answer, the entry cites it.

## ED-1 — Version set: AGP 8.7.3 / Gradle 8.11.1 / Kotlin 2.1.0 / KSP 2.1.0-1.0.29

A mutually-verified stable compatibility set (SPEC §3: stability over novelty). Version bumps ride the catalog with the spec's cadence rules (Kotlin within a quarter, AGP after a point release matures).

## ED-2 — Convention plugins in an included build (`build-logic`)

The SPEC §4.1 module type system implemented literally: `nexa.kotlin.domain`, `nexa.android.api`, `nexa.android.impl`, `nexa.android.feature`, `nexa.android.platform`, plus shared capability plugins (`nexa.android.compose`, `nexa.hilt`, `nexa.room`) and the app plugin. A new module = one type alias + a namespace. Rejected: `subprojects {}` cross-config (breaks config cache, unmaintainable) and `buildSrc` (whole-build invalidation).

## ED-3 — Typesafe project accessors

Compile-time-checked module references (`projects.core.design`). Used by Google's own multi-module samples; the feature-preview flag is stable in practice.

## ED-4 — Day-1 module skeleton includes post-V1 seams

All cognition/self/kernel modules exist now as empty api/impl pairs (SPEC §4.2: "the seams are real even when implementations are Phase B/C"). Cost: ~80 tiny build files. Payoff: the dependency law is enforceable from the first real line of code, and no later sprint pays a restructuring tax.

## ED-5 — Quality tooling: ktlint runs as detekt-formatting

Per SPEC §19.2 exactly: **detekt** (static analysis + NEXA overrides) with **detekt-formatting** providing the complete ktlint ruleset driven by `.editorconfig`; **spotless** covers only non-Kotlin hygiene (md/yml/toml); **Konsist** owns architecture law in `:konsist-tests`; Android Lint completes the four layers. Rejected: the standalone ktlint Gradle plugin — running two ktlint hosts (plugin + detekt-formatting) produces duplicate, potentially contradictory findings.

## ED-6 — BuildConfig strategy

Only `:app` enables BuildConfig and owns `NEXA_ENV` (`dev`/`prod` by build type). Libraries carry none (AGP 8 default) — configuration flows through DI, not static fields. Distribution differences are flavors, not BuildConfig flags.

## ED-7 — Signing strategy: debug-only; release reuses debug signing

Sprint scope is explicitly debug-only. Release builds still run full R8 + resource shrinking in CI (shrinking problems surface now, not at launch) and are signed with the default debug keystore so `assembleRelease` verifies end-to-end. Real keys arrive via CI secrets/Play App Signing before any distribution (SPEC §19.3) — never in the repository.

## ED-8 — Flavors: `gms` / `nogms` from day 1

Required by SPEC §2.5/§19.1 (Play + RuStore/AppGallery/direct-APK from one codebase). Sprint 1 flavors are configuration-identical placeholders (`gms` is default; `nogms` carries a versionName suffix); dependency divergence (FCM vs. WebSocket push, ML Kit vs. portable ports) lands when those integrations land.

## ED-9 — DataStore is typed (Wire proto), not Preferences

SPEC §2.3 bans plain Preferences DataStore. `:core:proto` owns the schema (`LocalSettings` with `schema_version` from field one — every future change has a migration path); `:core:data` provides the store via a Wire-backed `Serializer`. Room is wired in `:core:data` (KSP verified by the build) with no entities yet; SQLCipher + sqlite-vec arrive with the first schema, per SPEC §10.

## ED-10 — Navigation as owned dependency, no NavHost

The `nexa.android.feature` type wires navigation-compose into every feature module; type-safe route classes and the NavHost arrive with the first screen. A zero-destination NavHost would be dead code (forbidden by sprint rules).

## ED-11 — Konsist law active from Sprint 1

`:konsist-tests` ships two executable rules now (api/domain modules are Android-free; no cross-module `.internal.` imports). They pass trivially over the skeleton and harden automatically as code lands. The full §4.3 rule set (feature→kernel-only, impl↔impl bans) is Sprint 2, once those packages contain code to inspect.

## ED-12 — `applicationIdSuffix ".debug"`

Debug and release installs coexist on one device; testers cannot report against the wrong build. (`.beta` suffix per SPEC §5.2 arrives with the beta track.)

## ED-13 — Package root `ai.nexa`; applicationId `ai.nexa.app`

Dictated by SPEC §5.2. Treated as immutable after first public install.

## ED-14 — Benchmark module compiles in CI, runs on devices

`:benchmark` (Macrobenchmark, cold-start per SPEC §16) is compile-checked by CI (`:benchmark:assembleDebug`) so it can never rot; actual measurement runs on connected devices/the device lab, which JVM CI does not have.
