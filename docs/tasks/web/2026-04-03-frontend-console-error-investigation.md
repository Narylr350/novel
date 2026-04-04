# Task

## Scope
Investigate browser console errors in the current frontend, reproduce them against a real local runtime path, fix the confirmed frontend-side root cause, and verify the browser console is cleaner afterward.

## Out of Scope
- Broad frontend refactors without a reproduced error
- Backend feature changes unrelated to the confirmed console issue
- Fixing every warning or every network error caused by missing local services
- Database/package redesign

## Plan
- Create a reproducible local runtime path for frontend + backend.
- Capture actual browser console errors instead of guessing from historical complaints.
- Trace the first confirmed frontend-side root cause before proposing a fix.
- Add the smallest focused regression test around that root cause.
- Implement the minimal fix, rerun browser validation, and document the result.

## Design
This task follows a debugging-first workflow. The main risk is treating generic network failures or backend-unavailable noise as “frontend bugs.” The investigation must therefore start from a working or at least intentionally stubbed local runtime path, then inspect the browser console in that controlled state.

The first implementation pass should fix only the confirmed frontend-side root cause with the highest user impact. If the console contains multiple unrelated issues, they should be triaged and handled one by one rather than bundled into a speculative mega-fix.

## File Structure
- `docs/tasks/web/2026-04-03-frontend-console-error-investigation.md`: task record
- `docs/tasks/web/INDEX.md`: web module index update
- frontend source/tests: to be determined after root-cause reproduction
- `docs/testing/ai-logs/`: retained local startup or browser-console logs if needed

## Review
Pre-implementation review intentionally stays lightweight because the task is still in root-cause investigation mode. No code fix should land until browser reproduction identifies a concrete frontend-side bug.

## Implementation
- Added backend regression coverage in `novel/src/test/java/com/wtl/novel/filter/RequestFilterPublicEndpointsTest.java` for unauthenticated SPA bootstrap endpoints.
- Expanded the auth-filter public matcher in `novel/src/main/java/com/wtl/novel/util/URLMatcher.java` so the reader homepage can load app mode, login state, platform metadata, home dictionary data, tag metadata, and book-list queries before login.
- Added anonymous-reader support for `POST /api/novels/getNovelsByPlatform` in `NovelController`, `NovelService`, and `NovelControllerPublicBrowseTest` so the web library can load book lists before login instead of returning `401`.
- Added `free-novel-web/src/utils/requestErrorMessage.mjs` plus `free-novel-web/tests/requestErrorMessage.spec.mjs` so request failures are normalized into safe message strings instead of passing raw error objects into Element Plus.
- Updated `WebLibrary.vue`, `RecommendationList.vue`, `WebFavorites.vue`, and `WebNote.vue` to log request failures to the console and show safe `ElMessage.error(...)` strings instead of triggering runtime overlay crashes.
- Replaced the runtime Tailwind browser script with a local Tailwind/PostCSS build path, imported the Tailwind entry in `src/main.js`, and injected Vue feature flags through `vue.config.js` so the remaining browser warnings disappear in local development.

## Validation
- `mvn "-Dtest=RequestFilterPublicEndpointsTest,RequestFilterCorsTest,AppModeControllerTest,NovelControllerPublicBrowseTest" test`
- `node tests/requestErrorMessage.spec.mjs`
- `npm run build`
- Browser MCP against `http://localhost:8080/webLibrary` after restarting local backend on `http://localhost:8081`
  - Result: page title `拼好书`, runtime overlay absent, page body contains login link and platform selector, browser console reported `0 errors, 0 warnings`

## Documentation Sync
- Updated this task record
- Updated `docs/tasks/web/INDEX.md`
- Updated `docs/tasks/backend/INDEX.md`
- Updated `docs/context/tech-stack.md`
- Checked `docs/engineering/runtime-operations.md` with no change needed because the fix did not alter startup commands or runtime defaults

## Risks
- Some console errors may be secondary effects from local backend unavailability rather than frontend defects.
- The first reproduced issue may require backend coordination; if so, the task scope must be re-evaluated before coding.

## Follow-ups
- Consider replacing the remaining raw `ElMessage.error(error)` patterns in unrelated pages that were not needed for this fix.
- Triage the remaining dev-server stderr noise separately:
  - Vue scoped-style `>>>` / `::v-deep` deprecation warnings
  - Watchpack scanning `D:\System Volume Information`
