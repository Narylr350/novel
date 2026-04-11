# Project Overview

## Product Goal
FreeNovel provides a translated online novel platform that combines crawling, translation, reading, notes, favorites, community features, export, and admin/operations capabilities.

The current maintenance direction treats those capabilities as two runtime shapes instead of one mixed surface:
- deployed / end-user runs should default to a streamlined reader-facing product
- development / maintenance runs may still expose the broader operator toolset

The product north star is one usable content loop:
1. collect source novels from upstream platforms
2. store and synchronize chapter data
3. translate and enrich content
4. expose readable web content and supporting features
5. let operators control crawlers, translation, uploads, and maintenance tasks

The now-confirmed long-term product framing is:
- `reader product` for normal users
- `maintainer platform` for acquisition, processing, and operational tooling

That maintainer platform is expected to evolve around:
- precision crawlers for complex sites
- rule-based book sources for lower-cost site expansion
- translation tools that no longer pollute the deployed runtime
- OCR fallback for image-first or anti-text sites

## Project Goal
This repository is now in a post-launch feature-delivery stage. The immediate goal is to keep improving the live reader product while only expanding maintainer-side capability where it directly supports content growth, quality, and operational efficiency.

The medium- and long-term goal is not to turn FreeNovel into a larger mixed-surface monolith. It is to keep the system usable as:
- a lightweight self-hosted reader site by default
- a clearer maintainer-facing content platform behind that reader site
- a codebase that can accept normal feature work without reopening release-era environment drift

## Active Applications
- `novel`: backend APIs, scheduled tasks, crawler/translator logic, persistence, file handling, and SPA hosting in packaged builds
- `free-novel-web`: Vue SPA that now serves both reader-facing flows and maintainer-facing flows, with the production-profile default converging on reader mode

## Supporting Assets
- `app/`: runtime file storage, temp files, and logs
- root Docker and compose files: local and external deployment variants
- database bootstrap/import assets should be treated as operational inputs when they exist, not as a guaranteed tracked source-tree surface

## Current Delivery Status
### Implemented
- Backend controller/service/repository structure for novels, chapters, auth, favorites, notes, tags, uploads, glossary, comments, messages, and platform configuration
- Scheduled crawler, translation, sitemap, and file-import task management
- Vue SPA routes for reading, search, favorites, uploads, glossary, history, translation config, and crawler manager
- Single-database and dual-database runtime configuration paths

### Known Gaps
- Documentation and runtime configuration are not yet aligned
- Startup workflows are inconsistent across README, HELP docs, Docker files, and current source
- Automated test coverage is effectively absent in this repository
- The frontend and backend auth/signature coupling is fragile and under-documented
- Post-launch product priorities need to stay reflected in canonical docs so future work does not keep defaulting back to release-package assumptions

## Repository Layout
- `novel/`
- `free-novel-web/`
- `app/`
- `docs/`

## Reading Continuation
For the required AI reading order, follow `AI_CONTEXT.md`.
