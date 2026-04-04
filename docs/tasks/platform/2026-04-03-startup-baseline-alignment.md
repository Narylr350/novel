# Task

## Scope
Align the repository startup and environment baseline with the current source of truth so maintainers can trust the documented local and Docker entrypoints again.

## Out of Scope
- Backend business logic changes
- Frontend feature or UI changes
- Database schema or data import changes
- Full runtime smoke testing against a real MariaDB dataset

## Plan
1. Audit the current startup surfaces across `README.md`, `HELP.md`, Docker files, and compose files.
2. Update the root `Dockerfile` to match the backend Java baseline declared in `novel/pom.xml`.
3. Resynchronize root docs so frontend tooling, Docker entrypoints, and retired script guidance match the codebase.
4. Update platform context/engineering docs to reflect the corrected baseline and document remaining drift only where it still exists.
5. Run configuration-oriented validation for the changed startup surfaces and record the results.

## Design
This task treats `novel/pom.xml` and the actual compose files as the source of truth, then moves the docs and Docker build path to match them. The changes stay in the platform/configuration layer because the current stabilization roadmap prioritizes startup repeatability over feature work.

The implementation will keep the supported startup matrix intentionally small: direct local development with `novel` and `free-novel-web`, plus the four explicit compose files already present in the root. Legacy helper-script guidance will be reduced or retired rather than maintained in parallel.

## File Structure
- `Dockerfile`: repository root image build path for frontend bundle + backend packaged app
- `README.md`: maintainer-facing startup guide and high-level stack summary
- `HELP.md`: short root note for local environment assumptions and canonical doc handoff
- `docs/context/tech-stack.md`: canonical stack baseline and drift notes
- `docs/engineering/runtime-operations.md`: canonical runtime/startup guidance
- `docs/tasks/platform/2026-04-03-startup-baseline-alignment.md`: task record
- `docs/tasks/platform/INDEX.md`: platform task index

## Review
Pre-implementation review completed. Scope is intentionally limited to startup/configuration alignment because it directly supports the current stabilization roadmap and avoids accidental business-layer drift.

## Implementation
- Updated the root `Dockerfile` to use Node 20 for the frontend build stage and Java 21 for both the Maven build stage and the runtime image so the container path now matches `novel/pom.xml`.
- Rewrote `README.md` to describe the currently supported startup surfaces only: direct local development and the four explicit root compose files.
- Replaced the outdated `HELP.md` content with a short maintenance note that points back to the canonical docs instead of preserving stale environment instructions.
- Updated canonical platform docs to capture the corrected stack baseline, the lack of a root `docker-compose.yml`, and the remaining drift around published-image compose usage and Node engine warnings.

## Validation
- Backend build: `mvn -DskipTests package -Pdev` in `novel` completed successfully.
- Frontend dependency install: `npm ci` in `free-novel-web` completed successfully.
- Frontend build: `npm run build` in `free-novel-web` completed successfully with existing asset-size warnings and Vue deep-selector deprecation warnings.
- Docker build: `docker build -t freenovel-startup-baseline-check .` could not run because the local Docker daemon was unavailable (`npipe:////./pipe/dockerDesktopLinuxEngine` not found).
- Browser validation was not required because this task did not change frontend behavior.

## Documentation Sync
- Updated: `README.md`, `HELP.md`, `docs/context/tech-stack.md`, `docs/engineering/runtime-operations.md`, `docs/tasks/platform/2026-04-03-startup-baseline-alignment.md`, `docs/tasks/platform/INDEX.md`
- Checked with no change needed: `docs/context/project-overview.md`, `docs/context/architecture.md`, `docs/context/development-roadmap.md`

## Risks
- Docker/compose runtime still has one unresolved split: compose files pull the published `mattgideon/freenovel:v1.0.11-prod` image instead of building the local repository image.
- The Dockerfile change is syntax-level reviewed but not runtime-verified yet because Docker Desktop was not running during validation.
- Frontend maintenance on Node 24 is possible, but the current dependency set emits engine/deprecation warnings that make Node 20 LTS the safer baseline.

## Follow-ups
- Validate a full local backend/frontend startup against a real development database.
- Decide whether compose-based maintenance should keep using the published image or switch to a local `build:` path from this repository.
- Triage the frontend dependency warnings (`EBADENGINE`, deprecated deep selectors, large bundle size) into a separate stabilization task.
- Audit the custom frontend/backend auth-signature contract as the next cross-layer stabilization task.
