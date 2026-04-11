# Database Notes

## Runtime Model
- The repository supports `single` and `dual` database modes.
- Database mode is configured from Spring properties and Docker/compose environment variables.
- The backend uses MariaDB-style JDBC configuration with separate primary/secondary datasource support.

## Current Source-Repo Boundary
- The active source tree no longer includes a tracked `sql/` directory or repository-supported SQL packaging/install scripts.
- Historical `lite`, `reader full`, redistribution, and release-package workflows should not be treated as the current source-repo interface.
- If database bootstrap packages or large content imports are still needed, treat them as external operational inputs and verify them against the current application config before use.

Historical packaging work is recorded for reference in:
- `docs/tasks/backend/2026-04-04-lite-database-export-implementation.md`
- `docs/tasks/backend/2026-04-04-reader-full-package-and-heavy-table-retirement.md`

## Current Local Defaults
Current repo-tracked defaults are documented by compose and environment examples:
- host: `127.0.0.1`
- port: `3306`
- default database name in local compose: `novel`
- root password in local compose examples: `novel_root_password`

## Database Mode Meaning
- `single`:
  - one MariaDB database instance
  - primary business tables and scaled chapter data live in the same database
  - preferred mode for local reader-focused deployment
- `dual`:
  - one primary MariaDB database plus one secondary MariaDB database
  - the secondary database is used for the `chapter_scaling_up_one` scaling table and related repository wiring
  - maintainer / operator mode, not the default small-user deployment target

## Tables And Configuration Notes
Based on the current repository docs and source:
- `dictionary` stores feature switches and translation/runtime controls
- `platform` and `platform_api_key` hold upstream or AI-platform metadata
- `chapter_sync` is used for scaling/synchronization scenarios
- user upload permissions and other operational toggles are stored in database tables, not only in static config

## Bootstrap Boundary Notes
- Reader-mode startup no longer treats several maintainer-side tables as required bootstrap tables.
- This boundary reduction was done to lower runtime assumptions for reader-focused deployments, not to instruct operators to drop live tables blindly.
- Anonymous chapter reading is still not part of the verified bootstrap boundary. Current chapter-content APIs require a valid credential, so chapter reading remains a post-login flow.

## Maintenance Rules
- Treat database mode changes and database bootstrap/import changes as operational work requiring explicit validation.
- Before documenting any import workflow, verify that the referenced files and helper scripts actually exist in the current repository.
- When a task changes write behavior, record database verification in the task document.
- Do not reintroduce release-package or redistribution assumptions into canonical docs unless the corresponding artifacts are restored to the tracked source tree in the same change.
