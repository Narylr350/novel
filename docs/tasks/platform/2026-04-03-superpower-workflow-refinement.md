# Task

## Scope
Refine the repository-local superpower workflow preference so simple tasks do not require `using-superpowers`, while complex tasks still default to that workflow.

## Out of Scope
- Changing any product, runtime, crawler, or frontend/backend behavior
- Rewriting non-repository workflow policies
- Removing the repository preference for complex-task superpower usage

## Plan
1. Update `AGENTS.md` so the repository workflow rule distinguishes simple tasks from complex tasks.
2. Record the refinement in a platform task document.
3. Update the platform module index.

## Design
The previous repository-local rule was too broad because it forced `using-superpowers` for every turn.

The refined rule is:
- complex tasks still start with `using-superpowers`
- simple tasks execute directly without that requirement
- current user instructions can still override the repository default

This restores a more practical workflow split while keeping the user's preference that complex work should consistently use the superpowers process.

## File Structure
- `AGENTS.md`
  - repository-local workflow routing rules
- `docs/tasks/platform/2026-04-03-superpower-workflow-refinement.md`
  - task record for the refined workflow rule
- `docs/tasks/platform/INDEX.md`
  - module status and task listing

## Review
The user explicitly requested the refinement: simple tasks should not use `su`, and that change should be written into the requirements.

No separate plan/design review was needed because this is a narrow documentation-only workflow change.

## Implementation
- Refined `AGENTS.md` so only complex tasks default to `using-superpowers`.
- Added this platform task record.
- Updated the platform module index.

## Validation
- Reviewed `AGENTS.md` to confirm the workflow section and local preferences are consistent with the refined rule.
- No backend, browser, or database validation was required because this task changes documentation only.

## Documentation Sync
Updated:
- `AGENTS.md`
- `docs/tasks/platform/2026-04-03-superpower-workflow-refinement.md`
- `docs/tasks/platform/INDEX.md`

Checked with no change needed:
- `AI_CONTEXT.md`
- `docs/context/development-roadmap.md`
- `docs/tasks/platform/2026-04-03-superpower-workflow-preference.md`

## Risks
- Future sessions still need to judge task complexity correctly; the rule no longer removes that judgment call.
- The older preference task remains in history, so future readers should follow the newer refinement record when there is tension.

## Follow-ups
- None required immediately.
