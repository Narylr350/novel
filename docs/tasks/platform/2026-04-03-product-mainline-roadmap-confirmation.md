# Task

## Scope
Confirm the long-term maintenance mainline for FreeNovel and record it in canonical repository documentation.

The confirmed direction must cover:
- reader-mode productization as the published default
- precision crawler retention for complex sites
- rule-based source support inspired by Legado-style source definitions
- translation tooling decoupled from the reader product
- OCR fallback for image-first or anti-text sites

## Out of Scope
- Implementing crawler refactors, rule-source parsing, translation replacement, or OCR
- Changing runtime behavior or feature flags
- Selecting specific OCR engines or translation model providers
- Deleting any database tables or shipping the lite/full package split

## Plan
1. Write a platform task record that captures the approved roadmap sections from the discussion.
2. Update the canonical roadmap so the stabilization phase now points toward the agreed long-term mainline.
3. Update project overview and architecture docs so future work reads the same system shape from canonical context.
4. Update the platform module index with the new roadmap-confirmation task.

## Design
The project direction is a phased mainline, not a flat feature list.

The confirmed product shape is:
- published runs default to a reader-facing product
- maintenance runs expose operator capabilities
- content acquisition and processing are layered behind that reader surface

The confirmed long-term system shape is:
1. reader product layer
2. precision crawler layer
3. rule-source layer
4. translation tool layer
5. OCR fallback layer

The execution order is intentionally uneven:
- short term: stabilize startup, database packaging, and core reading flows
- medium term: platformize the crawler and introduce rule-based source support
- long term: replace the current translation/terminology path and add OCR fallback for special sites

This keeps the project aligned with the current stabilization phase while still documenting the intended future architecture.

## File Structure
- `docs/tasks/platform/2026-04-03-product-mainline-roadmap-confirmation.md`
  - task record for the confirmed long-term direction
- `docs/tasks/platform/INDEX.md`
  - module status entry for the roadmap confirmation
- `docs/context/development-roadmap.md`
  - canonical delivery direction and phase sequencing
- `docs/context/project-overview.md`
  - product goal and maintenance goal framing
- `docs/context/architecture.md`
  - long-term layer boundaries across reader, crawler, rule-source, translation, and OCR

## Review
The roadmap was reviewed in conversation before writing:
- the user approved the recommended phased approach
- the user approved the phase boundaries and success criteria
- the user approved the five-layer system shape
- the user approved the near / medium / long execution order

No separate implementation review gate was needed because this task only updates canonical documentation.

## Implementation
- Added this task record to capture the approved roadmap and architecture direction.
- Updated the canonical roadmap to distinguish the current stabilization phase from the confirmed long-term mainline.
- Updated project overview and architecture docs so future maintenance work starts from the same product/system framing.
- Registered the task in the platform module index.

## Validation
- Reviewed updated documentation for internal consistency across roadmap, project overview, and architecture.
- No backend, browser, or database validation was required because this task changes documentation only.

## Documentation Sync
Updated:
- `docs/tasks/platform/2026-04-03-product-mainline-roadmap-confirmation.md`
- `docs/tasks/platform/INDEX.md`
- `docs/context/development-roadmap.md`
- `docs/context/project-overview.md`
- `docs/context/architecture.md`

Checked with no change needed:
- `AI_CONTEXT.md`
- `docs/context/tech-stack.md`
- `docs/tasks/backend/INDEX.md`
- `docs/tasks/web/INDEX.md`

## Risks
- The roadmap is now explicit, but feature-level specs are still needed before crawler platformization, rule-source parsing, translation replacement, or OCR work begins.
- Legado-inspired source support may have compatibility and licensing constraints that still need explicit review later.
- OCR fallback is intentionally under-specified at the tooling level until there is a dedicated design task.

## Follow-ups
- Finish the full-database import and run the promised table audit before data packaging work continues.
- Create a dedicated crawler-platformization design task for adapter boundaries and task orchestration.
- Create a dedicated rule-source design task for the simplified Legado-like source model.
- Create a dedicated translation-system replacement design task before touching the current terminology path.
- Create a dedicated OCR fallback design task once the crawler and rule-source layers are clearer.
