# Task

## Scope
Align the local compose maintenance path with the current repository source so `docker-compose.local-*` defaults run code built from this workspace instead of silently pulling a published image.

## Out of Scope
- Changing external compose deployment strategy
- Reworking backend/runtime environment variables beyond what local source builds need
- Docker runtime smoke tests against a live daemon
- Business logic changes in backend or frontend

## Plan
- Record the local compose/source alignment scope and file boundaries.
- Update the local compose files to build from the repository `Dockerfile` instead of pulling the published image by default.
- Keep the external compose files untouched so remote/image deployment remains available as a separate path.
- Sync README and runtime docs so the canonical local compose command reflects the source-build path.
- Validate the changed compose files with `docker compose config` and record any residual risks.

## Design
The repository already distinguishes between local and external compose entrypoints. The cleanest alignment is:
- `docker-compose.local-single.yml` and `docker-compose.local-dual.yml`: source-build local maintenance path
- `docker-compose.external-single.yml` and `docker-compose.external-dual.yml`: published-image deployment path

This keeps the user-visible startup matrix simple and avoids inventing new flags or conditional compose logic. Local maintainers get code they can actually modify, while external/server deployments retain the existing image-oriented behavior.

## File Structure
- `docker-compose.local-single.yml`: local single-database source-build compose path
- `docker-compose.local-dual.yml`: local dual-database source-build compose path
- `README.md`: canonical local compose command and source/image path explanation
- `docs/engineering/runtime-operations.md`: canonical runtime note about local compose building current source
- `docs/tasks/platform/INDEX.md`: platform task index update
- `docs/tasks/platform/2026-04-03-local-compose-source-alignment.md`: task record

## Review
Pre-implementation review stayed lightweight. This task directly follows the startup-baseline task and closes one of the explicit residual risks recorded there.

## Implementation
- Updated `docker-compose.local-single.yml` and `docker-compose.local-dual.yml` so the `novel-app` service now builds from the repository root `Dockerfile` instead of defaulting to the published image.
- Kept the local compose files tagged with local image names so repeat runs can reuse the built image cache while still tracking current source when invoked with `--build`.
- Left the external compose files untouched so image-oriented deployment remains available as a separate path.
- Updated maintainer-facing docs to make the local source-build path explicit and to keep the external image-based path clearly separated.

## Validation
- `docker compose -f docker-compose.local-single.yml config`
- `docker compose -f docker-compose.local-dual.yml config`
- Both local compose files resolve `novel-app.build.context` to the current repository root and keep the expected environment/volume wiring.

## Documentation Sync
- Updated: `README.md`
- Updated: `docs/engineering/runtime-operations.md`
- Updated: `docs/context/tech-stack.md`
- Updated: `docs/tasks/platform/INDEX.md`
- Updated: `docs/tasks/platform/2026-04-03-local-compose-source-alignment.md`
- Checked with no change needed: `HELP.md`, `docs/context/development-roadmap.md`, `docs/tasks/platform/2026-04-03-startup-baseline-alignment.md`

## Risks
- Docker daemon availability is still outside repository control, so validation may be limited to compose config resolution.
- Source-based local images will take longer to build than the previous pull-only path.
- `docker compose config` reports that the top-level `version` field is obsolete in the local compose files. It does not break resolution, but it should be cleaned up in a future compose hygiene pass.

## Follow-ups
- Decide whether to add a dedicated local override file for faster maintainer iteration later.
- Revisit external compose defaults after the lite database/bootstrap path is redesigned.
