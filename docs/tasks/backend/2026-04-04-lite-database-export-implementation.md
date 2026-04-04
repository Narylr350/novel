# Task

## Scope
Implement the first repository-owned `lite` database export/import tooling so the project can generate a reader-focused bootstrap package from the healthy local database instead of depending on the legacy full dump.

## Out of Scope
- Full-package splitting for `terminology`, `chapter_execute`, or other maintainer datasets
- Flyway migration adoption
- OCR, crawler, or translation-system refactors
- Docker-based automatic database seeding
- Deleting maintainer tables from the imported full database

## Plan
- Add a backend task record for the executable `lite` package work and register it in the backend module index.
- Create a shared PowerShell helper module plus Pester coverage for the package defaults, candidate SQL, and demo-account seed generation.
- Add a candidate-novel probe script and export/import scripts for `schema.sql`, `seed-system-lite.sql`, `seed-reader-demo.sql`, and the one-shot `import-lite` path.
- Validate the scripts against the healthy local MariaDB instance and record the first provisional demo-novel shortlist.
- Sync the backend/database docs with the new export workflow and command entrypoints.

## Design
This task is the executable follow-up to [2026-04-03-lite-database-package-design.md](./2026-04-03-lite-database-package-design.md). The design choices already confirmed in conversation stay in force:

- `schema.sql` is exported from the healthy database, not reconstructed from `TableSqlRepository`.
- The package is split into three files:
  - `schema.sql`
  - `seed-system-lite.sql`
  - `seed-reader-demo.sql`
- The default demo account is a fixed, reader-only account rather than an admin account or random bootstrap user.
- Demo novels come from the healthy local database and must be screened for reader-chain completeness before inclusion.

### Demo Account Baseline
The current schema stores login identity in `user.email`, not `user.username`. The first `lite` seed therefore ships a deterministic reader-only email identity:

- email: `demo_reader@lite.local`
- password: `ReaderPass1`
- purpose: first-login validation only, not administrator bootstrap

The system seed will preload:
- the full `dictionary` table for runtime safety in the first pass
- one invitation-code row reserved for the demo account
- one `user` row with a known bcrypt password hash

No persistent `credential` rows are preloaded; login should mint them at runtime.

### Schema Boundary
The first schema export should include the reader-facing tables required by the current reader-mode bootstrap plus the optional image-link table used by some books:

- `chapter`
- `chapter_comment`
- `chapter_image_links`
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

This keeps the package focused on reader behavior while avoiding the maintainer-only tables that were explicitly removed from `reader` bootstrap.

### Candidate-Novel Strategy
The candidate probe must score novels by:
- chapter count
- `novel_chapter` count
- mismatch between chapter and TOC counts
- optional image-link count
- optional comment count

The shortlist rule for the first package is conservative:
- prefer books with both chapters and `novel_chapter` rows
- prefer books where `chapter_count == toc_count`
- avoid books that only have raw chapter rows but no directory chain

### Reader Demo Export Boundary
The first `seed-reader-demo.sql` export will dump:
- `novel`
- `novel_tag`
- `tag`
- `novel_chapter`
- `chapter`
- `chapter_image_links`

Comments remain optional in the first executable package. The goal is “browse, open detail, load directory, read chapters,” not “replay historical community state.”

## File Structure
- `docs/tasks/backend/2026-04-04-lite-database-export-implementation.md`: executable task record for this phase
- `docs/tasks/backend/INDEX.md`: backend module status
- `docs/engineering/database.md`: canonical database export/import workflow
- `scripts/lib/LitePackageTools.psm1`: shared PowerShell defaults, SQL builders, and dump-spec helpers
- `scripts/tests/LitePackageTools.Tests.ps1`: Pester coverage for the helper module
- `scripts/find-lite-candidate-novels.ps1`: DB probe for demo-novel screening
- `scripts/export-lite-schema.ps1`: schema-only export entrypoint
- `scripts/export-lite-system-seed.ps1`: minimal system/demo-account export entrypoint
- `scripts/export-lite-reader-demo.ps1`: chosen-novel export entrypoint
- `scripts/import-lite.ps1`: one-path local import wrapper

## Review
Pre-implementation review was completed in conversation and then revalidated against the imported healthy database. Two important corrections were applied before implementation:

- the actual auth schema uses `user.email` plus `user.password`; there is no `username` field in `user`
- `credential` stores runtime tokens, so it stays as schema-only in the first `lite` seed

## Implementation
Implemented the first runnable `lite` package toolchain under `scripts/`:

- Added `scripts/lib/LitePackageTools.psm1` to centralize:
  - reader-mode schema table boundaries
  - candidate-novel probe SQL
  - per-table dump specs for demo-book export
  - the deterministic demo-account seed payload
  - `mysqldump.exe` and `mariadb.exe` path resolution
- Added `scripts/tests/LitePackageTools.Tests.ps1` for Pester coverage of the helper module.
- Added `scripts/find-lite-candidate-novels.ps1` to screen usable demo books from the healthy local database.
- Added `scripts/export-lite-schema.ps1` to export the reader-focused schema directly from the healthy database.
- Added `scripts/export-lite-system-seed.ps1` to export the first-pass `dictionary` payload and append the fixed demo reader account:
  - email: `demo_reader@lite.local`
  - password: `ReaderPass1`
- Added `scripts/export-lite-reader-demo.ps1` to export selected demo novels with their tags, TOC, chapters, and image-link rows.
- Added `scripts/import-lite.ps1` to wrap the repository-supported import path and load the three generated `lite` package files in order.

### First Provisional Demo Books
The first candidate probe showed that many large novels have no usable `novel_chapter` rows, so the provisional `lite` set was deliberately restricted to books with both chapter payloads and TOC rows:

- `353498` / `绿皮`
- `353474` / `祸世末子的回归`
- `353487` / `亲手培养的女团`

### Package Output
Validated output files were generated under `sql/lite/`:
- `schema.sql`
- `seed-system-lite.sql`
- `seed-reader-demo.sql`

### Reader-Lite Runtime Hardening
Validated the generated `novel_lite_validation` database by running the full backend and web stack against it, then tightened the reader-mode runtime assumptions until the chapter-detail reading flow was stable.

Backend/runtime changes:
- `DatabaseInitializer` already limited reader bootstrap to the 15 core reader tables; this runtime pass confirmed that assumption against the actual lite database.
- Added reader-mode fallbacks so missing maintainer tables no longer break chapter-detail loading:
  - `request_log`
  - `user_access_logs`
  - `user_chapter_edit`
  - `chapter_sync`
  - `user_glossary`
- Updated `ChapterController` so empty chapter content is returned as an empty string instead of encrypting an empty payload into a misleading base64 blob.

Frontend/runtime changes:
- Added `free-novel-web/src/utils/chapterDetailPayloads.mjs` and `free-novel-web/src/utils/chapterCrypto.mjs` to normalize chapter-version requests, note payloads, and chapter-content decryption.
- Updated `free-novel-web/src/components/ChapterDetail.vue` to:
  - route version/original chapter requests correctly in reader mode
  - tolerate empty note payloads
  - use the dedicated chapter decryption helper
  - remove leftover debug `console.log(...)` noise once the page was stable

This work confirmed that the first `lite` package is not only importable, but also runnable through the logged-in chapter reading flow.

## Validation
Completed validation:

- `Invoke-Pester -Path .\scripts\tests\LitePackageTools.Tests.ps1`
- `.\scripts\find-lite-candidate-novels.ps1 -Limit 10`
- `.\scripts\export-lite-schema.ps1`
- `.\scripts\export-lite-system-seed.ps1`
- `.\scripts\export-lite-reader-demo.ps1 -NovelIds 353498,353474,353487`
- `.\scripts\import-lite.ps1 -LiteDirectory .\sql\lite -Database novel_lite_validation`

Database verification against `novel_lite_validation`:
- `novel`: `3`
- `chapter`: `1187`
- `novel_chapter`: `1187`
- `dictionary`: `66`
- `user`: `1`
- `chapter_image_links`: `145`

Backend HTTP smoke tests against the healthy source database:
- `GET /api/novels/353498`
- `GET /api/novels/353474`
- `GET /api/novels/353487`
- `GET /api/chapters/getChaptersByNovelId/353498`

Key findings from validation:
- `mysqldump.exe` must be invoked with `--column-statistics=0` against MariaDB.
- `reader-demo` exports that use subqueries in `--where` must also disable table locks and run in a transaction (`--single-transaction --skip-lock-tables`).
- The first provisional demo-book set produces a self-consistent TOC/chapter package in the validation database.

### Runtime Validation Against `novel_lite_validation`
Additional backend/frontend validation completed after the initial export/import phase:

- `mvn "-Dtest=ChapterControllerReaderModeTest,ChapterServiceReaderModeTest,UserAccessLogServiceReaderModeTest,RequestLogServiceReaderModeTest,PlatformServiceReaderModeTest,UserTagFilterServiceReaderModeTest,PostServiceReaderModeTest,RequestFilterPublicEndpointsTest,NovelControllerPublicBrowseTest,AppModeControllerTest" test`
- `mvn -DskipTests package`
- `node .\tests\chapterDetailPayloads.spec.mjs`
- `node .\tests\signatureCrypto.spec.mjs`
- `npm run build`

Browser MCP validation against:
- web: `http://localhost:8080`
- backend: `http://localhost:8081`
- database: `novel_lite_validation`
- route: `http://localhost:8080/chapterDetail/6006108`

Observed result:
- `/api/chapters/findAllContentVersion/6006108` -> `200`
- `/api/notes/chapter/6006108` -> `200`
- `/api/chapters/getChapterByIdApi/6006108` -> `200`
- browser console -> `0 errors / 0 warnings / 0 logs`

Key findings from the runtime pass:
- The first failure chain was caused by hidden maintainer-table dependencies, not by broken lite exports.
- `chapter 6006108` stores empty content in both the full and lite databases, which exposed a real API contract bug: encrypting an empty payload caused the frontend to treat a valid empty chapter as a decryption failure.
- The generated lite package is now validated through a real logged-in chapter-detail flow, not only through import counts and list/detail API smoke tests.

## Documentation Sync
- Updated: `docs/tasks/backend/2026-04-04-lite-database-export-implementation.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `docs/engineering/database.md`
- Updated: `docs/tasks/web/INDEX.md`
- Checked with no change needed: `docs/context/development-roadmap.md`
- Checked with no change needed: `docs/context/architecture.md`
- Checked with no change needed: `docs/context/project-overview.md`

## Risks
- The first-pass system seed still ships the full `dictionary` table, which includes crawler/translation-era keys. That is acceptable for runtime safety now but should be trimmed later.
- The current `lite` demo account uses a fixed, documented password and is suitable only for bootstrap/demo environments until the user changes it.
- The provisional book shortlist was validated through public detail/TOC APIs, not through a full app boot against `novel_lite_validation`; a later step should run the backend against that validation database directly.
- Some otherwise attractive novels have large `chapter` payloads but no `novel_chapter` rows, which would break the reader directory chain in `lite`.
- Some demo-book selections may still reveal hidden dependencies after export, especially around image links or detail-page metadata.

## Follow-ups
- Decide whether the provisional 3-book set should become the first committed/released `lite` baseline or be swapped for a more diverse trio.
- Trim `dictionary` to a truly reader-minimal subset after the first package is proven runnable.
- Add packaging guidance for distributing the generated `sql/lite/` artifacts outside the repository.
- Start the separate `full` split work only after `lite` is reproducible.
- Decide whether empty-content chapters should stay readable as blank chapters or be surfaced with a clearer “no text available” reader UX.
