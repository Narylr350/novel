# Task

## Scope
Embed the Vue reader frontend into the packaged Spring Boot jar so the beginner path no longer requires a separate frontend process, switch core reader navigation away from opening new tabs/windows, and normalize backend runtime directories so logs/temp/storage stay inside a predictable local runtime root.

## Out of Scope
- Reworking crawler, translation, OCR, or maintainer tooling
- Replacing the existing source-package SQL layout
- Converting the whole frontend to a different build system
- Broad cleanup of old utility/demo classes that are not part of the packaged runtime path

## Plan
- Add a new platform task record for packaged frontend delivery and runtime directory normalization.
- Wire the backend Maven build to build the Vue app and copy `free-novel-web/dist` into the jar static resources.
- Replace reader-facing `_blank` navigation with in-app route pushes on the core reading flows.
- Normalize runtime/log/temp/storage paths through one runtime base directory and make the beginner backend launcher pin that path explicitly.
- Rebuild the backend jar, validate backend-only startup, validate browser behavior, and sync the distribution package plus docs.

## Design
The current beginner path still requires two long-running processes because the packaged backend jar does not contain the Vue SPA assets. The repository already has `SpaController`, so the missing layer is build-time asset bundling, not runtime route forwarding.

This task bundles the frontend into the backend jar by:
- building the Vue app during Maven packaging
- copying the built frontend assets into Spring Boot static resources
- keeping the frontend API base URL same-origin friendly so packaged deployments can call `/api/**` directly

Reader-facing navigation should stay inside the SPA for the default reading flow. Core routes such as:
- web library -> novel detail
- favorites -> novel detail
- novel detail -> chapter detail / continue reading
- rule-source library -> novel detail when a local novel exists

should use router navigation instead of `window.open(..., '_blank')`.

Runtime directories should no longer depend on Linux container defaults for the packaged beginner path. One runtime base directory should drive:
- upload temp directory
- upload storage directory
- file logs

The backend launch script should set this base directory explicitly relative to the package root and create the directory structure up front so logs and temp files land in one place.

## File Structure
- `docs/tasks/platform/2026-04-09-packaged-frontend-jar-and-runtime-normalization.md`: task record
- `docs/tasks/platform/INDEX.md`: platform progress
- `docs/tasks/web/INDEX.md`: web progress note
- `docs/tasks/backend/INDEX.md`: backend/runtime progress note
- `novel/pom.xml`: frontend build integration and static asset copy into the jar
- `novel/src/main/resources/application-dev.properties`: runtime base-dir normalization for local dev
- `novel/src/main/resources/application-prod.properties`: runtime base-dir normalization for packaged/prod startup
- `free-novel-web/src/components/WebLibrary.vue`: in-app reader navigation
- `free-novel-web/src/components/WebFavorites.vue`: in-app reader navigation
- `free-novel-web/src/components/WebLibraryNp.vue`: in-app reader navigation for local novels
- `free-novel-web/src/components/NovelDetail.vue`: in-app reader navigation for chapter flow
- `scripts/start-reader-backend.ps1`: set and prepare packaged runtime directory root
- `scripts/build-source-package-distribution.ps1`: stage the new packaged jar/runtime layout into distribution output
- `docs/engineering/runtime-operations.md`: packaged backend-only startup path
- `docs/engineering/小白使用说明-源码包.md`: beginner flow no longer requires separate frontend startup
- `README.md`: root routing note for backend-only packaged startup

## Review
Pre-implementation review was completed in conversation.

Confirmed decisions:
- packaged beginner usage should avoid a separate frontend process
- reader-facing clicks should stay inside one SPA tab instead of spawning new windows
- runtime logs/temp/storage should be pinned to one local runtime directory instead of drifting to machine-specific defaults

## Implementation
Implemented the packaged backend-only delivery path and aligned the reader-facing click behavior with a single-page app flow.

Backend packaging and runtime changes:
- `novel/pom.xml`
  - builds `free-novel-web` during `prepare-package`
  - runs `npm ci` and `npm run build`
  - copies `free-novel-web/dist` into `target/classes/static`
- `application-dev.properties` and `application-prod.properties`
  - now derive temp/storage/log paths from one `app.runtime.base-dir`
- `scripts/start-reader-backend.ps1`
  - now creates `app\logs`, `app\tmp`, and `app\file`
  - pins `APP_RUNTIME_BASE_DIR`
  - supports explicit `-ServerPort`
  - supports both packaged-jar locations:
    - `release\backend\free-novel.jar`
    - legacy `novel\free-novel.jar`

Reader-facing navigation changes:
- `free-novel-web/src/components/WebLibrary.vue`
- `free-novel-web/src/components/WebFavorites.vue`
- `free-novel-web/src/components/WebLibraryNp.vue`
- `free-novel-web/src/components/NovelDetail.vue`
- `free-novel-web/src/components/RecommendationList.vue`

These routes now use `this.$router.push(...)` for the normal reader flow instead of opening `_blank` tabs/windows. External upstream links remain external.

Distribution/package sync:
- copied the rebuilt backend jar to `release\backend\free-novel.jar`
- synced the updated backend launcher into `D:\FreeNovel\01\scripts\start-reader-backend.ps1`
- synced the rebuilt jar into `D:\FreeNovel\01\novel\free-novel.jar`
- changed `D:\FreeNovel\01\04-启动前端.cmd` to stop launching a separate frontend process for normal usage and instead open `http://localhost:8081`
- updated `D:\FreeNovel\安装说明.txt` and `D:\FreeNovel\目录说明.txt` so the packaged user path is backend-only

## Validation
Completed validation:

- `mvn -DskipTests package`
  - result: success
  - confirmed frontend assets copied into the Spring Boot jar
- `npm ci`
- `npm run build`
  - rerun sequentially after a failed parallel build attempt
  - result: success
- jar content verification:
  - `BOOT-INF/classes/static/index.html`
  - `BOOT-INF/classes/static/js/app...`
  - `BOOT-INF/classes/static/css/app...`
- packaged backend HTTP smoke tests from source repo:
  - `http://localhost:19086/api/auth/app-mode` -> `200 {"mode":"reader"}`
  - `http://localhost:19086/` -> `200` with packaged `index.html`
  - `http://localhost:19086/novelDetail/892` -> `200` with packaged `index.html`
- distribution-package HTTP smoke tests from `D:\FreeNovel\01`:
  - `http://localhost:19087/api/auth/app-mode` -> `200 {"mode":"reader"}`
  - `http://localhost:19087/` -> `200` with packaged `index.html`
  - `http://localhost:19087/novelDetail/892` -> `200` with packaged `index.html`
- reader navigation grep verification:
  - no `_blank` remains in the patched core reader components
  - only the external upstream `novelpia.com` link remains external

Validation logs retained:
- `docs/testing/ai-logs/packaged-backend-19086.out.log`
- `docs/testing/ai-logs/packaged-backend-19086.err.log`

Browser MCP was unavailable in this session, so frontend acceptance used HTTP smoke checks plus source verification instead of interactive browser navigation.

## Documentation Sync
- Updated: `docs/tasks/platform/2026-04-09-packaged-frontend-jar-and-runtime-normalization.md`
- Updated: `docs/tasks/platform/INDEX.md`
- Updated: `docs/tasks/web/INDEX.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `README.md`
- Updated: `docs/engineering/runtime-operations.md`
- Updated: `docs/engineering/小白使用说明-源码包.md`
- Checked with no change needed: `docs/context/development-roadmap.md`, `docs/context/architecture.md`

## Risks
- Maven-based frontend packaging adds build time and depends on Node/npm being available on the packaging machine.
- The source repository currently lacks some previously documented root-level beginner wrapper files; this task synced the active packaged distribution directly, but the repository-level distribution assembly path still needs a cleanup pass.
- Some maintainer-only views still use new-tab or external-link behavior unless they are intentionally migrated later.
- Runtime directory normalization only covers the packaged/default path, not every legacy helper utility under `novel/src/main/java/com/wtl/novel/util`.

## Follow-ups
- Simplify the beginner source package by retiring the standalone frontend launcher once the packaged path proves stable.
- Continue auditing legacy hardcoded utility paths that are outside the default runtime path if they become active again.
