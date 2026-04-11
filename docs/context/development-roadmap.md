# Development Roadmap

## Purpose
This document is the repository-level control document for delivery direction.

It exists to stop normal post-launch work from drifting into isolated local edits, release-surface cleanup loops, or speculative rewrites that do not improve the shipped product.

## Current Phase
Current phase: `Post-Launch Feature Delivery`

Meaning:
- the product is already live and no longer in a release-prep stage
- work should prioritize shipped reader value, maintainer efficiency, and safe iteration speed
- environment alignment and runtime hardening still matter, but mainly when they unblock or de-risk real product work

## Priority Rule
Prefer work that does one of the following:
1. improves active reader-facing product flows
2. improves maintainer-side throughput for content intake, translation, moderation, or operations when that work supports reader value
3. reduces cross-layer fragility in auth, signature, runtime, or API flow when it blocks normal feature delivery
4. keeps deployment/runtime/docs aligned enough that feature work stays safe and repeatable
5. adds just enough documentation and task structure that future delivery stops being blind work

## Current Mainline
The current mainline focus is:
- reader-side experience improvements on the live product surface
- targeted bug fixing on core user-facing and operator-facing flows
- maintainer tooling that directly supports content acquisition, processing, and review
- incremental contract/runtime cleanup only where it unblocks current feature work
- continued retirement of release-oriented documentation assumptions from the source repo

## Confirmed Long-Term Mainline
The confirmed product and system direction after launch is:

1. `Reader Product Continuation`
   - keep the deployed default as a streamlined reader-facing product
   - keep startup and validation simple enough for small self-hosted deployments
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
   - redesign translation support as a maintainer tool, not a deployment requirement
   - replace the current large, low-trust terminology path with smaller, reviewable structures later

5. `OCR Fallback`
   - add an operator-only fallback for image-first or anti-text sites
   - use screenshot or page-capture acquisition, OCR extraction, and AI cleanup only when crawler and rule-source paths fail
   - treat OCR as a special-case recovery tool, not a default ingestion path

## Phase Ordering
The approved sequencing for this longer-term mainline is:
- short term: iterate on the live reader product and fix delivery-blocking drift
- medium term: platformize the crawler and introduce the first rule-source path
- long term: replace translation internals and add OCR fallback for special sites

This ordering is intentional. It prevents future work from skipping over the current repo baseline problems in favor of speculative feature expansion.

## Non-Priorities
The following are not default priorities unless they unblock the current phase:
- large visual redesigns
- speculative framework migrations
- broad refactors with no validation plan
- rebuilding release/distribution workflows inside the source repository
- new product modules unrelated to the current reader or maintainer mainline

## Phase Completion Standard
The current phase is complete only when:
- the canonical docs reflect the post-launch product mainline and how the repo actually runs
- normal reader and maintainer feature work can be shipped without rediscovering environment truth each time
- backend and frontend validation expectations are documented and usable
- core high-risk flows can be changed with less guesswork than today
