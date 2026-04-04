# AI_CONTEXT

## Purpose
This file is the mandatory AI bootstrap entry for the repository.

It is intentionally short. Do not duplicate long-form project context here when the canonical version already lives under `docs/context/` or `docs/engineering/`.

## Project
FreeNovel is an AI-assisted maintenance and stabilization project for a translated online novel platform.

Active application roots:
- `novel`: Spring Boot backend
- `free-novel-web`: Vue web frontend

Supporting roots:
- `sql`: database bootstrap and large data imports
- `app`: local runtime directories for logs, files, and temp storage

## Required Reading Order
For any non-trivial task, read in this order before implementation:
1. `AI_CONTEXT.md`
2. `docs/context/project-overview.md`
3. `docs/context/development-roadmap.md`
4. `docs/context/architecture.md`
5. `docs/context/tech-stack.md`
6. Relevant files under `docs/engineering/`
7. Relevant module `docs/tasks/<module>/INDEX.md`

## Execution Workflow
After reading context, execute in this order:
1. Define scope
2. Produce plan
3. Produce design
4. Review the task document plan/design before implementation when the task is multi-step, high-risk, or crosses module boundaries
5. Implement code and docs
6. Run backend validation when backend behavior changed
7. Run browser validation when frontend behavior changed
8. Save screenshots under `docs/testing/screenshots/` when kept
9. Save AI-captured logs under `docs/testing/ai-logs/` when kept
10. Update the relevant task document
11. Update the relevant module `INDEX.md`

Repository adaptation for updated superpowers skills:
- Keep task execution records in `docs/tasks/<module>/<task>.md` instead of creating `docs/superpowers/**` by default.
- Treat the `Plan` and `Design` sections of the task document as the active plan/spec unless a separate document is explicitly requested.
- Record file boundaries and review notes inside the task document when the task spans multiple files or layers.

## Non-Negotiable Rules
- Prefer work that reduces operational risk, startup drift, and hidden runtime breakage.
- Work on one business goal at a time, even when multiple areas are touched.
- Do not skip required validation.
- For version-sensitive work, verify behavior against primary sources before implementation.
