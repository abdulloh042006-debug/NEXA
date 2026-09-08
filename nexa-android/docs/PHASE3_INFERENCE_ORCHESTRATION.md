# Phase 3 Inference Orchestration

## Request and ownership

`InferenceExecutionRequest` wraps the existing structured `ChatRouteRequest` with opaque execution/correlation IDs and a bounded timeout. It does not duplicate model input, carry credentials, or name a provider. `InferenceOrchestratorPort` is owned by the kernel and is the only chat path coordinating routing and execution.

The router is selection-only: it evaluates manifests and returns a deterministic ranked decision. The orchestrator resolves a selected ID through the immutable typed `ChatModelRegistry`; concrete adapters remain behind `ChatModelPort`.

## Lifecycle and streaming

An execution emits ordered `Queued`, `Started`, `Delta`, and exactly one terminal `Completed`, `Cancelled`, `TimedOut`, or `Failed` event. The state machine rejects events after a terminal transition. Each execution has one collector, and `cancel()` is idempotent.

Deltas retain adapter order and are never replayed. An expected sanitized adapter failure may advance to the next already-eligible ranked model only before any delta. After the first delta, a failure is terminal and the orchestrator never switches providers. A successful zero-delta stream completes normally; the chat boundary may classify an empty answer as invalid for its product contract.

Execution runs in the collector's coroutine hierarchy. Parent cancellation propagates to routing and adapter collection. Explicit cancellation cancels and joins backend work before publishing `Cancelled`. `withTimeout` bounds the complete route-and-execute operation, cancels child work, and produces a distinct `TimedOut` terminal event. Terminal races are resolved by one synchronized state machine.

## Failures and diagnostics

Adapters map transport/provider errors to sanitized `ModelInvocationException` categories. The orchestrator exposes provider-neutral codes for no route, unavailable backend/network/authentication, rejection, rate limiting, provider failure, and protocol failure, including explicit retryability. Raw provider bodies and exceptions do not cross the kernel contract.

`InferenceExecutionObserver` receives only opaque execution ID, lifecycle state, selected model/provider IDs, and failure code. It never receives prompts, messages, deltas, generated content, credentials, or personal data.

## Chat integration

The flow is `Chat UI → ChatSessionPort → InferenceOrchestratorPort → RouterPort → ChatModelPort`. User messages remain persisted before execution. Assistant text is persisted only after completed non-empty output. Structured failure, timeout, and cancellation events leave the stored user turn intact and map to honest UI error states. No paid provider access or API key is enabled by this phase.
