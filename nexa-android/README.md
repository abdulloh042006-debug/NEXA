# NEXA — Android

The Android client of the NEXA AI Operating Companion.
This layer of the repository contains **engineering foundation only** (Sprint 1): the complete 86-module Gradle skeleton per ANDROID_ENGINEERING_SPECIFICATION.md, build logic, quality tooling, and CI — deliberately no business features.

Governing documents live in the repository root (`../`): `ANDROID_ENGINEERING_SPECIFICATION.md` (how this codebase is built — the normative reference for everything here), `ARCHITECTURE.md`/`ARCHITECTURE_V2.md`, `CONSTITUTION.md`, `PRODUCT_REQUIREMENTS.md`, and the behavior bibles. They are **final and approved**; code implements them.

## Requirements

- JDK 17 (Temurin recommended)
- Android SDK: platform 35, build-tools 35.0.0
- No global Gradle — use the wrapper (Gradle 8.11.1 pinned)

Full setup: [docs/SETUP.md](docs/SETUP.md)

## Quick start

```bash
./gradlew :app:assembleDebug         # debug APKs (gms + nogms flavors)
./gradlew test testDebugUnitTest     # unit tests + Konsist architecture law
./gradlew detekt                     # static analysis + ktlint rules (detekt-formatting)
./gradlew lintDebug :app:lintGmsDebug
```

## Layout (SPEC §5.1)

```
nexa-android
├── app/                       :app — composition root (flavors gms/nogms)
├── kernel/  reasoning/  router/            {api,impl} seams
├── cognition/<8 engines>/     {api,impl}   worldmodel goal planning critic
│                                           reflection learning curiosity preference
├── self/<6 engines>/          {api,impl}   identity personality emotion trust
│                                           relationship experience
├── engine/                    memory context automation {api,impl}
│                              voice {api,impl,whisper,tts,androidspeech}
│                              vision {api,impl,ocr,screen,camera}
│                              plugin {api,impl}
├── core/                      common proto ai inference-local permission
│                              data sync events background network design
├── platform/<10 adapters>/    the ONLY home of sensitive Android APIs
├── feature/<10 surfaces>/     chat voice-ui overlay memory-browser workflows
│                              goals trust timeline onboarding settings-privacy
├── benchmark/                 Macrobenchmark (cold start, SPEC §16)
├── konsist-tests/             architecture law, CI-enforced
├── build-logic/               convention plugins = the §4.1 module type system
└── config/detekt/             detekt + formatting (ktlint) configuration
```

Graph, rules, and populated-module map: [docs/MODULE_GRAPH.md](docs/MODULE_GRAPH.md)
Why each decision: [docs/ENGINEERING_DECISIONS.md](docs/ENGINEERING_DECISIONS.md)

## The module type system (SPEC §4.1)

| Declare | Get |
|---|---|
| `nexa.kotlin.domain` | pure Kotlin, coroutines + javax.inject, zero Android |
| `nexa.android.api` | Android library + `:core:common` — contracts only |
| `nexa.android.impl` | library + Hilt — implementations & DI bindings |
| `nexa.android.feature` | library + Compose + Hilt + design system + navigation |
| `nexa.android.platform` | library + `:core:permission` — sensitive API adapters |

## CI

[.github/workflows/ci.yml](.github/workflows/ci.yml): quality (spotless + detekt/ktlint) · unit tests + Konsist · Android Lint · debug assembly both flavors (APK artifacts) · release assembly (R8, debug-signed per ED-7) · benchmark compile check.

## Sprint status

**Sprint 1 — foundation: complete.** Sprint 2 opens with the first contracts: `ModelPort` in `:core:ai`, memory entities + SQLCipher in `:core:data`, kernel blackboard API, and the chat feature's navigation graph.
