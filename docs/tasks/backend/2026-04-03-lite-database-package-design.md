# Task

## Scope
Define the first repository-supported `lite` database package strategy so small-user deployments can start with a minimal reader-ready dataset instead of importing the full content dump.

## Out of Scope
- Executing the final lite export before a healthy full database import exists
- Rewriting the current database schema
- Deleting or migrating full-content tables
- Building a Flyway-based migration system in this task
- Selecting the exact three demo novels before the healthy source database is ready

## Plan
- Capture the target product shape and data-package boundaries for `lite`.
- Define the table dependency rules for extracting a reader-ready subset from a healthy imported database.
- Define the output package structure and import sequence for future implementation.
- Record the next execution steps needed once the full local import completes.

## Design
The repository will support a new `lite` bootstrap path aimed at the smallest useful reader deployment. The design goal is not “full site fidelity.” The design goal is “import a small package, start the app, log in immediately, and read a few real books.”

### Product Goal
`lite` mode should provide:
- backend and frontend can start against the package
- one default demo account is preloaded
- the user can log in immediately
- the reader surface can browse/search and read approximately three real novels
- each selected demo novel keeps its complete chapters

`lite` mode does not aim to preload:
- broad community history
- large comment archives
- full user data
- maintainer/operations history
- crawler/translator/upload task state

### Extraction Strategy
The recommended strategy is:
1. complete one healthy full import into local MariaDB
2. select approximately three real novel IDs from that healthy database
3. re-export the `lite` package from the database itself, not by cutting raw `main.sql`

The repository must not build `lite` by manually slicing raw SQL text. The current dump already shows client/encoding sensitivity, and raw text cutting is too likely to corrupt large `INSERT` statements, escaped content, or LOB data.

### Dependency Layers
The `lite` package should be built from these data layers.

#### 1. Core Reading Layer
Always include:
- `novel`
- `novel_chapter`
- `chapter`

This is the minimum chain required to browse a selected book, view its directory, and read the full text.

#### 2. Presentation Support Layer
Conditionally include only if the selected novels actually depend on them:
- `chapter_image_links`
- other directly read metadata tables needed by novel detail or chapter rendering

The rule is practical: only include what the reader-facing pages actually touch for the selected books.

#### 3. Minimal Identity Layer
Include only the smallest user/auth footprint needed for immediate login:
- `user`
- `credential`
- any other auth-adjacent table proven necessary by the current login flow

This layer should preload one default demo account rather than cloning historical user data.

#### 4. Minimal System Layer
Include only small tables required to let the app start and operate normally in reader mode:
- `dictionary`
- other startup-critical small tables if runtime checks prove they are required

This layer is operational glue, not a copy of full site operations state.

### Explicit Exclusions
The first `lite` package should exclude:
- `chapter_error_execute`
- chapter scaling/synchronization copies unless startup proves they are required for `single` mode
- upload/share history
- glossary/translation task history
- message/blacklist/platform operations history
- maintainer-only or operator-only tables not needed by reader-mode startup

### Package Layout
The future `lite` distribution should be split into three required files:

1. `schema.sql`
- all required table definitions

2. `seed-system-lite.sql`
- default demo account
- minimal auth/system data
- minimal startup dictionaries and other required small-table seed data

3. `seed-reader-demo.sql`
- approximately three real demo novels
- complete chapters for those novels
- only the reader-facing dependent data needed by those books

This split makes failures diagnosable and avoids one opaque all-in-one bootstrap blob.

### Import Order
Future `lite` installation should import in this order:
1. `schema.sql`
2. `seed-system-lite.sql`
3. `seed-reader-demo.sql`

### Future Full Package Relationship
The repository should eventually maintain separate tracks:
- `lite`: reader-ready minimal bootstrap
- `full`: large content packages for advanced users or site operators
- optional operator-only packages for maintenance-side history or high-side-effect data

## File Structure
- `docs/tasks/backend/2026-04-03-lite-database-package-design.md`: design baseline for the lite package effort
- `docs/tasks/backend/INDEX.md`: backend module progress tracking
- `docs/engineering/database.md`: canonical database packaging notes once implementation begins

## Review
This design was reviewed in conversation and explicitly confirmed for these decisions:
- `lite` targets the smallest useful startup, not near-full realism
- preload a default demo account
- use approximately three real novels
- keep full chapters for each selected demo novel
- build from a healthy imported database, not from raw SQL text slicing

## Implementation
Design only in this task. No SQL export or package generation has been executed yet.

Implementation preparation completed:
- Reviewed current entity/table definitions and the repository-owned schema source in `novel/src/main/java/com/wtl/novel/Config/TableSqlRepository.java`.
- Confirmed the first-pass table inventory for `lite` planning:
  - core reading tables: `novel`, `novel_chapter`, `chapter`
  - likely presentation-support table: `chapter_image_links`
  - minimal identity tables: `user`, `credential`
  - minimal system table: `dictionary`
  - likely reader-optional but currently excludable tables: `comment`, `chapter_comment`, `favorites`, `notes`, `reading_record`
- Confirmed that `DatabaseInitializer` currently treats many maintainer/operations tables as required for startup, which means `schema.sql` should come from the repository schema source rather than from a hand-picked subset in the first implementation pass.

### Export Preparation Checklist
Once the full local import completes, the next execution pass should run this checklist in order:

1. Select three candidate demo novels from the healthy database.
2. Record their `novel.id`, `novel.true_id`, `novel.title`, `author_name`, and chapter counts.
3. Export dependency probes for:
   - `novel`
   - `novel_chapter`
   - `chapter`
   - `chapter_image_links`
   - `novel_tag` and `tag`
   - `comment` and `chapter_comment`
4. Verify which of the probed tables are actually needed to avoid reader-mode page breakage.
5. Build `seed-system-lite.sql` separately from the chosen demo-account rows in:
   - `user`
   - `credential`
   - any login-proven helper tables if discovered

### Probe SQL Skeleton
The following SQL is the starting point for the post-import dependency scan:

```sql
-- 1. Candidate novels by chapter volume
SELECT
  n.id,
  n.title,
  n.author_name,
  COUNT(c.id) AS chapter_count
FROM novel n
LEFT JOIN chapter c ON c.novel_id = n.id AND c.is_deleted = 0
WHERE n.is_deleted = 0
GROUP BY n.id, n.title, n.author_name
HAVING COUNT(c.id) > 0
ORDER BY chapter_count DESC, n.id ASC;

-- 2. Directory rows for chosen novels
SELECT *
FROM novel_chapter
WHERE novel_id IN (/* chosen novel ids */)
ORDER BY novel_id, chapter_num;

-- 3. Full chapter payload for chosen novels
SELECT *
FROM chapter
WHERE novel_id IN (/* chosen novel ids */)
  AND is_deleted = 0
ORDER BY novel_id, chapter_number;

-- 4. Image-link dependency probe
SELECT cil.*
FROM chapter_image_links cil
JOIN novel_chapter nc ON nc.chapter_true_id = cil.chapter_true_id
WHERE nc.novel_id IN (/* chosen novel ids */);

-- 5. Tag dependency probe
SELECT nt.*, t.*
FROM novel_tag nt
JOIN tag t ON t.id = nt.tag_id
WHERE nt.novel_id IN (/* chosen novel ids */);

-- 6. Reader-comment dependency probe
SELECT *
FROM comment
WHERE chapter_id IN (
  SELECT id FROM chapter WHERE novel_id IN (/* chosen novel ids */)
);

SELECT *
FROM chapter_comment
WHERE novel_id IN (/* chosen novel ids */);
```

### Packaging Guidance For The Execution Pass
- `schema.sql` should be generated from the repository schema source or from a clean schema-only dump, not manually rewritten.
- `seed-system-lite.sql` should contain one controlled demo account and the minimal runtime dictionaries required by current reader-mode startup.
- `seed-reader-demo.sql` should contain only the chosen novels and their traced dependencies.
- Comment/history tables remain optional in the first executable `lite` package unless reader-mode validation proves a hard dependency.

## Validation
- Conversational design review completed and approved
- No runtime validation yet because the full source database import is still the prerequisite

## Documentation Sync
- Updated: `docs/tasks/backend/2026-04-03-lite-database-package-design.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Checked with no change needed: `docs/engineering/database.md`, `docs/context/development-roadmap.md`

## Risks
- Exact table dependency edges may still expand once the healthy full database is available for concrete tracing.
- Login flow may depend on additional small tables beyond `user` and `credential`.
- Selected real novels may reveal hidden dependencies such as image/link tables or auxiliary metadata not yet documented.

## Follow-ups
- After the full local import completes, identify three candidate demo novels and trace their reader-path dependencies.
- Build a table/dependency inventory for the selected demo novels.
- Implement export scripts that generate `schema.sql`, `seed-system-lite.sql`, and `seed-reader-demo.sql` from the healthy database.
- Add a reader-focused install path in README once the first lite package is real.
