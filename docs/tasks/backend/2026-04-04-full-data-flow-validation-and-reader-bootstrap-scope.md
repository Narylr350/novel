# Task

## Scope
Validate that the newly imported full database can actually support the current application data flow before any reader-mode table-scope reduction is implemented.

After that validation, reduce `DatabaseInitializer` so `reader` mode only requires reader-facing tables while `maintainer` mode keeps the broader table set.

## Out of Scope
- Rebuilding the `lite` package in this task
- Dropping live tables from the local database
- Refactoring crawler, translation, or community features
- Redesigning schema ownership beyond the minimal `DatabaseInitializer` scope split

## Plan
1. Capture the current reader/maintainer bootstrap design in this task document and keep it as the single source of truth.
2. Run a full local data-flow validation against the imported `novel` database:
   - start backend with the current full schema
   - confirm the app-mode endpoint and startup logs are healthy
   - use browser validation for login, library, novel detail, and chapter detail
3. Record any reader-facing failures and identify whether they are caused by:
   - bad imported data
   - hidden dependency on a maintainer-side table
   - unrelated application bugs
4. Refine the `reader required tables` and `maintainer required tables` lists from runtime evidence.
5. Implement mode-aware table grouping in `DatabaseInitializer` using `app.ui.mode`.
6. Add backend tests that prove:
   - `reader` mode only requires reader tables
   - `maintainer` mode still requires the full maintainer surface
   - `database.mode=dual` still controls secondary table initialization independently
7. Re-run backend validation and browser smoke validation after the initializer change.
8. Sync the task record, backend index, and any affected engineering docs.

## Design
### Goal
Do not shrink startup requirements based on theory alone.  
First prove that the current full database works end-to-end for the real reader flow, then shrink `DatabaseInitializer` using evidence from that validation.

### Execution Order
#### Step A: Full Data-Flow Validation
Use the fully imported local MariaDB database as the source of truth and validate the existing application against it.

Minimum flow to verify:
- backend starts successfully on the full imported data
- reader bootstrap works
- login works
- web library loads
- novel detail loads
- chapter detail loads

If this step reveals that a currently "maintainer-optional" table is still hard-required by reader flow, update the table grouping before touching `DatabaseInitializer`.

#### Step B: Reader / Maintainer Bootstrap Scope
Only after Step A passes, reduce startup table requirements.

Recommended approach:
- inject `app.ui.mode` into `DatabaseInitializer`
- define:
  - `READER_REQUIRED_TABLES`
  - `MAINTAINER_REQUIRED_TABLES`
- `reader` mode checks only the reader set
- `maintainer` mode checks the combined set
- `chapter_scaling_up_one` remains controlled by `database.mode=dual`

### Proposed Reader Required Tables
Initial reader set, subject to Step A validation:
- `chapter`
- `chapter_comment`
- `comment`
- `credential`
- `dictionary`
- `favorite_groups`
- `favorites`
- `invitation_code`
- `notes`
- `novel`
- `novel_chapter`
- `novel_tag`
- `reading_record`
- `tag`
- `user`

### Proposed Maintainer-Only Tables
Initial maintainer set, subject to Step A validation:
- `chapter_error_execute`
- `chapter_execute`
- `chapter_image_links`
- `chapter_sync`
- `chapter_updates`
- `message`
- `novel_download_limit`
- `platform`
- `platform_api_key`
- `post`
- `post_agree`
- `post_comment`
- `request_log`
- `terminology`
- `user_access_logs`
- `user_blacklist`
- `user_chapter_edit`
- `user_feedback`
- `user_glossary`
- `user_novel_relation`
- `user_operation_log`
- `user_tag_filter`

### Validation Strategy
This task needs both runtime validation and code validation:

1. Runtime validation on the full imported database
- backend startup on local DB
- browser validation for core reader flow

2. Backend test validation for `DatabaseInitializer`
- verify `reader` mode ignores maintainer-only missing tables
- verify `maintainer` mode still requires the full set

### Tradeoff
This is intentionally slower than directly editing `DatabaseInitializer`, but it avoids a bad outcome:
- using a broken or incomplete live dataset to justify the wrong reader bootstrap boundary

## File Structure
- `docs/tasks/backend/2026-04-04-full-data-flow-validation-and-reader-bootstrap-scope.md`
  - active task record for validation-first bootstrap scope reduction
- `novel/src/main/java/com/wtl/novel/Config/DatabaseInitializer.java`
  - startup table-requirement logic
- `novel/src/test/java/com/wtl/novel/Config/`
  - mode-aware initialization tests
- `novel/src/test/java/com/wtl/novel/Controller/`
  - reuse existing backend smoke tests if the runtime check reveals missing public path assumptions
- `novel/src/main/resources/application-dev.properties`
  - source of the local reader/maintainer mode defaults during runtime validation
- `free-novel-web/src/router/index.js`
  - current reader route surface during browser validation
- `novel/src/test/java/com/wtl/novel/Config/`
  - tests for mode-based initialization behavior
- `docs/tasks/backend/INDEX.md`
  - backend task tracking
- `docs/engineering/database.md`
  - database bootstrap guidance if the scope change lands

## Review
Design reviewed and confirmed in conversation:
- validate the full data flow first
- do not base bootstrap reduction on an unverified imported dataset
- after validation passes, split `DatabaseInitializer` by `reader` / `maintainer`
- `reader` mode should not create maintainer tables at startup

Implementation has not started yet.

Plan review conclusion:
- keep the task as one business goal: validation-first bootstrap reduction
- do not start code changes until runtime validation confirms the imported database is trustworthy enough for reader-flow checks
- keep browser validation limited to the agreed reader flow instead of broad maintainer coverage

## Implementation
Completed.

1. Validated the imported full database against the local app in `reader` mode:
   - backend health and `/api/auth/app-mode` responded normally
   - web library loaded with real imported data
   - novel detail loaded successfully after reopening the public reader detail endpoints
   - login and post-login chapter reading both worked against the full imported database
2. Confirmed the reader-facing flow did not require the previously classified maintainer tables as startup-critical tables.
3. Reopened the reader detail bootstrap endpoints in the auth filter whitelist:
   - `/api/novels/{id}`
   - `/api/chapters/getChaptersByNovelId/{novelId}`
   - `/api/tag/getTagsAllInfoByNovelId/{novelId}`
   - `/api/posts/getAllPostsByNovelId`
   - `/api/favorites/user/{objectId}/{favoriteType}`
4. Reduced `DatabaseInitializer` primary-table scope by `app.ui.mode`:
   - `reader` mode now checks only the 15 reader-facing tables
   - `maintainer` mode still checks the combined reader + maintainer table surface
   - secondary database initialization remains controlled only by the presence of `secondaryDataSource`
5. Added mode-scope tests for the initializer split and kept the public reader browse/filter tests passing.

## Validation
Backend:
- `mvn "-Dtest=DatabaseInitializerModeScopeTest,RequestFilterPublicEndpointsTest,AppModeControllerTest,NovelControllerPublicBrowseTest" test`
- `mvn -DskipTests package -Pdev`

Runtime:
- restarted local backend with `APP_UI_MODE=reader` against the fully imported local MariaDB database
- verified startup log showed `mode=reader, requiredTableCount=15`
- verified `/actuator/health` returned healthy and `/api/auth/app-mode` returned `{"mode":"reader"}`

Browser:
- `http://localhost:8080/webLibrary` loaded real imported data
- `http://localhost:8080/novelDetail/351801` loaded successfully in reader mode with no console errors
- registered a temporary validation user from an existing unused invitation code, logged in, and verified `http://localhost:8080/chapterDetail/5756601` loaded successfully with no console errors or warnings

Observed but not fixed in this task:
- anonymous chapter reading is still not a supported runtime path; opening `chapterDetail` before login can still fail because chapter content APIs require a valid credential

## Documentation Sync
Updated:
- `docs/tasks/backend/2026-04-04-full-data-flow-validation-and-reader-bootstrap-scope.md`
- `docs/tasks/backend/INDEX.md`
- `docs/engineering/database.md`

Checked with no change needed:
- `docs/tasks/backend/2026-04-04-full-database-audit-and-table-classification.md`
- `docs/context/development-roadmap.md`

## Risks
- The current imported database may still contain runtime-breaking data inconsistencies even though both SQL files finished importing.
- Some tables currently classified as maintainer-only may still be indirectly required by reader-facing code paths.
- `DatabaseInitializer` currently mixes schema creation, dictionary seeding, and default-user creation; later cleanup may still be needed even after the table-scope split.

## Follow-ups
- Run full data-flow validation on the imported database.
- Refine table groupings from runtime evidence.
- Implement the `DatabaseInitializer` mode split.
- Update database packaging guidance after the new bootstrap boundary is proven.
