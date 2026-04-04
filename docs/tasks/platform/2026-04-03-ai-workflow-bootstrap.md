# Task

## Scope
Bring the reusable AI workflow scaffold from the LoopNova mainline repository into FreeNovel as a repo-local reference and starting point.

## Out of Scope
- migrating LoopNova business task history into FreeNovel
- changing FreeNovel runtime behavior
- cleaning unrelated legacy code or configuration drift in this task
- adding tests or CI enforcement for the workflow

## Plan
1. Inspect the reusable workflow entry files and docs structure in `D:\java\loopnova`.
2. Keep the reusable workflow shape, but adapt it to FreeNovel's real repository layout.
3. Create the repo-local bootstrap files, docs structure, task template, testing guidance, and initial module indexes.
4. Record this migration as the first normalized platform task in FreeNovel.

## Design
LoopNova's reusable value is the workflow shape, not its business history. FreeNovel therefore adopts the same entry pattern of `AGENTS.md`, `AI_CONTEXT.md`, concise canonical docs under `docs/context/` and `docs/engineering/`, a single-task-document workflow under `docs/tasks/`, and explicit validation guidance under `docs/testing/`.

The migrated scaffold is adapted rather than copied verbatim. FreeNovel has one backend, one web frontend, heavy SQL imports, and known documentation/runtime drift, so the new docs emphasize stabilization, startup truth, and cross-layer contract safety instead of LoopNova's marketplace roadmap.

## File Structure
- `AGENTS.md`: repository-wide workflow and execution rules for FreeNovel
- `AI_CONTEXT.md`: mandatory bootstrap index for AI sessions
- `CLAUDE.md`: aligned secondary workflow entry file
- `docs/README.md`: docs tree overview
- `docs/context/*`: stable FreeNovel context docs
- `docs/engineering/*`: API, database, and runtime engineering notes
- `docs/product/README.md`: reserved product-doc placeholder
- `docs/testing/*`: backend and browser validation standards
- `docs/tasks/TEMPLATE.md`: normalized task record template
- `docs/tasks/platform/INDEX.md`: platform task index
- `docs/tasks/backend/INDEX.md`: backend task index
- `docs/tasks/web/INDEX.md`: web task index
- `docs/tasks/platform/2026-04-03-ai-workflow-bootstrap.md`: traceable record for this bootstrap task

## Review
Pre-implementation review was performed during repository inspection. The chosen adaptation keeps LoopNova's reusable workflow shape while explicitly rejecting a blind copy of its business-specific history and assumptions.

## Implementation
- created the repository-level AI workflow entry files
- created the `docs/` structure for context, engineering, tasks, testing, and archive layers
- added FreeNovel-specific context docs covering project overview, architecture, tech stack, and maintenance roadmap
- added engineering notes for API, database, and runtime operations
- added backend/browser validation guidance and a normalized task template
- created initial module indexes for platform, backend, and web work
- recorded this bootstrap as the first platform task record

## Validation
- documentation review only
- verified the expected workflow entry files and docs directories were created
- no backend API validation required because runtime behavior did not change
- no browser validation required because frontend behavior did not change

## Documentation Sync
Updated:
- `AGENTS.md`
- `AI_CONTEXT.md`
- `CLAUDE.md`
- `docs/README.md`
- `docs/context/project-overview.md`
- `docs/context/architecture.md`
- `docs/context/tech-stack.md`
- `docs/context/development-roadmap.md`
- `docs/engineering/api.md`
- `docs/engineering/database.md`
- `docs/engineering/runtime-operations.md`
- `docs/product/README.md`
- `docs/testing/backend-api-validation.md`
- `docs/testing/playwright-integration.md`
- `docs/tasks/TEMPLATE.md`
- `docs/tasks/platform/INDEX.md`
- `docs/tasks/backend/INDEX.md`
- `docs/tasks/web/INDEX.md`
- `docs/tasks/platform/2026-04-03-ai-workflow-bootstrap.md`

Checked with no change needed:
- `README.md`
- `HELP.md`
- `novel/pom.xml`
- `free-novel-web/package.json`

## Risks
- the workflow scaffold is now present, but the canonical docs still summarize a messy repo rather than a fully normalized one
- actual startup, validation, and deployment drift in source/runtime files still needs follow-up work
- older repository artifacts such as runtime logs, `node_modules`, and missing helper scripts remain unresolved

## Follow-ups
- create the first backend stabilization task to align startup docs with the actual runtime/build chain
- create the first web/backend contract task to document and harden the custom signature/auth flow
- decide whether `sql/` should later get its own task module once schema/data work becomes active
