# Development Roadmap

## Purpose
This document is the repository-level control document for delivery direction.

It exists to stop maintenance work from drifting into isolated local edits without reducing actual platform risk.

## Current Phase
Current phase: `Stabilization And Maintenance Baseline`

Meaning:
- the product already has broad surface area
- the main risk is not missing pages but inconsistent runtime assumptions, fragile cross-layer contracts, and poor change safety
- work should prefer environment alignment, validation baseline, and high-risk flow hardening over feature expansion

## Priority Rule
Prefer work that does one of the following:
1. makes the repo easier to start or validate
2. reduces cross-layer fragility in auth, signature, or API flow
3. clarifies database/runtime behavior and deployment modes
4. hardens crawler, translation, upload, or scheduler safety
5. adds enough documentation and task structure that future maintenance stops being blind work

## Current Mainline
The current mainline focus is:
- startup and environment truth alignment
- backend/frontend contract understanding
- validation baseline establishment
- reader-mode surface simplification for core reading flows
- targeted bug fixing on core user-facing paths only after the baseline is clearer

## Confirmed Long-Term Mainline
Once the stabilization baseline is strong enough, the confirmed product and system direction is:

1. `Reader Productization`
   - keep the published default as a streamlined reader-facing product
   - make startup, validation, and data packaging easy enough for small self-hosted deployments
   - keep reader features focused on reading, search, comments, favorites, notes, history, export, and Legado-facing compatibility

2. `Crawler Platformization`
   - preserve the current code-based crawler path for high-value or complex sites
   - turn crawler work into clearer adapters, shared task orchestration, and maintainable failure handling
   - prioritize maintainability and site depth over raw site count

3. `Rule-Source Expansion`
   - add a simpler rule-based source layer inspired by Legado-style book sources
   - use that layer for lower-cost site onboarding and community-maintainable source definitions
   - keep this as a complement to the precision crawler, not a replacement

4. `Translation Tooling Decoupling`
   - remove translation and terminology growth from the default reader-product dependency chain
   - redesign translation support as a maintainer tool, not a published requirement
   - replace the current large, low-trust terminology path with smaller, reviewable structures later

5. `OCR Fallback`
   - add an operator-only fallback for image-first or anti-text sites
   - use screenshot or page-capture acquisition, OCR extraction, and AI cleanup only when crawler and rule-source paths fail
   - treat OCR as a special-case recovery tool, not a default ingestion path

## Phase Ordering
The approved sequencing for this longer-term mainline is:
- short term: stabilize the reader product, database packaging, and local maintenance workflow
- medium term: platformize the crawler and introduce the first rule-source path
- long term: replace translation internals and add OCR fallback for special sites

This ordering is intentional. It prevents future work from skipping over the current repo baseline problems in favor of speculative feature expansion.

## Non-Priorities
The following are not default priorities unless they unblock the current phase:
- large visual redesigns
- speculative framework migrations
- broad refactors with no validation plan
- new product modules unrelated to maintenance or stability

## Phase Completion Standard
The current phase is complete only when:
- the canonical docs reflect how the repo actually runs
- the main local startup paths are understood and repeatable
- backend and frontend validation expectations are documented and usable
- core high-risk flows can be changed with less guesswork than today
