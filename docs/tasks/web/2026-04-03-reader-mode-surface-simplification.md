# Task

## Scope
Introduce a product-level split between `reader mode` and `maintainer mode` so the published app defaults to a streamlined reading experience, while advanced maintenance and site-operator features remain available in development or maintenance-oriented runs.

## Out of Scope
- Removing backend controllers or disabling backend endpoints
- Refactoring crawler, translation, upload, glossary, or scheduler internals
- Database schema redesign or data-package redesign
- Role-based authorization redesign
- Docker and SQL import workflow redesign

## Plan
### Task 1: Backend App Mode Contract

**Files:**
- Modify: `novel/src/main/resources/application-dev.properties`
- Modify: `novel/src/main/resources/application-prod.properties`
- Create: `novel/src/main/java/com/wtl/novel/Controller/AppModeController.java`
- Check: `novel/src/main/java/com/wtl/novel/Config/WebConfig.java`

- [ ] **Step 1: Define one canonical runtime mode property**

Add one property to each profile:

```properties
# application-dev.properties
app.ui.mode=${APP_UI_MODE:maintainer}
```

```properties
# application-prod.properties
app.ui.mode=${APP_UI_MODE:reader}
```

- [ ] **Step 2: Expose the mode through a lightweight public API**

Create a controller on an auth-excluded path so the frontend can bootstrap without the custom signature flow:

```java
@RestController
@RequestMapping("/api/auth")
public class AppModeController {

    @Value("${app.ui.mode:reader}")
    private String appUiMode;

    @GetMapping("/app-mode")
    public Map<String, String> appMode() {
        return Map.of("mode", appUiMode);
    }
}
```

- [ ] **Step 3: Verify the endpoint stays outside the signature interceptor**

Check that `WebConfig` still excludes `/api/auth/**` and therefore does not require extra interceptor changes.

Run:

```powershell
rg -n "/api/auth/\\*\\*" novel/src/main/java/com/wtl/novel/Config/WebConfig.java
```

Expected: the auth path remains excluded.

### Task 2: Frontend Runtime Mode Bootstrap

**Files:**
- Create: `free-novel-web/src/config/appMode.js`
- Modify: `free-novel-web/src/main.js`
- Check: `free-novel-web/src/api/axios.js`

- [ ] **Step 1: Create a single frontend mode helper**

Create one helper that fetches and validates the backend mode:

```javascript
import axios from 'axios';

const ALLOWED_MODES = ['reader', 'maintainer'];

export async function loadAppMode() {
  const response = await axios.get('/api/auth/app-mode');
  const mode = response?.data?.mode;
  return ALLOWED_MODES.includes(mode) ? mode : 'reader';
}
```

- [ ] **Step 2: Inject the mode before mounting the app**

Update `main.js` so the app mounts only after the mode is loaded:

```javascript
const app = createApp(App);
const mode = await loadAppMode();
app.provide('appMode', mode);
app.config.globalProperties.$appMode = mode;
```

- [ ] **Step 3: Verify the bootstrap still compiles**

Run:

```powershell
cd free-novel-web
npm run build
```

Expected: Vue build succeeds.

### Task 3: Router And Landing-Page Gating

**Files:**
- Modify: `free-novel-web/src/router/index.js`
- Check: `novel/src/main/java/com/wtl/novel/Controller/SpaController.java`

- [ ] **Step 1: Tag maintainer-only routes with route metadata**

For maintainer pages, add explicit route metadata:

```javascript
meta: { appMode: 'maintainer' }
```

Apply this to:
- `SyosetuNovel`
- `NovelPiaNovel`
- `TranslationConfig`
- `CrawlerManager`
- `UploadAndShare`
- `UploadNovelDetail`
- `UploadNovelEdit`
- `UploadChapterAdmin`
- `UploadChapterEdit`
- `GlossaryPage`
- `UserGlossaryPage`
- `WebNovelPlatform`
- `TagFilter`
- `MessageView`
- `WebStore`
- `BlacklistPage`
- `InfoList`
- `WriterDetail`
- community-feed routes that are not required for core reading

- [ ] **Step 2: Make reader mode land on reading, not community**

When the loaded mode is `reader`, visiting `/` should resolve to the reading surface rather than the current post/community feed:

```javascript
if (to.path === '/' && appMode === 'reader') {
  next({ name: 'WebLibrary' });
  return;
}
```

- [ ] **Step 3: Block direct access to maintainer pages in reader mode**

In the global route guard:

```javascript
if (to.meta.appMode === 'maintainer' && appMode === 'reader') {
  next({ name: 'WebLibrary' });
  return;
}
```

- [ ] **Step 4: Re-check SPA forwarding only if route paths change**

If the task only changes frontend route metadata and guarding, leave `SpaController` unchanged. If any route path is removed or repurposed, update forwarding patterns accordingly.

### Task 4: Global Navigation Simplification

**Files:**
- Modify: `free-novel-web/src/App.vue`

- [ ] **Step 1: Remove maintainer entries from reader mode navigation**

Gate these top-nav entries behind maintainer mode:
- upload
- 汉化
- 消息
- Cookie

Use one shared condition:

```vue
<li v-if="isLoginTag && isMaintainerMode">
```

- [ ] **Step 2: Clean the user dropdown for reader mode**

Gate these dropdown items behind maintainer mode:
- 标签过滤
- 积分兑换
- 拉黑
- 翻译配置
- 爬虫管理

Keep reader-mode items:
- personal info
- history
- logout

- [ ] **Step 3: Remove message polling in reader mode**

Only call `/api/msg/getMessage` when maintainer mode exposes the message center.

### Task 5: Shared Page Entry-Point Cleanup

**Files:**
- Modify: `free-novel-web/src/components/NovelDetail.vue`
- Modify: `free-novel-web/src/components/UserDetail.vue`
- Modify: `free-novel-web/src/components/WebNovelPlatform.vue`
- Check: `free-novel-web/src/components/RecommendationList.vue`
- Check: `free-novel-web/src/components/RecommendationDetail.vue`

- [ ] **Step 1: Strip maintainer actions from the novel detail page in reader mode**

Hide these buttons in `reader mode`:
- 获取新章节
- AI术语
- 用户术语
- 重新汉化
- title/tag editing controls

Keep:
- read / continue reading
- favorites
- TXT export

- [ ] **Step 2: Reduce user-center tabs to reader-safe surfaces**

Hide or remove these tabs in `reader mode`:
- 我的书帖
- 我的Cookie

Keep:
- profile
- password
- reading preferences
- invite code area only if it is still considered part of lightweight user onboarding

- [ ] **Step 3: Hide the platform/conversion page from reader mode**

`WebNovelPlatform` should become maintainer-only because it is currently an operations/conversion surface, not a reading surface.

- [ ] **Step 4: Re-evaluate community feed pages during implementation**

If `RecommendationList` and `RecommendationDetail` are only serving the old community/post flow, keep them out of reader mode and do not link to them from the reader surface. Preserve existing data without surfacing the feed by default.

### Task 6: Documentation And Validation Sync

**Files:**
- Modify: `docs/tasks/web/2026-04-03-reader-mode-surface-simplification.md`
- Modify: `docs/tasks/web/INDEX.md`
- Modify: `README.md`
- Modify: `docs/context/project-overview.md`
- Modify: `docs/context/development-roadmap.md`

- [ ] **Step 1: Document the published default**

Update docs so they explicitly state:
- published/prod runs default to reader mode
- maintainer mode is for developers and site operators
- phase one only changes frontend visibility, not backend endpoint registration

- [ ] **Step 2: Validate the final surface**

Run:

```powershell
cd novel
mvn -DskipTests package -Pdev
```

```powershell
cd free-novel-web
npm run build
```

Then run browser acceptance against:
- reader mode: maintainer routes blocked and hidden
- maintainer mode: advanced pages still reachable

## Design
### Product Shape
The application will formally support two runtime shapes:

- `Reader Mode`
  Default for published/local end-user deployments. Visible capabilities:
  - login and registration
  - library browsing and search
  - novel detail and chapter reading
  - reading history
  - favorites
  - notes
  - comments, including historical comments already stored in data
  - TXT export
  - Legado / 阅读3.0 book-source access

- `Maintainer Mode`
  Used for active maintenance, content operations, and station operation work. Visible capabilities:
  - crawler controls
  - translation controls
  - upload/share flows
  - glossary and terminology flows
  - platform configuration and filters
  - scheduler/maintenance pages
  - other non-reader operational pages

This split changes default visibility, not backend availability. In the first phase, backend endpoints remain registered and callable; only the frontend surface is reduced.

### Routing Boundary
Reader-facing routes remain available in `reader mode`:
- `/` as a landing alias that redirects to the reading library
- `/login`
- `/search`
- `/webLibrary`
- `/webLibraryNp`
- `/novelDetail/:id`
- `/chapterDetail/:id`
- `/favorites`
- `/webHistory`
- `/webNote`
- `/noteDetail/:id`
- `/userDetail`
- `/modifyPassword`

Maintainer-only routes become hidden and blocked in `reader mode`:
- `/syosetuNovel`
- `/novelPiaNovel`
- `/translationConfig`
- `/crawlerManager`
- `/uploadAndShare`
- `/uploadNovelDetail`
- `/uploadNovelEdit/:id`
- `/uploadChapterAdmin/:id`
- `/uploadChapterEdit/:id`
- `/glossaryPage/:id`
- `/userGlossaryPage/:id`
- `/novelPlatform`
- `/tagFilter`
- `/messageView`
- `/webStore`
- `/blacklistPage`
- `/infoList`
- `/writerDetail/:id`
- `/recommendationDetail/:id` stays reader-accessible because the current comment flow still drills into post detail from novel pages

Comments remain part of the reader product. Community-adjacent features that are not core to reading should move behind maintainer mode unless a later task deliberately reintroduces them to reader mode.

### Configuration Strategy
The mode should not be inferred from frontend build tooling alone. Instead:
- backend runtime configuration provides one canonical mode value
- the frontend reads that value once during bootstrap
- route registration, route guards, menus, and page-level controls all consume the same mode contract

Allowed values:
- `reader`
- `maintainer`

Default mapping:
- production configuration -> `reader`
- development configuration -> `maintainer`

This keeps the split aligned with the repository's existing `dev` / `prod` profile structure and avoids introducing a new permission system in the first phase.

### UI Gating Strategy
Mode gating must happen at three levels:
- navigation level: hide maintainer pages from menus and obvious entry points
- route level: prevent direct access in `reader mode`
- component level: hide advanced action buttons that would otherwise expose maintainer flows from shared pages

This is necessary because route hiding alone would still leave maintainer actions discoverable inside mixed pages.

### Tradeoffs
Recommended approach: profile-driven mode split without backend endpoint removal.

Benefits:
- lowest-risk path in a legacy codebase
- directly matches the existing configuration split
- quickly changes the product shape seen by end users
- keeps maintainer capability available for active development

Accepted limitation:
- maintainer endpoints still exist in published runs; they are simply not surfaced by the frontend in phase one

## File Structure
- `novel/src/main/resources/application-dev.properties`: maintainer-mode default for development runs
- `novel/src/main/resources/application-prod.properties`: reader-mode default for published runs
- `novel/src/main/java/com/wtl/novel/Controller/AppModeController.java`: lightweight runtime config endpoint for frontend bootstrap
- `free-novel-web/src/config/appMode.js`: validated app-mode loader and helper surface
- `free-novel-web/src/main.js`: app bootstrap wiring for the canonical mode value
- `free-novel-web/src/router/index.js`: route metadata, landing-route behavior, and reader/maintainer route gating
- `free-novel-web/src/App.vue`: top-level navigation/menu visibility by mode
- `free-novel-web/src/components/NovelDetail.vue`: remove maintainer-only buttons from the default reader surface
- `free-novel-web/src/components/UserDetail.vue`: remove maintainer-only tabs and controls from the default reader surface
- `free-novel-web/src/components/WebNovelPlatform.vue`: maintainer-only conversion/platform surface
- `free-novel-web/src/components/RecommendationList.vue`: legacy community feed review during reader-mode cleanup
- `free-novel-web/src/components/RecommendationDetail.vue`: legacy community detail review during reader-mode cleanup
- `novel/src/main/java/com/wtl/novel/Controller/SpaController.java`: keep SPA forwarding aligned with the retained route surface if route paths change
- `docs/tasks/web/2026-04-03-reader-mode-surface-simplification.md`: active design/spec and execution record
- `docs/tasks/web/INDEX.md`: module status tracking

## Review
Pre-implementation design review completed in conversation.

Corrections applied during review:
- keep login-time lightweight reader features instead of reducing to anonymous browsing only
- preserve comments for local deployments, including historical comments already present in data
- keep TXT export and Legado / 阅读3.0 support in the default reader product
- use existing configuration profiles to separate product surface
- keep backend interfaces registered in phase one; hide and route-block only at the frontend layer
- resolve the ambiguous homepage behavior by making reader-mode `/` land on the library instead of the old community feed

## Implementation
- Added a backend mode contract with `app.ui.mode` profile defaults and a public `/api/auth/app-mode` endpoint on the auth-excluded path.
- Added a frontend app-mode helper that normalizes `reader` / `maintainer`, trims accidental whitespace from environment values, and centralizes landing-route / maintainer-route redirects.
- Updated SPA bootstrap to load app mode before mounting so route guards and global navigation read one canonical mode source.
- Tagged maintainer-only routes in `free-novel-web/src/router/index.js` and redirected reader-mode access to `WebLibrary`.
- Kept `RecommendationDetail` reachable because novel comments still link into that page, while the `/` community feed remains maintainer-only and reader mode lands on the library.
- Simplified `App.vue` navigation for reader mode by hiding upload, translation, message, cookie, and maintainer-only dropdown entries, and by skipping message polling outside maintainer mode.
- Simplified mixed reader pages:
  - `NovelDetail.vue` now hides maintainer-only actions and the pending-translation section in reader mode while keeping read, favorite, comment, and TXT export actions.
  - `UserDetail.vue` now hides `我的书帖` and `我的Cookie` tabs outside maintainer mode.

## Validation
- `node tests/appMode.spec.mjs`
  - red: failed before `src/config/appMode.mjs` existed
  - green: passed after helper implementation
  - red/green repeated for whitespace-trimming normalization
- `mvn -Dtest=AppModeControllerTest test`
  - red: failed with `ClassNotFoundException` before `AppModeController` existed
  - green: passed after backend endpoint implementation
- `rg -n "/api/auth/\\*\\*" novel/src/main/java/com/wtl/novel/Config/WebConfig.java`
  - confirmed `/api/auth/**` remains excluded from the signature interceptor
- `mvn -DskipTests package -Pdev`
  - passed
- `npm run build`
  - passed with existing bundle-size and deprecated deep-selector warnings
- Browser acceptance via local Vue dev server on `http://localhost:8080` with a temporary stub backend on `http://localhost:8081`
  - reader mode:
    - `/` redirected to `/webLibrary`
    - `/crawlerManager` redirected back to `/webLibrary`
    - nav exposed only `收藏 / 看书 / 搜索 / np排行 / 用户`
    - `/userDetail` showed only `个人资料 / 修改密码`
    - `/novelDetail/1` showed only `阅读 / 收藏 / 发帖 / 下载小说`, and hid `待汉化目录`
  - maintainer mode:
    - `/` stayed on the community feed
    - nav restored `社区 / 上传 / 汉化 / 消息 / Cookie`
    - `/crawlerManager` stayed reachable
    - `/userDetail` restored `我的书帖 / 我的Cookie`
    - `/novelDetail/1` restored `获取新章节 / AI术语 / 用户术语 / 重新汉化` and the pending-translation section
- Local Spring Boot browser validation was blocked by the current database startup failure:
  - backend boot failed before serving HTTP because Hibernate could not determine a dialect without live JDBC metadata in the current local environment
  - captured logs under `docs/testing/ai-logs/reader-mode-validation/`

## Documentation Sync
- Updated: `README.md`, `docs/context/project-overview.md`, `docs/context/development-roadmap.md`, `docs/tasks/web/2026-04-03-reader-mode-surface-simplification.md`, `docs/tasks/web/INDEX.md`
- Checked with no change needed: `docs/context/architecture.md`, `docs/context/tech-stack.md`, `docs/engineering/runtime-operations.md`

## Risks
- Some reader-facing pages may still contain hidden dependencies on maintainer APIs or controls that are not obvious until browser validation.
- `UserDetail` and similar mixed pages may need more granular cleanup than route-level gating suggests.
- Because backend interfaces remain enabled, this phase improves product clarity but not backend attack surface.

## Follow-ups
- Audit more shared pages for reader-surface leakage after the first-pass route/menu cleanup.
- After surface simplification lands, decide whether published runs also need backend-side endpoint restrictions.
