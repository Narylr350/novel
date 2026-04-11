# Task

## Scope
Archive the remaining release-era engineering docs so they no longer live in the active `docs/engineering/` surface while still remaining available for traceability.

## Out of Scope
- Rewriting historical task records that mention release-era work
- Deleting historical validation logs under `docs/testing/ai-logs/`
- Reintroducing stubs or redirects under `docs/engineering/` for archived release-era workflows
- Changing runtime code, build logic, or deployment behavior

## Plan
- Identify the remaining release-era docs still living under `docs/engineering/`.
- Move them into a dedicated archive folder under `docs/archive/legacy/`.
- Add archive metadata so readers understand the docs are historical.
- Update nearby documentation indexes and task records to reflect the archive move.

## Design
The previous cleanup removed release-era workflows from the canonical docs, but two old engineering documents still lived under `docs/engineering/`:
- `小白使用说明-源码包.md`
- `reader-lite-quickstart.md`

Leaving them there kept the misleading shape of the docs tree even if current top-level docs no longer linked to them.

This task preserves the content but changes the boundary:
- active engineering docs stay under `docs/engineering/`
- historical release-surface docs move to `docs/archive/legacy/release-surface/`
- archive-local notes explain that the files are for traceability only

Historical task records remain in `docs/tasks/` because they document what happened and when.

## File Structure
- `docs/archive/legacy/release-surface/README.md`: archive scope and usage note
- `docs/archive/legacy/release-surface/小白使用说明-源码包.md`: archived beginner source-package guide
- `docs/archive/legacy/release-surface/reader-lite-quickstart.md`: archived reader-lite quickstart
- `docs/README.md`: docs tree note for the archive location
- `docs/tasks/platform/INDEX.md`: platform status and task list
- `docs/tasks/platform/2026-04-09-release-surface-doc-archive.md`: task record

## Review
Pre-implementation review stayed lightweight.

Confirmed decisions:
- the archive target should be `docs/archive/legacy/`, not another active engineering folder
- task records should stay in place
- archived docs should remain readable but clearly labeled as non-canonical

## Implementation
Archived the remaining release-era engineering docs.

Main changes:
- moved `docs/engineering/小白使用说明-源码包.md` to `docs/archive/legacy/release-surface/小白使用说明-源码包.md`
- moved `docs/engineering/reader-lite-quickstart.md` to `docs/archive/legacy/release-surface/reader-lite-quickstart.md`
- added archive notes at the top of both files
- added `docs/archive/legacy/release-surface/README.md`
- updated `docs/README.md` and `docs/tasks/platform/INDEX.md` to reflect the archive location

## Validation
Validation was documentation-only:

- verified the two release-era engineering docs no longer exist under `docs/engineering/`
- verified they now exist under `docs/archive/legacy/release-surface/`
- fixed the internal link from the archived quickstart to the archived Chinese guide
- reviewed the resulting git status for scope correctness

No backend runtime validation or browser validation was run because this task only moved and annotated docs.

## Documentation Sync
- Updated: `docs/README.md`
- Updated: `docs/tasks/platform/INDEX.md`
- Updated: `docs/tasks/platform/2026-04-09-release-surface-doc-archive.md`
- Updated: `docs/archive/legacy/release-surface/README.md`
- Updated: `docs/archive/legacy/release-surface/小白使用说明-源码包.md`
- Updated: `docs/archive/legacy/release-surface/reader-lite-quickstart.md`
- Checked with no change needed: `README.md`, `docs/engineering/runtime-operations.md`, `docs/engineering/database.md`

## Risks
- Historical task docs still mention the old engineering paths because they describe the state of the repository at that time.
- External notes or bookmarks outside the repository may still point at the old paths.

## Follow-ups
- If you want a cleaner historical boundary later, archive or summarize release-era validation logs that only exist to support the retired source-package flow.
