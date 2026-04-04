# Task

## Scope
Harden the current runtime baseline for reader-focused deployments by removing polluted signature configuration defaults and turning high-side-effect scheduler/crawler defaults off in published runtime paths.

## Out of Scope
- Reworking the full auth/signature design
- Deleting maintainer-only backend endpoints
- Refactoring scheduler internals or database-driven task switches
- Changing CORS policy in this task
- Repackaging SQL data dumps

## Plan
- Capture the runtime-hardening scope and file boundaries in this task record.
- Add the smallest regression tests that lock the published-safe defaults and the frontend auth-header cleanup.
- Update backend property files, frontend request signing code, and local compose defaults to remove polluted values and side-effect-heavy published defaults.
- Run targeted backend and frontend validation, then sync task/docs/index updates.

## Design
This task stays deliberately narrow. The goal is not to redesign auth or task orchestration. The goal is to stop shipping obviously unsafe or polluted defaults while preserving the current maintainer capabilities behind explicit opt-in configuration.

For auth/signature cleanup:
- the current backend signature flow validates request signatures with the user token, not the `api.signature.secret` property
- the frontend request interceptor still carries a garbage fallback string that came from an accidental code paste
- the safe change is to remove the polluted fallback and only generate signatures when a token exists

For task defaults:
- reader-focused published runs should not enable crawlers, sitemap generation, file imports, or error-repair loops by default
- maintainer runs can still opt in through environment variables or database-backed dictionary switches
- `prod` profile and compose files should agree on these safe defaults so local and published paths stop drifting

## File Structure
- `novel/src/main/resources/application-dev.properties`: remove polluted signature default while preserving dev maintainer mode
- `novel/src/main/resources/application-prod.properties`: make published runtime defaults reader-safe and remove polluted signature default
- `free-novel-web/src/api/axios.js`: remove the invalid fallback signature secret from request signing
- `docker-compose.local-single.yml`: align published-image local compose defaults with safe task switches
- `docker-compose.local-dual.yml`: align dual-mode local compose defaults with safe task switches
- `novel/src/test/java/com/wtl/novel/Config/RuntimeDefaultsTest.java`: lock backend property defaults with a focused regression test
- `free-novel-web/tests/requestAuth.spec.mjs`: lock frontend auth-header behavior without booting the whole app
- `docs/engineering/runtime-operations.md`: record the safe published-default stance
- `docs/tasks/backend/INDEX.md`: backend module task index update

## Review
Pre-implementation review stayed lightweight. The task matches the stabilization roadmap because it reduces runtime drift, removes one polluted cross-layer auth artifact, and makes published defaults safer for reader-mode deployments without widening scope into deeper scheduler or auth refactors.

## Implementation
- Added `RuntimeDefaultsTest` to lock safe prod defaults and clean signature-property placeholders in backend profile files.
- Added `requestAuth.spec.mjs` and a small shared helper `free-novel-web/src/api/requestAuth.mjs` so frontend auth-header behavior can be verified without booting the whole SPA.
- Replaced the polluted `api.signature.secret` literal in both backend profile files with an environment-variable placeholder.
- Changed published `prod` defaults for scheduler, crawler, sitemap, file-import, and chapter-repair task switches from opt-out to opt-in.
- Updated both local compose entrypoints so published-image local runs inherit the same safe task defaults.
- Removed the frontend request interceptor fallback that reused the pasted garbage string as a signing secret and now derive the signed header only when a token exists.

## Validation
- `mvn -Dtest=RuntimeDefaultsTest test`
- `node tests/requestAuth.spec.mjs`
- `mvn -DskipTests package -Pdev`
- `npm run build`
- Browser acceptance not run. This task changes request/auth plumbing and runtime defaults, not visible page behavior, and the local database import is still running in parallel.

## Documentation Sync
- Updated: `docs/engineering/runtime-operations.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `docs/tasks/backend/2026-04-03-runtime-hardening-safe-defaults.md`
- Checked with no change needed: `README.md`, `docs/context/development-roadmap.md`, `docs/engineering/database.md`

## Risks
- The scheduler still polls on startup; this task only hardens default switches, not the scheduler architecture.
- The backend still exposes maintainer APIs in reader mode by design.
- CORS remains broad until handled in a separate task.

## Follow-ups
- Audit and narrow CORS / preflight behavior for published reader-mode deployments.
- Decide whether `api.signature.secret` should be removed entirely from backend config after the auth flow is fully documented.
- Reconcile runtime schema ownership between SQL dumps and `DatabaseInitializer`.
