# Phase 2 Kernel and Routing Contracts

## Blackboard responsibility

The kernel blackboard is typed, process-local working memory for coordination between kernel owners. A key declares its value type and owner; only that owner can mutate it. Transactions apply atomically under one revision, snapshots are immutable, and optional TTL expiry is evaluated against an injected clock. It is not a durable store, service locator, arbitrary string map, or execution channel.

Each entry carries source, confidence, privacy class, provenance, creation time, and optional expiry. These metadata stay with the value so later orchestration can enforce provenance and privacy rather than infer them from content.

## Model manifest responsibility

`ModelManifest` describes model identity, provider, placement, capabilities, context capacity, quality, cost, latency, language suitability, privacy clearance, quota, and—only for local models—validated artifact/runtime requirements. Construction rejects contradictory or unsafe configurations. Credentials and product routing branches do not belong in manifests.

## Deterministic routing

Callers submit structured intent to `RouterPort`; they never call or select a concrete provider. The router first applies hard constraints:

- required capabilities and context capacity;
- privacy clearance and explicit offline/cloud rules;
- network, local artifact availability, and RAM suitability;
- language, model/provider restrictions, and cost ceilings.

An excluded candidate cannot be restored by scoring. Eligible candidates receive a deterministic weighted score for quality, cost, and latency. Equal scores use canonical manifest ID ordering, so identical input, registry, policy, and device state always produce the same decision.

## Privacy-safe decision records

Every resolution produces a `RoutingDecisionRecord` containing selected model ID, candidate model/provider IDs, eligibility, score, structured exclusion reason codes, and selection reason codes. It deliberately excludes prompts, conversation content, credentials, token text, and other user data. `RoutingDecisionObserver` is a minimal in-process observation seam, not telemetry infrastructure.

Streaming fallback is limited to an expected model invocation failure before the first delta. Once any delta is emitted, failure propagates; the router never silently switches models and replays partial output. Hard constraints are never relaxed during fallback.

## Integration boundary

`DefaultChatSessionPort` builds a privacy-sensitive route request and invokes `RouterPort`. The app composition root registers the Gemini gateway adapter and the offline placeholder, and supplies current runtime constraints through `RoutingEnvironmentPort`. Phase 2 reports unknown connectivity/local-runtime state conservatively, preserving offline-safe behavior without making paid calls or hiding a network fallback.
