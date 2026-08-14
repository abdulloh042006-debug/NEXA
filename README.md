# NEXA — AI Operating Companion

Monorepo for the NEXA project: an AI operating companion for Android — trilingual (uz/ru/en), memory-first, privacy-first, built for Central Asia first.

## Layout

| Path | Contents |
|---|---|
| [`nexa-android/`](nexa-android/) | Android client — 86-module Gradle project (Sprint 1 foundation) |
| `ANDROID_ENGINEERING_SPECIFICATION.md` | How the Android codebase is built — normative for everything in `nexa-android/` |
| `ARCHITECTURE.md` / `ARCHITECTURE_V2.md` | Approved system architecture (v2 supersedes v1 where they conflict) |
| `PRODUCT_REQUIREMENTS.md` | Product vision & requirements (PVRD) — the product source of truth |
| `CONSTITUTION.md` | Inviolable behavior rules — wins every conflict |
| `PERSONALITY_BIBLE.md` · `INTERACTION_PHILOSOPHY.md` · `PROACTIVE_INTELLIGENCE.md` | How NEXA speaks, relates, and when it acts first |
| `EMOTION_ENGINE.md` · `RELATIONSHIP_ENGINE.md` · `IDENTITY_ENGINE.md` | Self & social engine specifications |
| `DIGITAL_LEGACY.md` · `FAMILY_AI.md` | Death policy and household model |

## Precedence

`CONSTITUTION.md` → `PRODUCT_REQUIREMENTS.md` → architecture documents → engine/behavior documents → code. Code never overrides a governing document; changing behavior means amending the document first.

## Getting started (engineering)

See [`nexa-android/docs/SETUP.md`](nexa-android/docs/SETUP.md).
