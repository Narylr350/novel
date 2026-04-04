# Architecture

## Repository Shape
FreeNovel uses a practical two-app layout with heavy supporting data and runtime directories:
- `novel`
- `free-novel-web`
- `sql`
- `app`

Documentation is centralized under `docs/` to reduce repeated context gathering by AI agents.

## Backend Shape
The backend is a large Spring Boot monolith.

Primary layers:
- `Controller`: HTTP entrypoints and route wiring
- `Service`: business orchestration, scheduling hooks, task coordination, and domain behavior
- `repository`: persistence access
- `entity` and DTO/CDO packages: persistence and transport models
- `Config`, `filter`, and `Interceptor`: runtime wiring, auth/signature, and scheduling
- `util` and `translator`: crawler, translation, export, storage, and helper logic

## Frontend Shape
The frontend is a Vue 3 SPA built with Vue CLI.

Primary layers:
- `src/main.js`: app bootstrap
- `src/router`: route definitions and auth guard behavior
- `src/api`: Axios setup and request signing
- `src/components`: route-level and reusable view components

## Confirmed Long-Term System Layers
The repository is being stabilized toward a layered runtime shape instead of one mixed feature surface:

1. `Reader Product Layer`
   - published default
   - login, browse, search, read, comment, favorites, notes, history, export, and Legado-facing reader features

2. `Precision Crawler Layer`
   - code-based site adapters for complex or high-value sites
   - login-aware crawling, synchronization, and precise cleanup paths

3. `Rule-Source Layer`
   - simpler, rule-driven source definitions inspired by Legado-style source ideas
   - lower-cost onboarding for simpler sites

4. `Translation Tool Layer`
   - maintainer-only translation jobs, glossary, memory, and cleanup workflows
   - intentionally decoupled from the default reader runtime

5. `OCR Fallback Layer`
   - maintainer-only fallback for image-first or anti-text sites
   - screenshot/page capture, OCR extraction, and AI cleanup when standard crawling is insufficient

These layers are complementary:
- the reader layer is the product surface
- precision crawler and rule-source layers are content-ingestion paths
- translation and OCR layers are maintainer-side processing tools

## Boundary Rules
- Keep one business goal per task, even when backend, frontend, docs, and config all need changes.
- Treat auth/signature flow as a cross-layer contract; do not change one side without inspecting the other.
- Treat scheduler, crawler, translation, upload, and sitemap logic as high-side-effect areas requiring explicit validation.
- Treat `sql/` imports and database mode changes as operational changes, not simple config edits.
- Treat rule-source, translation, and OCR work as maintainer-platform capabilities unless a task explicitly changes the reader-facing product.

## Documentation Architecture
- Stable context: `docs/context/`
- Engineering contracts: `docs/engineering/`
- Module history and status: `docs/tasks/`
- Validation standards: `docs/testing/`
- Legacy/supporting materials: `docs/archive/`
