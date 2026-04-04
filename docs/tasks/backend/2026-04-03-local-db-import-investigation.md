# Task

## Scope
Investigate why large SQL imports behave differently across local MySQL/MariaDB clients versus Docker MariaDB, capture the minimum reproducible evidence needed for a stable local import path, and add one canonical local import entrypoint for maintainers.

## Out of Scope
- Redesigning the database schema
- Repackaging the full content dump
- Refactoring backend persistence code
- Verifying the entire `sql/main.sql` end-to-end import

## Plan
- Reproduce the import problem on the repaired local MariaDB service instead of guessing from historical screenshots.
- Compare local client behavior across `mariadb.exe` and `mysql.exe`.
- Extract a focused probe from the historical failure area in `sql/main.sql`.
- Record the root-cause evidence and the current recommended local import path.
- Add one repository-supported local import script that uses the validated MariaDB stdin path on Windows.
- Point README and engineering docs at that script as the canonical local import entrypoint.

## Design
This investigation treats the import issue as a multi-layer problem:
- database service behavior
- client executable behavior
- stdin / batch import mode
- dump encoding and escaped content

The first goal is not to import the whole 21GB dump. The first goal is to create a small reproduction slice from the historical failure region and prove which layer is breaking. A small deterministic probe is faster and gives cleaner evidence than rerunning a multi-hour import.

The current probe strategy uses the `chapter_error_execute` section from `sql/main.sql` because:
- the historical screenshot failed around that region
- it contains multilingual `mediumtext` content
- it contains many literal `\\n` sequences, which makes it a good stress case for batch import parsing

## File Structure
- `sql/main.sql`: source of the focused import probe
- `scripts/import-local-sql.ps1`: canonical local SQL import entrypoint
- `docs/testing/ai-logs/import-probe-chapter-error-execute.sql`: extracted reproduction slice
- `docs/testing/ai-logs/mysql-client-import*.log`: MySQL client import evidence
- `docs/testing/ai-logs/mariadb-client-stdin*.log`: MariaDB client import evidence
- `docs/engineering/database.md`: canonical operational note for local import behavior
- `docs/tasks/backend/INDEX.md`: backend module status

## Review
Pre-implementation review was kept lightweight. The task stays within the stabilization roadmap because it clarifies database/runtime behavior and reduces blind work around local startup and import failures.

## Implementation
- Repaired the broken local MariaDB service first so import testing could run against a healthy local database.
- Extracted a focused probe file from the historical failure region:
  - `docs/testing/ai-logs/import-probe-chapter-error-execute.sql`
- Verified that `mariadb.exe` can import the probe successfully into local MariaDB.
- Verified that `mysql.exe` against the same local MariaDB fails in batch mode by default with a client-side character decoding error.
- Verified that `mysql.exe --default-character-set=utf8mb4` imports the same probe successfully, with the only later failure coming from the intentionally truncated next table in the probe file.
- Verified that `mysql.exe --default-character-set=utf8mb4 --binary-mode` also imports the probe successfully up to the same intentional truncation boundary.
- Verified that `mariadb.exe` stdin redirection is more reliable than `-e "source ..."` for local operational imports. `dictionary.sql` did not materialize correctly through the `-e` path, but imported correctly through redirected stdin.
- Rebuilt the local `novel` database and confirmed these smaller project imports work through local MariaDB:
  - `sql/invitation_code.sql`
  - `sql/user.sql`
  - `sql/credential.sql`
  - `sql/dictionary.sql`
- Added the repository-supported local import script:
  - `scripts/import-local-sql.ps1`
- Pointed `README.md` and `docs/engineering/database.md` at that script as the canonical Windows local import entrypoint.

## Validation
- Local MariaDB service healthy and listening on `localhost:3306`
- `mariadb.exe` probe import:
  - created `chapter_error_execute`
  - imported `244` rows successfully
- `mysql.exe` probe import without explicit charset:
  - failed with `ERROR 1366 (22007)` on multilingual content
- `mysql.exe` probe import with `--default-character-set=utf8mb4`:
  - imported `244` rows successfully before reaching the intentionally truncated next-table definition
- Probe logs retained under:
  - `docs/testing/ai-logs/import-probe-chapter-error-execute.sql`
  - `docs/testing/ai-logs/mysql-client-import.err.log`
  - `docs/testing/ai-logs/mysql-client-import-utf8.err.log`
  - `docs/testing/ai-logs/mysql-client-import-utf8-binary.err.log`
  - `docs/testing/ai-logs/mariadb-client-stdin.err.log`
- Local `novel` database bootstrap checks:
  - `invitation_code`: imported
  - `user`: imported
  - `credential`: imported
  - `dictionary`: imported through stdin redirection with `45` rows
- Script validation:
  - `powershell -ExecutionPolicy Bypass -File scripts/import-local-sql.ps1 -SqlFile sql/dictionary.sql -Database novel_script_probe -EnsureDatabase`
  - verified `dictionary` table exists in `novel_script_probe`
  - verified `45` rows imported
  - verified a second run with `sql/invitation_code.sql` imports successfully into the same database

## Documentation Sync
- Updated: `docs/engineering/database.md`
- Updated: `README.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `docs/tasks/backend/2026-04-03-local-db-import-investigation.md`
- Checked with no change needed: `docs/context/tech-stack.md`, `docs/engineering/runtime-operations.md`

## Risks
- The full `sql/main.sql` has not been re-imported end-to-end on the repaired local database yet.
- The historical `Unknown command '\\n'` screenshot has not been reproduced verbatim in this session.
- The current root-cause evidence is strong for client encoding / batch mode differences, but there may still be additional edge cases later in the full dump.
- `spring.jpa.hibernate.ddl-auto=none` in both `dev` and `prod`, so backend startup still requires a much broader schema than the currently imported small tables.

## Follow-ups
- Validate the smallest safe command line for full local imports and standardize on it.
- Decide whether to officially support only `mariadb.exe` for local bootstrap.
- Split full-data import guidance from a future lite bootstrap path.
- After a full healthy import exists, rebuild the distribution data into `schema`, `seed`, `lite`, `full`, and optional operator-only packages.
