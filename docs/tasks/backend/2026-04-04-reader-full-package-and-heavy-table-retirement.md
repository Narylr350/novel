# Task

## Scope
Define and prepare the first executable `reader full` database packaging path so normal self-hosted users can import real reading content after validating deployment with `lite`, while removing heavy maintainer-only tables from the default distribution model.

This task establishes:
- `lite` as the deployment-validation package
- `reader full` as the normal-user reading package
- soft retirement of heavy maintainer tables from default distribution
- `chapter` partitioning by `chapter.id` range for resumable import

## Out of Scope
- Dropping live tables from the imported full database
- Reworking the translation system or deleting `terminology` at runtime
- OCR, crawler, or rule-source implementation work
- Flyway adoption
- Docker-based large-package bootstrap
- Browser-facing feature changes

## Plan
- Add a backend task record for the `reader full` package line and register it in the backend module index.
- Record the confirmed package model: `lite` for validation, `reader full` for real reading, and heavy maintainer tables as default-distribution retired.
- Implement reader-full export scripts for metadata, `chapter_image_links`, and partitioned `chapter` payloads.
- Implement reader-full import and verification scripts with resumable chapter-part handling.
- Validate the package against a fresh local database and confirm counts/progress tracking behavior.
- Sync database/distribution docs after the scripts and validation land.

## Design
This task is the direct follow-up to the now-stable `lite` packaging baseline. The key product correction confirmed in conversation is:

- `lite` is not the final user reading package
- `lite` exists to validate deployment quickly before spending time on large content imports
- normal users still need a real reading package after `lite` passes

### Distribution Model
The repository distribution model is now intentionally split into three layers:

1. `lite`
   - deployment validation package
   - small schema + system seed + three demo books
   - fast import, fast runtime verification

2. `reader full`
   - normal-user reading package
   - real novel metadata, TOC, chapters, and image-link support
   - resumable import path instead of a single giant SQL file

3. `maintainer heavy`
   - heavy maintainer-side historical tables
   - no longer part of the default end-user distribution path
   - kept only for historical retention, migration, or explicit maintainer needs

### Heavy Table Retirement Boundary
The following tables are now on the soft-retirement path for default distribution:

- `terminology`
- `chapter_execute`
- `chapter_error_execute`

Soft retirement means:
- remove them from default docs and default package flows
- keep runtime/database compatibility for now
- preserve the option to export them separately if needed
- do not make end users import them

This task may also identify nearby reader-irrelevant heavy tables, but the first explicit retirement target remains the three tables above.

### Reader Full Package Structure
The first `reader full` layout should be:

- `schema.sql`
- `seed-system.sql`
- `reader-content-meta.sql`
- `reader-chapter-image-links.sql`
- `chapter-part-0001.sql`
- `chapter-part-0002.sql`
- `...`

Package intent:
- `schema.sql`: reader-facing schema, shared with the validated package model
- `seed-system.sql`: minimal runtime seed for the full reader package
- `reader-content-meta.sql`: `novel`, `novel_chapter`, `tag`, `novel_tag`, and other confirmed reader metadata
- `reader-chapter-image-links.sql`: image-link support separated from the main chapter partitions
- `chapter-part-*`: chapter payload partitions by `chapter.id`

### Chapter Partition Strategy
`chapter` remains part of the real user reading package, but it can no longer be distributed as a single giant import file.

Confirmed partitioning rules:
- split by `chapter.id` range
- target about `100000` chapters per part
- optimize for lower failure/retry cost, not minimal part count

Why this boundary:
- deterministic ranges are easier to verify than size-based chunks
- restart logic can resume from a known part
- range-based validation is simpler and more trustworthy

### Reader Full Script Surface
The expected first executable script set is:

- `scripts/export-reader-full-meta.ps1`
- `scripts/export-reader-full-image-links.ps1`
- `scripts/export-reader-full-chapter-parts.ps1`
- `scripts/import-reader-full.ps1`
- `scripts/verify-reader-full.ps1`

Expected responsibilities:
- metadata export stays separate from chapter payload export
- image links stay separate from chapter partitions
- import orchestrator installs files in the right order
- verify script reports counts, part continuity, and resumable progress state

### Import Flow
The intended normal-user flow after this task is:

1. install `lite`
2. verify that the app starts and the demo books are readable
3. import `reader full`
4. skip retired maintainer-heavy packages unless explicitly needed

That flow prevents the old failure mode where users spend hours importing a giant SQL file only to discover the app or environment is broken.

## File Structure
- `docs/tasks/backend/2026-04-04-reader-full-package-and-heavy-table-retirement.md`: active task record for the reader-full package line
- `docs/tasks/backend/INDEX.md`: backend module state
- `docs/engineering/database.md`: canonical package/distribution model once implementation lands
- `docs/engineering/reader-lite-quickstart.md`: may need cross-links once the reader-full flow exists
- `scripts/lib/LitePackageTools.psm1`: likely shared defaults/helpers or extraction point for package-common logic
- `scripts/export-reader-full-meta.ps1`: export reader metadata
- `scripts/export-reader-full-image-links.ps1`: export image-link support
- `scripts/export-reader-full-chapter-parts.ps1`: export partitioned chapter payloads
- `scripts/import-reader-full.ps1`: orchestrate reader-full import
- `scripts/verify-reader-full.ps1`: verify package completeness and import progress

## Review
Pre-implementation review was completed in conversation. Two important product corrections were confirmed before script work starts:

- `lite` is a validation package, not the long-term reading package for normal users
- `chapter_image_links` must stay in the normal-user reading package because omitting it would degrade the reading experience for some books

The heavy-table strategy was also explicitly narrowed:
- remove heavy maintainer tables from default distribution first
- keep runtime/database compatibility for now
- defer structural redesign or deletion to a later phase

## Implementation
Implemented the first executable `reader full` script surface:

- Added `scripts/export-reader-full-meta.ps1`
  - exports `reader-content-meta.sql`
  - current metadata tables:
    - `novel`
    - `novel_tag`
    - `tag`
    - `novel_chapter`
- Added `scripts/export-reader-full-image-links.ps1`
  - exports `reader-chapter-image-links.sql`
- Added `scripts/export-reader-full-package.ps1`
  - wraps the schema, seed, meta, image-link, and chapter-part exports
  - writes `reader-full-package-manifest.json`
- Added `scripts/export-retired-heavy-table.ps1`
  - exports only whitelisted retired heavy tables
  - current whitelist:
    - `terminology`
    - `chapter_execute`
    - `chapter_error_execute`
- Added `scripts/export-reader-full-chapter-parts.ps1`
  - exports `chapter` by `chapter.id` range
  - default target is about `100000` chapters per part
  - writes `chapter-parts-manifest.json`
  - supports partial export for validation through:
    - `-StartChapterId`
    - `-EndChapterId`
    - `-MaxPartCount`
- Added `scripts/import-reader-full.ps1`
  - imports `schema.sql`
  - imports `seed-system.sql`
  - imports `reader-content-meta.sql`
  - imports `reader-chapter-image-links.sql`
  - imports selected `chapter-part-*`
  - writes resumable progress to `.reader-full-import-progress-<database>.json`
- Added `scripts/verify-reader-full.ps1`
  - checks required package files
  - checks chapter-part presence
  - optionally queries database row counts and chapter id range

Shared helper updates:
- extended `scripts/lib/LitePackageTools.psm1` with:
  - `Get-ReaderFullPackageDefaults`
  - `Get-ReaderFullChapterPartitionSpecs`
  - `Assert-RetiredHeavyTableName`
- added Pester coverage for the new defaults and partition logic in `scripts/tests/LitePackageTools.Tests.ps1`

## Validation
Completed validation:

- `Invoke-Pester -Path .\scripts\tests\LitePackageTools.Tests.ps1`
- `.\scripts\export-lite-schema.ps1 -OutputPath .\sql\reader-full\schema.sql`
- `.\scripts\export-lite-system-seed.ps1 -OutputPath .\sql\reader-full\seed-system.sql`
- `.\scripts\export-reader-full-meta.ps1`
- `.\scripts\export-reader-full-image-links.ps1`
- `.\scripts\export-reader-full-chapter-parts.ps1 -MaxPartCount 1`
- `.\scripts\export-reader-full-package.ps1 -MaxPartCount 2`
- `.\scripts\export-retired-heavy-table.ps1 -TableName terminology`
- `.\scripts\export-retired-heavy-table.ps1 -TableName chapter_execute`
- `.\scripts\export-retired-heavy-table.ps1 -TableName chapter_error_execute`
- `.\scripts\export-reader-full-package.ps1 -SkipExistingChapterParts`
- `.\scripts\verify-reader-full.ps1`
- `.\scripts\import-reader-full.ps1 -ReaderFullDirectory .\sql\reader-full -Database novel_reader_full_validation -StartPart 1 -EndPart 1 -ResetProgress`
- `.\scripts\import-reader-full.ps1 -ReaderFullDirectory .\sql\reader-full -Database novel_reader_full_resume_validation2 -StartPart 1 -EndPart 1 -ResetProgress`
- `.\scripts\import-reader-full.ps1 -ReaderFullDirectory .\sql\reader-full -Database novel_reader_full_resume_validation2 -StartPart 1 -EndPart 2`
- `.\scripts\verify-reader-full.ps1 -Database novel_reader_full_resume_validation2`

Database verification against `novel_reader_full_validation` after importing the first part:
- `novel`: `45255`
- `chapter`: `94687`
- `chapter_image_links`: `592736`

Progress-file verification:
- `.reader-full-import-progress-novel_reader_full_validation.json`
- imported files recorded:
  - `schema.sql`
  - `seed-system.sql`
  - `reader-content-meta.sql`
  - `reader-chapter-image-links.sql`
  - `chapter-part-0001.sql`

Retired heavy-table verification:
- `terminology.sql` exported successfully under `sql\retired-heavy\`
- `chapter_execute.sql` exported successfully under `sql\retired-heavy\`
- `chapter_error_execute.sql` exported successfully under `sql\retired-heavy\`
- only whitelisted retired tables are accepted by the helper guard

Resume validation against `novel_reader_full_resume_validation2`:
- second import run skipped:
  - `schema.sql`
  - `seed-system.sql`
  - `reader-content-meta.sql`
  - `reader-chapter-image-links.sql`
  - `chapter-part-0001.sql`
- second import run imported only:
  - `chapter-part-0002.sql`
- resulting validation counts:
  - `chapter`: `192473`
  - chapter id range: `7023 - 207022`
- progress file now records:
  - `schema.sql`
  - `seed-system.sql`
  - `reader-content-meta.sql`
  - `reader-chapter-image-links.sql`
  - `chapter-part-0001.sql`
  - `chapter-part-0002.sql`

Complete package export validation:
- `sql\reader-full\` now contains:
  - `schema.sql`
  - `seed-system.sql`
  - `reader-content-meta.sql`
  - `reader-chapter-image-links.sql`
  - `chapter-part-0001.sql` through `chapter-part-0061.sql`
  - `chapter-parts-manifest.json`
  - `reader-full-package-manifest.json`
- `reader-full-package-manifest.json` now records:
  - `chapterPartCount = 61`
- `chapter-parts-manifest.json` now records:
  - `chapterCount = 2172075`
  - `partCount = 61`
  - `minChapterId = 7023`
  - `maxChapterId = 6014497`
- current sparse-range observation:
  - `32` chapter-part files are header-only tiny dumps because those `chapter.id` ranges have no live rows
  - this is acceptable for the first deterministic range-based package and keeps resume logic simple

## Documentation Sync
- Updated: `docs/tasks/backend/2026-04-04-reader-full-package-and-heavy-table-retirement.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `docs/engineering/database.md`
- Updated: `docs/engineering/reader-lite-quickstart.md`
- Updated: `README.md`
- Checked with no change needed: `docs/context/development-roadmap.md`

## Risks
- The exact `reader-content-meta.sql` boundary may still need small adjustments if later validation reveals hidden dependencies.
- `chapter_image_links` may prove larger or more coupled than expected and could need its own future partitioning strategy.
- `seed-system.sql` for reader full should stay aligned with the validated reader bootstrap assumptions, or package drift will return.
- If hidden runtime dependencies still touch maintainer tables during full-reader flows, the retirement boundary may need another pass.
- Current validation exercises only the first two `chapter` parts. The full multi-part import path is still not fully exercised end-to-end.

## Follow-ups
- Exercise a longer real import path than the first two chapter parts, preferably against a clean validation database with more than one resume boundary.
- Decide whether future packaging should suppress header-only empty chapter parts or keep them for strict deterministic id-range coverage.
- Revisit `dictionary` trimming once both `lite` and `reader full` are stable.
