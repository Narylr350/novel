# FreeNovel Docs

## Structure
- `context/`: stable project context that should be read before task work
- `engineering/`: current API, database, and runtime engineering notes
- `product/`: product-facing references when they become worth preserving
- `tasks/`: module task records, current module indexes, and module-local `history/` folders
- `testing/`: validation standards for backend APIs and browser integration
- `archive/`: legacy docs preserved for traceability

## Entry Points
- AI sessions should start from `../AI_CONTEXT.md`, which owns the required reading order.
- Humans looking for current repository guidance should usually start from `context/project-overview.md` and `context/development-roadmap.md`.

## Notes
- `tasks/` is organized by real repository boundaries, not a forced fixed template.
- Module root task folders keep current task records. Historical execution support docs live under `docs/tasks/<module>/history/`.
- `context/development-roadmap.md` is the master delivery-direction document.
- Release-era source-package docs are archived under `docs/archive/legacy/release-surface/`.
