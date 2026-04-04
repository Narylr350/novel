# API Notes

## Backend Entry Surface
- Backend controllers live under `novel/src/main/java/com/wtl/novel/Controller/`.
- The application exposes a mixed API surface for auth, novels, chapters, notes, favorites, uploads, glossary, crawler controls, translation config, and admin-like operational pages.

## Current Contract Characteristics
- Most routes are under `/api/**`.
- The backend uses a custom request-signature flow enforced by `SignatureInterceptor`.
- `/api/auth/**` is excluded from the main interceptor path, while most other API routes are expected to carry the custom authorization/signature header format.
- Frontend request signing currently lives in `free-novel-web/src/api/axios.js` and `free-novel-web/src/utils/signature.js`.

## Maintenance Rules
- Treat auth and signature behavior as a backend/frontend contract, not a single-file concern.
- When changing controller request shape, inspect both frontend callers and interceptor behavior.
- When a task changes API behavior, record the affected endpoints in the task `Validation` section and use `docs/testing/backend-api-validation.md`.
