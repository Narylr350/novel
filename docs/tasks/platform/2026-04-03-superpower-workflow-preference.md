# Task

## Scope
Persist the user's current workflow preference that every turn in this repository should start through the superpowers workflow.

## Out of Scope
- Changing any product, runtime, crawler, or frontend/backend behavior
- Rewriting the full workflow policy stack outside this repository
- Editing machine-global instruction files outside the repository

## Plan
1. Update the repository `AGENTS.md` so the workflow preference is explicit and durable.
2. Record the preference change in a platform task document.
3. Update the platform module index.

## Design
The repository already contains guidance about when `using-superpowers` should or should not be used by default.

This task adds a repository-specific override for the current project:
- every turn in this repository should begin by invoking `using-superpowers`
- the workflow remains subject to direct user instructions
- this override is local to the repository and does not attempt to redefine other projects

This keeps future sessions aligned with the user's explicit collaboration preference while still respecting instruction precedence.

## File Structure
- `AGENTS.md`
  - repository-local workflow override for superpowers usage
- `docs/tasks/platform/2026-04-03-superpower-workflow-preference.md`
  - task record for the workflow preference change
- `docs/tasks/platform/INDEX.md`
  - module status and task listing

## Review
The user explicitly requested that every round use superpowers and approved persisting that preference into repository documentation.

No separate plan/design review was needed because this is a narrow documentation-only workflow change.

## Implementation
- Added a repository-local workflow preference to `AGENTS.md` so future work in this repository starts through `using-superpowers`.
- Added this platform task record.
- Updated the platform module index.

## Validation
- Reviewed `AGENTS.md` for consistency with the existing workflow section and the user instruction precedence note from the superpowers skill.
- No backend, browser, or database validation was required because this task changes documentation only.

## Documentation Sync
Updated:
- `AGENTS.md`
- `docs/tasks/platform/2026-04-03-superpower-workflow-preference.md`
- `docs/tasks/platform/INDEX.md`

Checked with no change needed:
- `AI_CONTEXT.md`
- `docs/context/development-roadmap.md`
- `docs/tasks/platform/2026-04-03-product-mainline-roadmap-confirmation.md`

## Risks
- This preference is repository-local. Other repositories on the same machine still follow their own instructions.
- If the user later wants lighter-weight workflow routing, `AGENTS.md` will need another explicit update.

## Follow-ups
- None required immediately.
