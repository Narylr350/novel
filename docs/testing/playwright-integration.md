# Playwright Integration

## Purpose
Browser MCP / Playwright validation is the default front-end integration gate for FreeNovel when frontend behavior changes. Its job is to prove that the page, API wiring, and main user flow are actually usable.

## Scope
Use browser validation first for:
- rendering failures
- console errors
- network errors
- broken navigation
- missing key elements
- obvious layout overlap

## Required Coverage
Every frontend-affecting task should cover:

### Smoke Check
At minimum verify:
- page opens successfully
- first screen renders
- key API requests return expected success status
- no obvious console error blocks the page

### Business Flow
Validate the main user flow changed by the task.

Typical pattern:
1. Open page
2. Fill required fields
3. Submit action
4. Verify visible result
5. Refresh
6. Re-check persistence or state recovery when relevant

### Responsive Coverage
If the change can affect layout, panel sizing, table readability, or detail rendering, validate at least one wide/maximized desktop viewport such as `1920x1080`.

## Evidence Storage
If screenshots are retained:
- store them under `docs/testing/screenshots/`
- do not keep acceptance screenshots in the repository root or `app/logs/`
- reference the stored path in the task document

If AI-captured text logs are retained:
- store them under `docs/testing/ai-logs/`
- do not mix text logs into `docs/testing/screenshots/`
- reference the stored path in the task document

## Local Environment
- Local origins are owned by `docs/context/tech-stack.md`.
- This repository does not yet define fixed acceptance accounts at the workflow level; use the task's actual available credentials and record them carefully without leaking secrets into committed docs.

## Completion Gate
A frontend-affecting task is not complete if:
- only build/test passed but the browser flow was not checked
- the main business flow was not executed in-browser
- console or network errors were seen and not explained
