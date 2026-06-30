# Backend Maintenance Notes

This file is a short compatibility note for the `novel` backend. Treat the root `README.md`, `.ai/PROJECT.md`, and `docs/engineering/runtime-operations.md` as the broader runtime guidance.

## Current Baseline

- Java: 21+
- Spring Boot: 4.0.0
- Database: MariaDB 11+ recommended
- Backend port: `8081` by default
- Runtime directory: root `app/` for logs, temporary uploads, and stored files

## Local Backend Startup

```powershell
cd novel
mvn spring-boot:run -Pdev
```

The `-Pdev` Maven profile is intentional because `application.properties` is filtered during build/startup.

## Environment Examples

- Single database example: `novel/.env.example`
- Dual database example: `novel/.env.dual.example`
- Compose/shared example: root `.env.example`

Current profile and CORS variables use Spring Boot names:

```text
SPRING_PROFILES_ACTIVE=prod
APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:8080
```

## Important Runtime Variables

- `DATABASE_MODE=single|dual`
- `PRIMARY_DB_URL`, `PRIMARY_DB_USERNAME`, `PRIMARY_DB_PASSWORD`
- `SECONDARY_DB_URL`, `SECONDARY_DB_USERNAME`, `SECONDARY_DB_PASSWORD` for dual database mode
- `PROXY_HOST`, `PROXY_PORT`, `PROXY_CLIENT` for optional outbound proxy access
- `TASK_SCHEDULER_ENABLED=false` by default; enable scheduler/crawler/file-import tasks explicitly only when intended

## Legacy Notes

- Java 17, Node 22, and MySQL 8 notes are obsolete for the current repository baseline.
- Historical proxy snippets and SQL package notes are not the canonical startup path.
- Keep high-side-effect crawler, scheduler, translation, upload, sitemap, and file-import changes aligned with current Spring properties and compose files.
