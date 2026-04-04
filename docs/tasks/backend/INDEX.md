# Backend Module Index

## Scope
Backend tasks cover `novel/`, backend runtime configuration, API contracts, scheduler/crawler/translation logic, and database-facing behavior.

## Current Status
- Local database import investigation started on 2026-04-03
- Canonical local SQL import entrypoint added on 2026-04-03
- Reader-focused runtime defaults hardened on 2026-04-03
- CORS and preflight handling tightened on 2026-04-03
- Lite database package strategy defined on 2026-04-03
- Full database import completed and the first table classification audit was recorded on 2026-04-04
- Full data-flow validation on the imported database completed and reader bootstrap scope was reduced on 2026-04-04
- Executable lite database export/import tooling started on 2026-04-04
- The generated `novel_lite_validation` package is now validated through a real logged-in chapter-detail flow on 2026-04-04
- Reader homepage bootstrap endpoints were reopened for unauthenticated SPA loading on 2026-04-03
- Anonymous reader book-list loading was normalized for the web library on 2026-04-03
- No backend behavior changes beyond the reader-mode work have been normalized yet

## Active Task Records
- [2026-04-03-local-db-import-investigation.md](./2026-04-03-local-db-import-investigation.md)
- [2026-04-03-runtime-hardening-safe-defaults.md](./2026-04-03-runtime-hardening-safe-defaults.md)
- [2026-04-03-cors-preflight-tightening.md](./2026-04-03-cors-preflight-tightening.md)
- [2026-04-03-lite-database-package-design.md](./2026-04-03-lite-database-package-design.md)
- [2026-04-04-full-database-audit-and-table-classification.md](./2026-04-04-full-database-audit-and-table-classification.md)
- [2026-04-04-full-data-flow-validation-and-reader-bootstrap-scope.md](./2026-04-04-full-data-flow-validation-and-reader-bootstrap-scope.md)
- [2026-04-04-lite-database-export-implementation.md](./2026-04-04-lite-database-export-implementation.md)
- [../web/2026-04-03-frontend-console-error-investigation.md](../web/2026-04-03-frontend-console-error-investigation.md)

## Notes
- Use this module for backend behavior, persistence, runtime config, and operational safety tasks.
