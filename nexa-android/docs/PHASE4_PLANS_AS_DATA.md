# Phase 4 Plans as Data

## Safety boundary

Plans are inert, typed data. Model text and tool-call output are untrusted and can never invoke Android, a shell, reflection, or an action adapter. Phase 4 ends at `ValidatedPlan`; it contains no action executor and performs no device action.

The canonical contracts live in `:cognition:planning:api`. `Plan` carries an opaque ID, schema version, safe origin/correlation metadata, and immutable nodes. Each `PlanNode` has a typed `ActionIntent`, explicit dependencies, and declared capability requirements. The closed representative intents are app launch, HTTP(S) URL opening, reminder creation, and message drafting. They describe proposed effects only.

## Deterministic DAG and validation

`PlanValidator` is the authoritative deterministic boundary. It accepts schema version 1, rejects empty or oversized plans, limits a plan to 64 nodes and each node to 16 dependencies, and rejects duplicate IDs, duplicate edges, missing dependencies, self-dependencies, cycles, missing capabilities, and capability/action mismatches. Independent ready nodes are ordered by node ID, producing a stable topological order.

A successful result contains a validator-issued immutable snapshot. Changing a source collection cannot alter the validated plan; any revised proposal must create a new plan/version and be validated again.

## Untrusted proposal compiler

`JsonPlanCompiler` accepts one bounded JSON document through an explicit allowlist. Unknown fields, duplicate keys, unknown action types, malformed targets, missing fields, unsupported schemas, prose, truncated JSON, and invalid DAGs fail closed with structured codes. It never performs reflective class construction and never accepts raw command strings or generic maps.

`PlanningPort` sends an explicit planning request through the Phase 3 inference orchestrator with no tool schemas. Streamed text is compiled and validated only after completion. Provider failure, cancellation, timeout, and invalid proposals remain distinct typed results. Normal conversational chat is unchanged.

## Dry-run and observability

Dry-run is deterministic inspection of what a validated plan would request: node order, dependencies, action categories, and capability categories. It is not side-effect simulation and cannot execute anything. Planning diagnostics contain IDs, counts, schema, result codes, and capability categories only—never goals, prompts, reasoning text, generated payload values, credentials, or personal data.

## Phase 5 boundary

Capability requirements are declarations, not grants. Phase 4 does not request Android permissions, grant or persist authority, decide consent, or bypass the future Permission Engine. Phase 5 must remain the sole authorization choke point before any later Phase 6 execution path.
