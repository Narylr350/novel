# Runtime Operations

## Supported Startup Paths
Current repo evidence points to two supported runtime paths:
- direct local development using `novel` and `free-novel-web`
- Docker / compose deployment from the repository root

Supported compose entrypoints:
- `docker-compose.local-single.yml`
- `docker-compose.local-dual.yml`
- `docker-compose.external-single.yml`
- `docker-compose.external-dual.yml`

There is no canonical root `docker-compose.yml`.

The source repository no longer treats any of the following as active runtime entrypoints:
- `release\`
- root-level beginner `.cmd` wrappers
- source-package distribution directories
- repository-tracked SQL install packages or release assembly scripts

If those artifacts still exist outside this repository, treat them as external operational assets and verify them separately from current source.

## Default Ports
- Web: `8080`
- Backend: `8081`
- Database: commonly `3306` in local compose

## Current Risks
- The repository worktree already contains runtime artifacts such as logs and installed dependencies.
- Local compose entrypoints build the current repository source through the root `Dockerfile`.
- External compose entrypoints still consume the published application image and remain a separate deployment path.
- Some frontend packages emit engine warnings on Node 24, so Node 20 LTS is the safer maintenance baseline.
- Production-profile defaults are reader-focused: scheduler, crawler, sitemap, and file-import tasks stay off unless a maintainer explicitly enables them through environment variables or runtime dictionary switches.
- Cross-origin API access is now explicit instead of wildcard-based:
  - dev default: `APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:8080`
  - prod default: empty, which means no cross-origin API access is advertised unless an operator configures allowed origins
  - same-origin packaged deployments are unaffected because they do not rely on CORS

## Maintenance Rules
- Before changing runtime behavior, inspect source config, compose files, and docs together.
- When starting services for validation, capture logs under `docs/testing/ai-logs/` if they need to be retained.
- If a task only changes docs or workflow, do not claim runtime validation that was not actually executed.
- For source-aligned local compose runs, prefer `docker compose -f docker-compose.local-single.yml up --build -d` or the dual-database equivalent so image rebuilds track current code changes.
