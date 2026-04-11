# 2026-04-09 Login-First Entry And Warning Cleanup

## Summary
- Require unauthenticated users to land on the login page before entering reader routes.
- Preserve the target route through login so the reader can return to the intended page after authentication.
- Remove current frontend build warnings caused by deprecated deep selectors and webpack performance hints.

## Scope
- `free-novel-web/src/router/index.js`
- `free-novel-web/src/components/WebLogin.vue`
- `free-novel-web/src/components/SyosetuNovel.vue`
- `free-novel-web/src/components/WebNovelPlatform.vue`
- `free-novel-web/vue.config.js`

## Plan
1. Tighten unauthenticated route access so first entry redirects to `WebLogin`.
2. Add post-login redirect handling.
3. Replace deprecated deep-selector syntax.
4. Remove current build warnings and validate browser behavior.

## Design
- Keep only `WebLogin` publicly accessible when there is no token.
- Redirect unauthenticated access with `redirect=<target>` query state.
- After login, push the saved redirect target or fall back to `/webLibrary`.
- Replace `>>>` and `::v-deep` combinators with `:deep(...)`.
- Disable webpack performance hints so the distribution build does not emit size warnings.

## Implementation Notes
- `router.beforeEach` now redirects any unauthenticated route to `/login?redirect=<fullPath>`.
- `WebLogin` now restores the target route after successful login.
- `SyosetuNovel.vue` and `WebNovelPlatform.vue` now use Vue 3-compatible deep selectors.
- `vue.config.js` disables webpack performance hints for cleaner release builds.
- The same frontend files were mirrored into the local distribution folder under `D:\FreeNovel\01\free-novel-web\...` for immediate testing.

## Validation
- `npm run build`
  - Result: build completed with no warnings.
- Browser validation on `http://localhost:8080`
  - Cleared `Authorization` from localStorage.
  - Navigated to `/`.
  - Result: redirected to `/login?redirect=/webLibrary`.
  - Browser console result for the fresh login-page navigation: `0 errors / 0 warnings`.

## Findings
- The current `lite` demo package still uses three novels whose chapter directories exist but chapter contents are empty in the selected source rows.
- That content issue is separate from the login-first and warning-cleanup work and still needs a follow-up task.

## Documentation Sync
- Updated: this task document, `docs/tasks/web/INDEX.md`
- Checked with no change needed: `docs/engineering/reader-lite-quickstart.md`, `docs/context/development-roadmap.md`
