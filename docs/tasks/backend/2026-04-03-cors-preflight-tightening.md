# Task

## Scope
Tighten CORS and preflight handling for reader-focused deployments by replacing wildcard origin behavior with an explicit allowed-origin policy shared by Spring MVC CORS config and the early OPTIONS filter path.

## Out of Scope
- Changing token or signature validation
- Introducing role-based API authorization
- Auditing every response header or security header
- Refactoring non-CORS request filtering
- Browser UI changes

## Plan
- Record the current CORS/preflight problem and file boundaries in this task document.
- Add focused failing tests for allowed-origin parsing and preflight responses before changing production code.
- Introduce one shared backend CORS policy source and wire both `WebConfig` and `RequestFilter` to it.
- Add explicit dev/prod allowed-origin properties, validate backend packaging/tests, and sync docs/indexes.

## Design
The repository currently has two conflicting CORS surfaces:
- `WebConfig` allows `*` for all origins
- `RequestFilter` answers every `OPTIONS` request with `Access-Control-Allow-Origin: *`

This task unifies them behind one shared policy object that parses a comma-separated property such as:
- dev: `http://localhost:8080`
- prod: empty by default, with operators opting in through environment variables

The shared policy keeps the change low risk:
- one parsing and origin-match implementation
- one source of truth for Spring MVC CORS registration
- one source of truth for early preflight handling in the filter

Published reader-mode deployments should not advertise cross-origin API access unless a maintainer explicitly configures allowed origins. Same-origin packaged deployments continue to work because they do not rely on CORS.

## File Structure
- `novel/src/main/java/com/wtl/novel/Config/CorsPolicy.java`: shared parsing and origin-match logic for configured CORS origins
- `novel/src/main/java/com/wtl/novel/Config/WebConfig.java`: register Spring MVC CORS only when explicit origins are configured
- `novel/src/main/java/com/wtl/novel/filter/RequestFilter.java`: answer preflight requests through the shared policy instead of wildcard headers
- `novel/src/main/resources/application-dev.properties`: set explicit dev frontend origin
- `novel/src/main/resources/application-prod.properties`: leave prod origin list empty unless configured
- `novel/src/test/java/com/wtl/novel/Config/CorsPolicyTest.java`: regression tests for parsing and origin matching
- `novel/src/test/java/com/wtl/novel/filter/RequestFilterCorsTest.java`: regression tests for allowed and blocked preflight requests
- `docs/engineering/runtime-operations.md`: record the new explicit-origin rule
- `docs/tasks/backend/INDEX.md`: backend module index update

## Review
Pre-implementation review stayed lightweight. This task directly supports the stabilization roadmap by reducing published runtime exposure and removing one known config/filter mismatch without changing application business behavior.

## Implementation
- Added a shared `CorsPolicy` component that parses configured origin patterns and matches explicit or wildcard origins from one place.
- Wired `WebConfig` to register Spring MVC CORS only when explicit origin patterns are configured instead of advertising `*`.
- Updated `RequestFilter` preflight handling to use the same policy, return the matched origin, and reject disallowed cross-origin preflight requests with `403`.
- Added explicit profile defaults:
  - dev allows `http://localhost:8080`
  - prod leaves the origin list empty unless configured through `APP_CORS_ALLOWED_ORIGIN_PATTERNS`

## Validation
- `mvn "-Dtest=CorsPolicyTest,RequestFilterCorsTest" test`
- `mvn -DskipTests package -Pdev`
- Browser validation not run. This task changes backend CORS behavior only and does not alter visible frontend UI.

## Documentation Sync
- Updated: `docs/engineering/runtime-operations.md`
- Updated: `docs/tasks/backend/INDEX.md`
- Updated: `docs/tasks/backend/2026-04-03-cors-preflight-tightening.md`
- Checked with no change needed: `README.md`, `docs/context/tech-stack.md`, `docs/tasks/backend/2026-04-03-runtime-hardening-safe-defaults.md`

## Risks
- This task narrows origin policy only. It does not review every allowed method/header combination.
- Operators who rely on cross-origin access in prod will need to set the new origin property explicitly.

## Follow-ups
- Audit whether `allowedHeaders("*")` and `allowedMethods("*")` should also be narrowed after reader-mode validation is more complete.
- Revisit CORS behavior for direct file download endpoints that currently support token query parameters.
