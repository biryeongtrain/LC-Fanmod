---
적용: 항상
---

# Lobotomy Corporation AGENT GUIDE

This document defines repository-wide rules for Codex and other automated contributors.  
Scope: entire repository unless a deeper `AGENTS.md` overrides this file.

## 0) Mission and Priorities

- Primary mission: maintain dedicated-server stability while implementing requested gameplay/content changes safely.
- Prioritize runtime reliability, backward-compatible behavior, and maintainability over broad refactors.
- Prefer small, reversible patches that preserve existing behavior unless a behavior change is explicitly requested.

---

## 1) Project Baseline

- Target runtime: Minecraft `1.21.1`
- Mod loader and API: Fabric Loader + Fabric API
- Language: Java `21`
- Build system: Gradle Wrapper (`gradlew`, `gradlew.bat`)
- Current module roots:
  - `com.biryeongtrain.lc` (mod bootstrap/entrypoint)
  - `com.biryeongtrain.lc.block`
  - `com.biryeongtrain.lc.canvas`
  - `com.biryeongtrain.lc.player`
  - `com.biryeongtrain.lc.mixin`
- Main verification entry points:
  - `.\gradlew.bat build` (or `./gradlew build`)
  - `.\gradlew.bat runServer` (or `./gradlew runServer`)
  - `.\gradlew.bat runClient` (or `./gradlew runClient`) when client-side display behavior is changed

---

## 2) Package Responsibilities

### MUST
- Keep `com.biryeongtrain.lc` focused on bootstrap, registration, and lifecycle wiring.
- Keep `com.biryeongtrain.lc.player` as gameplay role/domain contracts and related logic.
- Keep `com.biryeongtrain.lc.canvas` for map-canvas display/view composition only.
- Keep `com.biryeongtrain.lc.block` for block-specific runtime behavior and integration glue.
- Keep `com.biryeongtrain.lc.mixin` narrow and purpose-specific with minimal business logic.
- Keep server-authoritative decisions in server-side logic, not in visual/canvas wrappers.

### FORBIDDEN
- Do not move core gameplay rules into mixins unless injection constraints require it.
- Do not introduce ad-hoc global state bypassing existing manager/provider-style flows once they exist.
- Do not mix unrelated concerns across package boundaries in a single class.

---

## 3) Dependency and Boundary Rules

### MUST
- Preserve dedicated-server safety in gameplay code paths (avoid client-only assumptions in shared runtime logic).
- Keep Polymer/MapCanvas integration details isolated from core gameplay decision logic.
- Use existing project dependencies/JDK APIs before introducing new libraries.
- Keep long-running or delayed gameplay actions on the server thread with explicit scheduling patterns.

### FORBIDDEN
- Do not hard-couple generic logic to optional integrations when an integration boundary can be used.
- Do not add client-required behavior to server-only entrypoints unless explicitly requested.

---

## 4) Data and Persistence Policy

### MUST
- Treat persisted keys/paths as compatibility-sensitive once introduced.
- Prefer codec- or schema-driven serialization for new persistent data.
- Keep fallback/default behavior deterministic when data is missing or malformed.
- Log enough context for persistence errors (player UUID, path, operation).

### FORBIDDEN
- Do not silently change JSON/data shapes used by existing saved data.
- Do not replace structured parsing with ad-hoc string parsing for stable data paths.

---

## 5) Mixins and Runtime Safety

### MUST
- Keep mixin targets explicit and minimal.
- Add short intent comments for non-obvious or safety-critical injections.
- Re-verify startup flow after any mixin change.
- Keep mixin package declarations and resource config aligned with actual Java package structure.

### FORBIDDEN
- No broad/speculative injections.
- No global behavior changes to vanilla/server flow without explicit justification.

---

## 6) Commands and Lifecycle

### MUST
- Keep command behavior explicit and operator feedback actionable.
- Keep command registration centralized and lifecycle-safe.
- Preserve mod initialization order: bootstrap -> register events/commands -> runtime actions.
- Include permission checks for admin-impacting command actions.

### FORBIDDEN
- Do not add hidden side effects to command handlers.
- Do not weaken permission gates without explicit request.

---

## 7) Code Style and Implementation Rules

### MUST
- Follow local style of touched files (imports, naming, formatting, logging).
- Keep diffs tightly scoped to the requested task.
- Prefer small additive edits over broad rewrites.
- Update docs/comments when public behavior contracts change.

### RECOMMENDED
- Reuse existing helpers before adding new abstractions.
- Keep interfaces and data carriers simple and immutable where practical.

### FORBIDDEN
- No drive-by refactors or unrelated cleanup in the same patch.

---

## 8) Exception and Logging Policy

### MUST
- Fail with actionable context for critical operations.
- Include identifiers in logs when relevant (player name/UUID, command, resource path/id).
- Keep fallback behavior explicit when recovery is possible.

### FORBIDDEN
- No swallowed exceptions without logging.
- No vague error messages lacking failing operation context.

---

## 9) Backward Compatibility Policy

### MUST
- Treat the following as compatibility-sensitive:
  - command names/arguments/permission requirements
  - saved data schemas and key names
  - registry/resource identifiers
  - gameplay role semantics once used by worlds/saves
- For unavoidable breaking changes, document impact and migration steps in the change summary.

---

## 10) Dependency Introduction Policy

### MUST
- New dependency additions require explicit justification:
  - why current stack is insufficient
  - exact scope (packages/features affected)
  - runtime/maintenance impact

### FORBIDDEN
- Do not add overlapping utility libraries for trivial features.

---

## 11) Testing and Verification Policy

### MUST
- Run checks relevant to touched areas and report exactly what was run:
  - baseline compile/build: `build`
  - startup/lifecycle/mixin/block/runtime behavior: `runServer`
  - client-visible map/canvas behavior: `runClient` when required
- If a verification step cannot be run, state the limitation explicitly.
- When stopping verification tasks, terminate only processes started for the current task/session.

### RECOMMENDED
- Add focused automated tests when introducing non-trivial gameplay logic.

### FORBIDDEN
- Do not use broad kill patterns that can stop unrelated Java/Gradle processes.

---

## 12) Documentation and Change Hygiene

### MUST
- Update docs when changing command behavior, data format, or operator workflow.
- Keep summaries practical: what changed, why, compatibility impact, validation results.
- Never revert unrelated user-authored changes.

---

## 13) Existing Code Precedence

### MUST
- Existing repository behavior and established local conventions take precedence over generic style advice.
- If this guide conflicts with hard code constraints, preserve runtime behavior first and note the mismatch.

---

## 14) Final Operational Checklist

Before finishing, verify:
- scope is minimal and task-aligned
- package boundaries are respected
- compatibility-sensitive behavior is preserved or documented
- relevant checks were run (or limitations reported)
- summary includes rationale, risk notes, and validation results
