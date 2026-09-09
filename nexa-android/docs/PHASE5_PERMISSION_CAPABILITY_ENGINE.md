# Phase 5 Permission and Capability Engine

## Default-deny authority

`:core:permission` is NEXA's authoritative capability choke point. A validated plan is not authorized. Android permission state is not authorization. Missing, invalid, expired, revoked, mismatched, or policy-prohibited authority is denied deterministically. Model output can declare a requirement but cannot create a grant or decision.

The closed model contains capability IDs and categories, typed targets, risk and consent metadata, requested scope, and Android prerequisites. It covers only Phase 4's app launch, external URL, reminder creation, and message drafting intents. There is no wildcard or arbitrary permission name.

## Requirements, grants, and decisions

A `CapabilityRequirement` describes what an action asks for. A `CapabilityGrant` is immutable, target-specific, time-bounded, consent-sourced, and scoped to one request, a session, or an explicit time window. The in-memory `GrantStore` returns grants in stable ID order and revocation is immediate and idempotent. Expiry uses an injected clock and is effective at the exact expiry timestamp.

`CapabilityEngine` returns only typed `ALLOW`, `DENY`, or `REQUIRE_CONSENT` decisions with structured reason codes. It selects the lowest matching active grant ID, never scores authority, never falls back to another action, and emits audit events containing only safe IDs/categories and timestamps.

## Consent and Android prerequisites

Consent requests expose the action category, capability ID, target category, and requested scope—not prompt text, reasoning, message content, target values, secrets, or provider details. A UI response must create a valid grant through the Permission Engine; it is never authority by itself.

Android runtime permissions are read through `RuntimePermissionStatePort`. They are prerequisites only. An OS permission without a matching NEXA grant remains denied; a NEXA grant without a required OS permission is also denied. Core policy never opens an Android permission dialog.

## Plan authorization and revalidation

`PlanAuthorizationPort` evaluates every node in a `ValidatedPlan`. Any denial prevents a fully authorized result, consent-required nodes remain explicit, and denied nodes are never silently dropped. `PermissionedPlanningPort` composes Phase 3/4 planning with authorization and returns at the decision boundary.

A fully allowed plan receives an immutable snapshot bound to plan ID, version, a SHA-256 content fingerprint, decision records, grant IDs, and issue time. The snapshot is not a bearer token and explicitly requires live Phase 6 revalidation. Revoked or expired grants must be checked again immediately before every future side effect.

## Storage and Phase 6 boundary

Phase 5 intentionally uses an in-memory session store behind `GrantStore`; no database schema changes are made. A future persistent adapter must use protected storage and a non-destructive migration.

Phase 5 performs **no device actions**. It contains no Android intent dispatch, shell, accessibility action, notification/calendar/message mutation, or Automation Engine implementation. Phase 6 may consume authorization data only after revalidating the snapshot and current grants/runtime prerequisites through this engine.
