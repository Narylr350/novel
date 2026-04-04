# Database Notes

## Runtime Model
- The repository supports `single` and `dual` database modes.
- Database mode is configured from Spring properties and Docker/compose environment variables.
- Large content imports live under `sql/` and are operationally significant.

## Important Operational Facts
- `sql/main.sql` is the largest content import and should be treated as a long-running bootstrap artifact.
- `sql/expand.sql` and related smaller SQL files provide supporting data and expansion tables.
- The backend uses MariaDB-style JDBC configuration with separate primary/secondary datasource support.
- Local batch imports are client-sensitive on Windows. Current probe evidence shows:
  - `mariadb.exe` can import the historical `chapter_error_execute` probe into local MariaDB successfully
  - `mysql.exe` fails on the same probe in stdin/batch mode unless `--default-character-set=utf8mb4` is supplied
  - `--binary-mode` is also a safer default for large escaped-content imports in non-interactive MySQL client runs
  - redirected stdin is a more reliable operational path than `-e "source file.sql"` for local imports

## Canonical Local Import Entry
- Repository-supported local entrypoint: `scripts/import-local-sql.ps1`
- Default target:
  - host: `127.0.0.1`
  - port: `3306`
  - database: `novel`
  - user: `root`
  - password: `novel_root_password`
- Example:

```powershell
.\scripts\import-local-sql.ps1 -SqlFile .\sql\main.sql
```

- The script intentionally uses `mariadb.exe` with redirected stdin instead of PowerShell `<` redirection or `-e "source ..."` because that path is the most stable one validated in this repository on Windows.

## Lite Package Workflow
The repository now has a first runnable `lite` package toolchain under `scripts/`.

### Export Scripts
- Candidate probe: `.\scripts\find-lite-candidate-novels.ps1 -Limit 10`
- Schema export: `.\scripts\export-lite-schema.ps1`
- System seed export: `.\scripts\export-lite-system-seed.ps1`
- Reader demo export:

```powershell
.\scripts\export-lite-reader-demo.ps1 -NovelIds 353498,353474,353487
```

These scripts currently generate package files under `sql/lite/`:
- `schema.sql`
- `seed-system-lite.sql`
- `seed-reader-demo.sql`

### Import Script
- One-path local import entrypoint for the generated lite package:

```powershell
.\scripts\import-lite.ps1 -LiteDirectory .\sql\lite -Database novel_lite_validation
```

### Current First-Pass Lite Baseline
- Demo account email: `demo_reader@lite.local`
- Demo account password: `ReaderPass1`
- First provisional demo novels:
  - `353498` / `绿皮`
  - `353474` / `祸世末子的回归`
  - `353487` / `亲手培养的女团`

### Tooling Constraints Confirmed During Export
- `mysqldump.exe` must include `--column-statistics=0` when exporting from the local MariaDB instance.
- Subquery-driven `--where` exports also require:
  - `--single-transaction`
  - `--skip-lock-tables`
- The first-pass `seed-system-lite.sql` intentionally exports the whole `dictionary` table for runtime safety; this is a temporary baseline, not the final minimal reader-only seed.

## Database Mode Meaning
- `single`:
  - one MariaDB database instance
  - primary business tables and `chapter_scaling_up_one` live in the same database
  - this is the preferred mode for local reader-focused deployment
- `dual`:
  - one primary MariaDB database plus one secondary MariaDB database
  - the secondary database is used for the `chapter_scaling_up_one` scaling table and related repository wiring
  - this is a maintainer / operator mode, not the default small-user deployment target

## Tables And Configuration Notes
Based on existing repository docs:
- `dictionary` stores feature switches and translation/runtime controls
- `platform` and `platform_api_key` hold upstream or AI-platform metadata
- `chapter_sync` is used for scaling/synchronization scenarios
- user upload permissions and other operational toggles are stored in database tables, not only in static config

## Maintenance Rules
- Treat database mode changes and SQL import changes as operational work requiring explicit validation.
- When a task changes write behavior, record database verification in the task document.
- Do not assume README database guidance is current until it has been checked against source and runtime config.

## Table Classification Guidance
After the first healthy full import audit on `2026-04-04`, the repository now has an explicit packaging direction:

### Core Keep
Reader-facing and startup-critical tables:
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

### Maintainer-Optional
Operational, scaling, community-side, or hidden-surface tables that should not define the smallest reader deployment by default:
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

### Candidate Removal From Default Distribution
High-cost translation/staging tables that should be removed from future default bootstrap packages first:
- `terminology`
- `chapter_execute`
- `chapter_error_execute`

Important:
- this classification is a distribution and maintenance guideline, not an instruction to drop live tables immediately
- `DatabaseInitializer` is now mode-aware:
  - `reader` mode checks only the 15 core reader-facing tables
  - `maintainer` mode checks the combined reader + maintainer table set
  - secondary table initialization still depends only on the configured secondary datasource

## Bootstrap Boundary Notes
- Reader-mode startup no longer treats `terminology`, `chapter_execute`, `chapter_error_execute`, `platform_api_key`, and other maintainer-side tables as required bootstrap tables.
- This change reduces startup assumptions for future `lite` packaging work without dropping any live tables from the full imported database.
- Anonymous chapter reading is still not part of the verified bootstrap boundary. Current chapter-content APIs require a valid credential, so chapter reading remains a post-login reader flow.

## Lite Runtime Validation Notes
The first generated `lite` package has now been validated against a real local runtime using:
- backend: `http://localhost:8081`
- web: `http://localhost:8080`
- database: `novel_lite_validation`
- demo reader: `demo_reader@lite.local`

Validated reader flow:
- login
- open demo book detail
- open chapter detail route
- load chapter version metadata
- load chapter notes payload
- load chapter content

Important runtime findings:
- reader-mode chapter-detail loading still touched several maintainer-side tables indirectly, so the backend now treats the following as optional in reader mode when they are absent:
  - `request_log`
  - `user_access_logs`
  - `user_chapter_edit`
  - `chapter_sync`
  - `user_glossary`
- Empty chapter content exists in the imported source data for at least some chapters. The chapter API now returns an empty string instead of encrypting an empty payload, which avoids false frontend decryption failures.
- The current verified boundary is `logged-in` chapter reading. Anonymous chapter reading remains outside the validated lite bootstrap path.
