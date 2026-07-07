# Maintenance Notes

This file is a short compatibility note for maintainers. Treat `README.md` as the canonical source for current startup and environment guidance.

## Current Baseline

- Java: 21+
- Node.js: 20.x LTS preferred
- Frontend tooling: Vue CLI 5
- Database: MariaDB 11+ recommended

## Supported Entrypoints

- Local backend: `cd novel && mvn spring-boot:run -Pdev`
- Local frontend: `cd free-novel-web && npm install && npm run serve`

## Legacy Notes

- Historical PowerShell helper scripts are retired and should not be restored as the primary startup path.
- Older environment notes that mentioned Java 17, MySQL 8, or Vite-era assumptions are obsolete for the current repository state.
- Docker/Compose paths have been removed and are no longer maintained; deployment goes through source build or external images.
- Node 24 currently builds but emits package engine warnings; prefer Node 20 LTS for maintenance work.

## Next Reference

- `README.md`
- `.ai/PROJECT.md`
