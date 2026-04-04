# Task

## Scope
Audit the fully imported local MariaDB database, verify that the expected table set exists after `main.sql` and `expand.sql`, and classify tables for future maintenance work into:
- core keep
- maintainer-optional
- candidate removal from default distribution

The goal is to reduce blind maintenance and make later `lite/full/optional` packaging decisions evidence-based instead of name-based guesses.

## Out of Scope
- Dropping any live tables from the local database
- Rewriting backend startup requirements in this task
- Generating the final `lite` package
- Refactoring crawler, translation, or reader features

## Plan
1. Inspect the live `novel` database table inventory after the full import.
2. Record table sizes and exact counts for the heaviest tables.
3. Compare the live schema with backend entity declarations and the current `DatabaseInitializer` required-table list.
4. Classify each table as core keep, maintainer-optional, or candidate removal from default distribution.
5. Sync the audit result into backend task records and engineering database guidance.

## Design
The audit uses three evidence sources:
- the live MariaDB schema and row counts
- backend entity/runtime declarations
- frontend and backend feature-surface references

The classification rule is:
- `Core Keep`: directly required for reader-mode startup, authentication, browsing, reading, comments, favorites, notes, or current app boot safety
- `Maintainer-Optional`: useful for operators, crawler workflows, translation workflows, upload/community side features, or scaling modes, but not appropriate as minimal reader bootstrap defaults
- `Candidate Removal From Default Distribution`: high-cost or low-value operator history/staging tables that should not ship in the default package and may later be removed from the product runtime after dedicated feature work

Important boundary:
- this task does **not** treat the current `DatabaseInitializer` list as the final truth of what the future product should require
- it treats that list as the current runtime constraint that future schema work must consciously reduce

## File Structure
- `docs/tasks/backend/2026-04-04-full-database-audit-and-table-classification.md`
  - audit record and table classification
- `docs/tasks/backend/INDEX.md`
  - backend progress tracking
- `docs/engineering/database.md`
  - durable database packaging and table-classification guidance
- `docs/testing/ai-logs/db-table-size-audit.tsv`
  - captured live table inventory with approximate row counts and sizes
- `docs/testing/ai-logs/backend-table-reference-scan.txt`
  - raw code scan for entity/query references
- `docs/testing/ai-logs/feature-surface-scan.txt`
  - raw feature-surface scan across backend/frontend

## Review
The user explicitly asked to inspect all tables after the import, find the unnecessary ones, and lower maintenance cost.  
The review gate stayed lightweight because this is an audit/documentation task, not a behavior change.

## Implementation
### Live Database Audit
Verified that the imported local `novel` database now contains `38` tables:
- `chapter`
- `chapter_comment`
- `chapter_error_execute`
- `chapter_execute`
- `chapter_image_links`
- `chapter_scaling_up_one`
- `chapter_sync`
- `chapter_updates`
- `comment`
- `credential`
- `dictionary`
- `favorite_groups`
- `favorites`
- `invitation_code`
- `message`
- `notes`
- `novel`
- `novel_chapter`
- `novel_download_limit`
- `novel_tag`
- `platform`
- `platform_api_key`
- `post`
- `post_agree`
- `post_comment`
- `reading_record`
- `request_log`
- `tag`
- `terminology`
- `user`
- `user_access_logs`
- `user_blacklist`
- `user_chapter_edit`
- `user_feedback`
- `user_glossary`
- `user_novel_relation`
- `user_operation_log`
- `user_tag_filter`

### Heaviest Tables
Exact counts gathered for the heaviest operational tables:
- `chapter`: `2,172,075` rows, about `11,466.91 MB`
- `terminology`: `16,331,908` rows, about `1,589.00 MB`
- `chapter_execute`: `31,685` rows, about `792.14 MB`
- `chapter_scaling_up_one`: `38,328` rows, about `235.28 MB`
- `chapter_image_links`: `592,736` rows, about `151.25 MB`
- `message`: `209,080` rows, about `69.19 MB`
- `reading_record`: `239,926` rows, about `48.16 MB`
- `novel`: `45,255` rows, about `29.16 MB`
- `user_access_logs`: `73,361` rows, about `28.08 MB`
- `novel_tag`: `262,261` rows, about `11.52 MB`

### Runtime Constraint Check
The current backend startup path still treats all `38` main-database tables as required in:
- `novel/src/main/java/com/wtl/novel/Config/DatabaseInitializer.java`

This means the current application can recreate every missing table automatically, but it does **not** mean every table belongs in the future default distribution.

### Classification
#### Core Keep
These directly support current reader-mode startup or the agreed reader product:
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

Keep rationale:
- they back login/bootstrap, browse/search, novel detail, chapter reading, notes, favorites, tag filtering, or comments
- they are aligned with the confirmed reader product direction

#### Maintainer-Optional
These are real features or operator capabilities, but they should not define the smallest supported reader deployment:
- `chapter_image_links`
- `chapter_scaling_up_one`
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
- `user_access_logs`
- `user_blacklist`
- `user_chapter_edit`
- `user_feedback`
- `user_glossary`
- `user_novel_relation`
- `user_operation_log`
- `user_tag_filter`

Optional rationale:
- some of these belong to maintainer or operator tooling
- some belong to hidden community or moderation surfaces
- some are useful product history tables but are not required for a minimal reader bootstrap
- some belong to scaling modes or crawler-side operational controls

#### Candidate Removal From Default Distribution
These are the clearest high-cost / low-default-value tables and should be removed from the future default data path first:
- `terminology`
- `chapter_execute`
- `chapter_error_execute`

Candidate rationale:
- `terminology` is a massive translation-side byproduct table with weak data quality and no reason to block reader deployment
- `chapter_execute` is extremely large for a staging/translation workflow table
- `chapter_error_execute` is an error-compensation table and should not ship as a default bootstrap requirement

### Key Audit Conclusion
The next database-maintenance target is **not** dropping random tables from the live database.  
The next target is:
1. shrink the default distribution surface
2. move translation/crawler staging tables out of the default package
3. eventually reduce `DatabaseInitializer` so runtime does not pretend all maintainer tables are reader essentials

## Validation
- Queried live MariaDB table inventory after the full import
- Captured approximate table sizes in `docs/testing/ai-logs/db-table-size-audit.tsv`
- Captured exact counts for the heaviest tables through direct `COUNT(*)` queries
- Reviewed backend startup requirements in `DatabaseInitializer.java`
- Reviewed repository schema declarations in `TableSqlRepository.java`
- Reviewed frontend/backend feature references in:
  - `docs/testing/ai-logs/backend-table-reference-scan.txt`
  - `docs/testing/ai-logs/feature-surface-scan.txt`

No backend or browser validation was required because this task did not change application behavior.

## Documentation Sync
Updated:
- `docs/tasks/backend/2026-04-04-full-database-audit-and-table-classification.md`
- `docs/tasks/backend/INDEX.md`
- `docs/engineering/database.md`

Checked with no change needed:
- `docs/context/development-roadmap.md`
- `docs/tasks/backend/2026-04-03-lite-database-package-design.md`
- `docs/context/project-overview.md`

## Risks
- Some tables classified as maintainer-optional are still treated as required by current startup code, so packaging changes must be paired with startup/schema work later.
- `chapter_image_links` may be needed by a subset of books and should be validated against real reader-path examples before excluding it from any first `lite` package.
- `message`, `post`, and related tables are still real features in the codebase even if hidden from the simplified reader surface.

## Follow-ups
- Reduce `DatabaseInitializer` so reader-mode startup no longer requires the full maintainer table set.
- Make `terminology`, `chapter_execute`, and `chapter_error_execute` the first tables removed from the future default distribution.
- Start the first executable `lite` export using the new classification as the package boundary.
- Audit whether `message/post` should remain maintainer-optional or be fully retired from the product surface later.
