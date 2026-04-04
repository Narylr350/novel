# CLAUDE.md

This file mirrors the repository-level workflow guidance for AI coding sessions.

Keep durable project detail in `AI_CONTEXT.md`, `docs/context/`, and `docs/engineering/` instead of growing a second long-form guide here.

## Required Reading Order
For any non-trivial task, read in this order before implementation:
1. `AI_CONTEXT.md`
2. `docs/context/project-overview.md`
3. `docs/context/development-roadmap.md`
4. `docs/context/architecture.md`
5. `docs/context/tech-stack.md`
6. Relevant files in `docs/engineering/`
7. Relevant module `docs/tasks/<module>/INDEX.md`

## Execution Workflow
For complex tasks, keep following the repository workflow:
- Plan
- Design
- Implement
- Validate
- Document

For simple tasks, execute directly without unnecessary workflow overhead.

## Completion Standard
A task is not complete until all of the following are true:
- code or docs changes are implemented
- backend validation is complete when backend behavior changed
- browser validation is complete when frontend behavior changed
- task documentation is updated
- module `INDEX.md` is updated

## Local Dev Origins
- Web: `http://localhost:8080`
- Backend: `http://localhost:8081`

## Engineering Constraints
- Do not modify unrelated modules.
- Do not perform unrequested refactors.
- Do not silently expand scope.
- Do not skip validation.
- Treat runtime/configuration assumptions as suspect until verified from the current repo.
