# AGENTS.md

## Project Instructions

### Required Reading Order
For any non-trivial task, read in this order before implementation:
1. `AI_CONTEXT.md`
2. `docs/context/project-overview.md`
3. `docs/context/development-roadmap.md`
4. `docs/context/architecture.md`
5. `docs/context/tech-stack.md`
6. Relevant files in `docs/engineering/`
7. Relevant module `docs/tasks/<module>/INDEX.md`

`AI_CONTEXT.md` is the bootstrap index for AI sessions. Keep durable detail in the canonical files under `docs/context/` and `docs/engineering/`, and keep `AI_CONTEXT.md` concise.

### Module Boundary Rule
- Module grouping must follow the real repository structure.
- A task may span backend, web, configuration, docs, and validation when they belong to one business objective.
- Do not force every task into an artificial taxonomy that does not match `novel/`, `free-novel-web/`, `sql/`, and runtime/deployment files.

### Documentation Workflow
- Canonical long-lived context lives under `docs/context/` and `docs/engineering/`.
- Task history and current module state live under `docs/tasks/`.
- `docs/plans/` is not a canonical destination for new long-lived artifacts in this repository.
- When a skill suggests writing to `docs/superpowers/**`, fold that content into the active `docs/tasks/<module>/<task>.md` record unless a human explicitly asks for a separate artifact.
- Validation screenshots must live under `docs/testing/screenshots/`.
- AI-generated text logs and captured validation output must live under `docs/testing/ai-logs/`.
- Do not leave AI-generated logs or screenshots in the repository root or other ad hoc folders.
- Active development-facing docs should default to English unless a task explicitly requires another language.
- Every completed task must update:
  - the relevant task document
  - the relevant module `INDEX.md`
  - any affected context, engineering, or testing doc
- The task document must include a short `Documentation Sync` note listing updated docs and nearby docs checked with no change needed.
- Use `docs/tasks/TEMPLATE.md` for new task documents.

### Execution Workflow
For complex tasks, follow the execution order defined in `AI_CONTEXT.md` and apply these repository-specific rules:
- Keep the task document as the single source of task truth instead of creating parallel plan/spec trees.
- Treat the task document `Plan` section as the implementation plan and the `Design` section as the spec unless a separate artifact is explicitly requested.
- Capture file boundaries in the `File Structure` section before implementation when the task touches multiple files or layers.
- Use a lightweight plan/design review before implementation for multi-step, high-risk, or cross-layer tasks.
- Start complex tasks in this repository by invoking `using-superpowers` unless the user explicitly overrides that preference in the current conversation.

For simple tasks, execute directly without unnecessary workflow overhead.

### Context Refresh Rule
- For long, multi-step, or cross-layer tasks, re-read the minimum relevant instruction set:
  - before implementation starts
  - before validation starts
  - before final documentation sync / commit / push
- The minimum refresh set should usually include:
  - `AGENTS.md`
  - the active task document
  - any canonical doc directly governing the current task area

### Completion Standard
A task is not complete until all of the following are true:
- code or docs changes are implemented
- backend validation is complete when backend behavior changed
- browser validation is complete when frontend behavior changed
- task documentation is updated
- module `INDEX.md` is updated
- git changes are reviewed for scope correctness

### Delivery Direction
- `docs/context/development-roadmap.md` is the master document for delivery direction.
- Module `INDEX.md` files describe local progress; they do not replace the global roadmap.
- If a task does not clearly strengthen the current stabilization roadmap, stop and justify the deviation before implementing.

### Browser Acceptance
- Use browser MCP in the local environment, not Docker, when frontend behavior changed and local startup is practical.
- Default local origins are owned by `docs/context/tech-stack.md`.
- Frontend acceptance is desktop-first by default and should focus on a wide/maximized desktop viewport.
- If browser MCP is unavailable, fall back to HTTP smoke tests, logs, builds, and user-provided screenshots.

### Local Dev Origin
- Backend origin: `http://localhost:8081`
- Web origin: `http://localhost:8080`
- Do not introduce a new dev port or origin unless matching config and docs are updated in the same change.

### Engineering Constraints
- Do not modify unrelated modules.
- Do not perform unrequested refactors.
- Do not introduce new frameworks without approval.
- Do not silently expand scope.
- Do not skip validation.
- Do not leave local-only undocumented process changes.
- Treat the current repository as a legacy codebase with known documentation/runtime drift. Verify assumptions from source files before changing behavior.
- For backend API work, remember that most `/api/**` endpoints pass through the custom signature/auth flow unless explicitly excluded.
- For configuration or deployment work, verify `pom.xml`, Docker files, compose files, and docs together; this repository already has environment drift.

### Code Commenting Rule
- New or materially changed code should include short comments where the intent is not obvious from names alone.
- Prioritize comments for:
  - non-trivial business rules
  - auth or signature branches
  - crawler / scheduler side effects
  - data synchronization and storage paths
- Keep comments concise and explanatory. Explain why the code exists or what invariant it protects.

### Frontend Styling Rule
- Reuse the current project's visual language, shared styles, and existing components first.
- Do not introduce a fresh design system unless the user explicitly asks for one.
- Avoid default-looking framework output when the surrounding page already has established patterns.

### Local Preferences Already In Force
- Work in the main workspace.
- Default to UTF-8 when reading project files.
- If terminal output looks garbled, treat terminal encoding as suspect before assuming file corruption.
- Start complex tasks in this repository through the superpowers workflow unless the current user instruction explicitly overrides that preference.
- Execute simple tasks directly without requiring the superpowers workflow.
