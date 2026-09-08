# Module Dependency Graph

86 Gradle modules. The complete skeleton remains in place (SPEC §4.2: post-V1 engines exist as day-1 seams); populated modules are marked ●.

## Structure

```
:app ●                          composition root (flavors: gms/nogms)
:kernel:{api,impl} ●            typed blackboard and inference/chat orchestration
:reasoning:{api,impl}           reasoning pipeline seam
:router:{api,impl} ●            deterministic manifest-driven model router
:cognition:planning:{api,impl} ● typed plans, validation, and proposal compilation
:cognition:{worldmodel,goal,critic,reflection,learning,curiosity,preference}:{api,impl}
:self:{identity,personality,emotion,trust,relationship,experience}:{api,impl}
:engine:memory:{api,impl}       :engine:context:{api,impl}   :engine:automation:{api,impl}
:engine:voice:{api,impl,whisper,tts,androidspeech}
:engine:vision:{api,impl,ocr,screen,camera}
:engine:plugin:{api,impl}
:core:common                    common services skeleton
:core:proto ●                   Wire schemas (LocalSettings v1)
:core:ai ●                      model ports and validated model manifests (pure JVM)
:core:inference-local           :inference process home (NDK later)
:core:permission ●              capability requirement contracts; Gatekeeper remains Phase 5
:core:data ●                    typed DataStore serializer/factory; Room build wiring only
:core:sync  :core:events  :core:background  :core:network
:core:design                    Compose build skeleton; theme pending
:platform:{telephony,calendar,contacts,files,notifications,accessibility,camera,sensors,connectivity,media}
:feature:{chat,voice-ui,overlay,memory-browser,workflows,goals,trust,timeline,onboarding,settings-privacy}
:benchmark ●                    Macrobenchmark (cold start)
:konsist-tests ●                architecture law (2 active rules)
```

## Current edges (Phase 2)

```mermaid
graph TD
    APP[":app"] --> DESIGN[":core:design"]
    APP --> KERNEL_IMPL[":kernel:impl"]
    APP --> ROUTER_IMPL[":router:impl"]
    KERNEL_IMPL --> ROUTER_API[":router:api"]
    ROUTER_IMPL --> ROUTER_API
    ROUTER_IMPL --> AI[":core:ai"]
    ROUTER_IMPL --> NETWORK[":core:network"]
    DATA[":core:data"] --> COMMON[":core:common"]
    DATA --> PROTO[":core:proto"]
    IMPLS["every :impl module"] --> OWNAPI["its own :api"]
    APIS["every :api module"] --> COMMON
    FEATS["every :feature module"] --> DESIGN
    FEATS --> COMMON
    PLATS["every :platform module"] --> PERM[":core:permission"]
    PLATS --> COMMON
    BENCH[":benchmark"] -. instruments .-> APP
```

Type-level edges (api→common, feature→design/common, platform→permission/common, impl→own api) are wired by the convention plugins — a new module is born lawful.

The `:app` composition root supplies a typed backend registry and an explicit runtime-device snapshot. The kernel orchestrator asks `RouterPort` for a deterministic ranked decision, resolves the selected adapter from that registry, and owns streaming lifecycle. Neither the UI nor router executes provider-specific logic. See [PHASE2_KERNEL_ROUTING.md](PHASE2_KERNEL_ROUTING.md) and [PHASE3_INFERENCE_ORCHESTRATION.md](PHASE3_INFERENCE_ORCHESTRATION.md).

The Planning Engine owns typed inert plan data. Its implementation uses the kernel inference port, strictly compiles untrusted model output, validates the bounded DAG, and stops at `ValidatedPlan`. It has no Automation Engine or Android actuator dependency. See [PHASE4_PLANS_AS_DATA.md](PHASE4_PLANS_AS_DATA.md).

## The law (SPEC §4.3 — Konsist-enforced, growing per ED-11)

1. `:feature:* → :kernel:api` + `:core:design`/`:core:common` only — never engine/cognition/self modules.
2. No `:impl → :impl` across engines; the kernel/events mediate.
3. `:cognition:* ↔ :self:*` direct edges forbidden.
4. Nothing depends on `:app`.
5. `kotlin-domain` and all `:api` modules: zero Android.
6. Runtime fences: Room/SQLCipher only in `:core:data`; OkHttp/gRPC only in `:core:network`; sensitive OS APIs only in `:platform:*`; model runtimes only in `:core:ai`/`:core:inference-local`.
7. `:core:*` never depends on engine/feature/cognition/self modules.
