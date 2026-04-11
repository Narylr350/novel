# Task

## Scope
Rewrite the source repository's active documentation surface after launch so it no longer presents release-package, source-package, packaged-jar, or redistribution workflows as the canonical way to work in this repo, and so the documented project mainline matches normal post-launch feature delivery.

## Out of Scope
- Removing historical task records that document how earlier release-oriented work was done
- Reworking actual runtime code, Docker wiring, or frontend behavior
- Reconstructing deleted release assets, SQL packages, or helper scripts
- Archiving or deleting untracked historical docs outside the canonical surface without explicit confirmation

## Plan
- Audit the current canonical docs for references to release/distribution surfaces that no longer exist in the tracked repository.
- Rewrite the root README and engineering docs so they describe the current source-maintenance surface and post-launch delivery focus.
- Update context docs and module indexes so roadmap/status language matches a post-launch feature-delivery stage instead of a release-prep or stabilization-only stage.
- Record the cleanup in a platform task document and review the diff for scope correctness.

## Design
The repository drift is no longer just terminology drift. Canonical docs were still routing readers toward `release/`, root `.cmd` wrappers, source-package distribution trees, and repository-owned SQL/install workflows even though those surfaces are not present in the tracked worktree anymore.

This cleanup keeps historical implementation records but removes those release-oriented surfaces from active guidance while also resetting the top-level narrative:
- `README.md` becomes a source-repo maintenance and feature-delivery entrypoint instead of a release-package landing page.
- `docs/engineering/runtime-operations.md` describes only the startup paths that are still supported by the tracked repository.
- `docs/engineering/database.md` stops advertising packaging/install workflows that are no longer part of the current source tree.
- Context docs replace release-stage wording with post-launch product-mainline wording and remove `sql/` as a guaranteed tracked root.

The task intentionally does not delete historical task docs. They remain useful evidence, but they should not control the active operator workflow.

## File Structure
- `AI_CONTEXT.md`: align the bootstrap file with post-launch feature delivery
- `README.md`: reset the root entrypoint to the current source-maintenance surface and current product focus
- `docs/engineering/runtime-operations.md`: remove release-package startup guidance from active operations docs
- `docs/engineering/database.md`: retire packaging/redistribution workflow guidance from canonical database notes
- `docs/context/project-overview.md`: align repository shape and post-launch product goals
- `docs/context/architecture.md`: align repository shape and architecture wording
- `docs/context/development-roadmap.md`: replace stabilization/release-stage wording in the roadmap
- `docs/tasks/platform/INDEX.md`: record the platform-level cleanup
- `docs/tasks/backend/INDEX.md`: record the backend/doc cleanup impact
- `docs/tasks/platform/2026-04-09-post-launch-release-surface-cleanup.md`: task record

## Review
Pre-implementation review stayed lightweight.

Confirmed decisions:
- active canonical docs should describe the tracked repository surface, not an older release package
- historical release work can remain in task records, but not as the default entrypoint for new maintenance work
- the top-level docs should now optimize for normal feature delivery, not stabilization-only framing
- this task should stay documentation-only and avoid unrelated runtime refactors

## Implementation
Implemented a post-launch rewrite of the canonical documentation surface.

Main changes:
- updated `AI_CONTEXT.md` so new sessions enter the repo with a feature-delivery framing instead of a stabilization-only framing
- rewrote `README.md` around the current tracked repository roots and supported startup paths
- removed active guidance that depended on `release/`, root `.cmd` wrappers, source-package distribution trees, and repository-tracked SQL/install packages
- simplified `docs/engineering/runtime-operations.md` and `docs/engineering/database.md` so they no longer advertise release-phase workflows as active repo contracts
- aligned context docs with the current source tree and post-launch product/roadmap terminology
- updated module indexes and added this task record

## Validation
Validation was documentation-only:

- verified the current repository root no longer contains `release/`, `scripts/`, root `.cmd` wrappers, or a tracked `sql/` directory
- reviewed the edited docs against the current worktree and compose/runtime files for consistency
- reviewed `git diff` scope after editing

No backend runtime validation or browser validation was run because this task did not change executable behavior.

## Documentation Sync
- Updated: `README.md`
- Updated: `AI_CONTEXT.md`
- Updated: `docs/engineering/runtime-operations.md`
- Updated: `docs/engineering/database.md`
- Updated: `docs/context/project-overview.md`
- Updated: `docs/context/architecture.md`
- Updated: `docs/context/development-roadmap.md`
- Updated: `docs/tasks/platform/INDEX.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `docs/tasks/platform/2026-04-09-post-launch-release-surface-cleanup.md`
- Checked with no change needed: `docs/context/tech-stack.md`, `HELP.md`, `docs/tasks/web/INDEX.md`

## Risks
- Historical release-oriented docs still exist in the worktree outside the canonical surface and may continue to confuse readers if linked or surfaced elsewhere.
- Some task records intentionally still mention release packaging because they document what happened at that time.
- If external deployment bundles are still maintained privately, they now need their own documentation boundary instead of relying on the source-repo README.

## Follow-ups
- Archive or remove any remaining untracked release-oriented docs once you confirm they are no longer needed outside the source repo.
- If a new deployment artifact flow is still required, document it as an external operations workflow instead of restoring it to the repository root by default.
