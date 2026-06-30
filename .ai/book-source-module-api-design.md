# Book Source Module and API Design

## Scope

This design covers Seed Task 2 from `.ai/PROJECT.md`: define the minimum FreeNovel backend module and API for imported Legado/阅读 style book sources, including `http` and `desktop_browser` renderer boundaries.

It does not implement the parser, crawler, desktop renderer, UI, cache policy, or Android Probe integration.

## Source Evidence

- `novel/src/main/java/com/wtl/novel/Controller/LegadoController.java` exposes the local FreeNovel library as a public reading-app source under `/api/legado/**`.
- `novel/src/main/java/com/wtl/novel/util/URLMatcher.java` currently whitelists `/api/legado/.*`, but most `/api/**` routes pass through `SignatureInterceptor`.
- `free-novel-web/src/api/requestAuth.mjs` signs API requests with the current credential token.
- `novel/src/main/java/com/wtl/novel/entity/User.java` has no explicit admin or maintainer role field.
- `D:\Narylr\阅读书源生成技能\validator\src\main\kotlin\io\legado\validator\web\WebServer.kt` provides reference endpoints for source import, list, debug run, smoke, cookie operations, and rule validation.
- `D:\Narylr\阅读书源生成技能\validator\src\main\kotlin\io\legado\validator\model\Models.kt` and `model\rule\Rules.kt` define the portable source/rule DTO shape.

## Naming

Do not put imported-source execution under `/api/legado/**`.

`/api/legado/**` means "FreeNovel local library exposed to Legado/阅读 App". The new module means "FreeNovel imports external Legado/阅读 source rules and executes them".

Use:

- Java package root: `com.wtl.novel.booksource`
- API root: `/api/book-sources`
- Frontend route prefix later: `/source/...` or `/book-source/...`, not `/legado/...`

## Backend File Boundary

Minimum implementation package layout:

- `booksource/model`
  - Imported rule source DTOs copied/adapted from validator: `BookSource`, `SearchRule`, `BookInfoRule`, `TocRule`, `ContentRule`.
  - Reader DTOs: `SourceSearchBook`, `SourceBook`, `SourceChapter`, `SourceContent`.
- `booksource/entity`
  - `BookSourceEntity`: stores imported source metadata and raw JSON.
  - `BookSourceCookieEntity`: optional first version storage for source/domain cookies when cookie support is implemented.
- `booksource/repository`
  - `BookSourceRepository`.
  - `BookSourceCookieRepository` only when cookie support is implemented.
- `booksource/service`
  - `BookSourceImportService`: parse, validate, normalize, persist, list.
  - `BookSourceExecutionService`: search, detail, toc, content orchestration.
  - `BookSourceDebugService`: run search -> detail -> toc -> content and return `DebugStep` style diagnostics.
  - `BookSourceCookieService`: domain normalization and cookie lookup/update when cookie support is implemented.
- `booksource/rule`
  - Rule parsing/evaluation adapter.
  - This is where validator `AnalyzeUrl`, `AnalyzeRule`, JSONPath, XPath, CSS, regex, and JS support migrate later.
- `booksource/render`
  - `RuleSourceRenderer` interface.
  - `HttpRuleSourceRenderer` for ordinary HTML/API fetch.
  - `DesktopBrowserRuleSourceRenderer` for `webView:true` or JS/CSR content.
- `booksource/controller`
  - `BookSourceController`: import/list/delete/validate management endpoints.
  - `BookSourceReaderController`: search/detail/toc/content endpoints.
  - `BookSourceDebugController`: debug run endpoint.

Keep these classes separate from `NovelController`, `ChapterController`, and `LegadoController`.

## Storage Shape

`BookSourceEntity` fields:

- `id`: Long database id.
- `sourceId`: stable opaque id, derived from `SHA-256(bookSourceUrl)` or equivalent.
- `bookSourceUrl`: original unique source URL.
- `bookSourceName`: display name.
- `bookSourceGroup`: optional group.
- `enabled`: whether this source is usable.
- `rawJson`: original source JSON object string.
- `createdAt`, `updatedAt`.

First MVP should not materialize external books into existing `Novel` / `Chapter` rows. External books and chapters should be represented as DTOs with opaque ids derived from source id + external URL.

Reason: existing `Novel` and `Chapter` ids are local database `Long` ids and are already tied to favorites, reading records, comments, encrypted chapter APIs, upload checks, and edit flows.

## Renderer Boundary

Define:

```java
public interface RuleSourceRenderer {
    RenderedPage fetch(RenderRequest request);
}
```

`RenderRequest` contains:

- `url`
- `method`
- `headers`
- `body`
- `charset`
- `rendererMode`: `http` or `desktop_browser`
- `rendererProfile`: `desktop` or `mobile`
- `webJs`
- `sourceRegex`
- `timeoutMillis`

`RenderedPage` contains:

- `finalUrl`
- `statusCode`
- `headers`
- `body`
- `rendererMode`
- `rendererProfile`
- `errorCode`
- `errorMessage`

Renderer selection:

- Use `http` by default.
- Use `desktop_browser` when parsed URL options contain `webView:true`.
- Use `desktop_browser` when content needs JS/CSR rendering and HTTP extraction returns a CSR shell or empty content with render evidence.
- A source may request a mobile profile, but the renderer result must still say `desktop_browser`, not Android WebView.

Desktop renderer safety requirements:

- Enforce timeout.
- Enforce concurrency limit.
- Close page/context after every request.
- Block local file URLs.
- Block loopback, link-local, private LAN, and metadata-service IP ranges unless a later explicit maintainer-only setting enables them.
- Return structured errors; do not throw raw browser exceptions to the API response.

Do not make Android WebView or Android Probe part of MVP.

## API Shape

All first-stage `/api/book-sources/**` endpoints should stay behind the existing signature/auth flow. Do not add them to `URLMatcher.PATTERNS` initially.

Reason: the module performs external I/O and can become an SSRF or resource-exhaustion surface if public.

### Management

`POST /api/book-sources/import`

Request:

```json
{
  "sourceJson": "[{...}]"
}
```

Response:

```json
{
  "ok": true,
  "count": 1,
  "sources": [
    {
      "sourceId": "opaque-source-id",
      "bookSourceUrl": "https://example.com",
      "bookSourceName": "Example"
    }
  ],
  "issues": []
}
```

`GET /api/book-sources`

Response:

```json
{
  "sources": [
    {
      "sourceId": "opaque-source-id",
      "bookSourceUrl": "https://example.com",
      "bookSourceName": "Example",
      "enabled": true
    }
  ]
}
```

`DELETE /api/book-sources/{sourceId}`

Response:

```json
{
  "ok": true
}
```

`POST /api/book-sources/validate`

Request:

```json
{
  "sourceJson": "{...}"
}
```

Response:

```json
{
  "ok": true,
  "issues": [
    {
      "field": "ruleToc.chapterUrl",
      "severity": "error",
      "message": "chapterUrl contains webView but is not parseable"
    }
  ]
}
```

### Reader Execution

Use POST for all external execution endpoints so long URLs, headers, and opaque external ids do not need to be path encoded.

`POST /api/book-sources/search`

Request:

```json
{
  "sourceId": "opaque-source-id",
  "keyword": "test",
  "page": 1
}
```

Response:

```json
{
  "sourceId": "opaque-source-id",
  "books": [
    {
      "bookKey": "opaque-book-key",
      "bookUrl": "https://example.com/book/1",
      "name": "Book Name",
      "author": "Author",
      "kind": "Fantasy",
      "coverUrl": "https://example.com/cover.jpg",
      "intro": "Intro",
      "lastChapter": "Chapter 10",
      "wordCount": "100000"
    }
  ]
}
```

`POST /api/book-sources/detail`

Request:

```json
{
  "sourceId": "opaque-source-id",
  "bookKey": "opaque-book-key",
  "bookUrl": "https://example.com/book/1"
}
```

Response:

```json
{
  "sourceId": "opaque-source-id",
  "bookKey": "opaque-book-key",
  "bookUrl": "https://example.com/book/1",
  "name": "Book Name",
  "author": "Author",
  "kind": "Fantasy",
  "coverUrl": "https://example.com/cover.jpg",
  "intro": "Intro",
  "lastChapter": "Chapter 10",
  "wordCount": "100000",
  "tocUrl": "https://example.com/book/1/catalog",
  "updateTime": "2026-07-01"
}
```

`POST /api/book-sources/toc`

Request:

```json
{
  "sourceId": "opaque-source-id",
  "bookKey": "opaque-book-key",
  "bookUrl": "https://example.com/book/1",
  "tocUrl": "https://example.com/book/1/catalog"
}
```

Response:

```json
{
  "sourceId": "opaque-source-id",
  "bookKey": "opaque-book-key",
  "chapters": [
    {
      "chapterKey": "opaque-chapter-key",
      "index": 0,
      "title": "Chapter 1",
      "chapterUrl": "https://example.com/book/1/1",
      "isVip": false,
      "isPay": false,
      "isVolume": false
    }
  ]
}
```

`POST /api/book-sources/content`

Request:

```json
{
  "sourceId": "opaque-source-id",
  "bookKey": "opaque-book-key",
  "chapterKey": "opaque-chapter-key",
  "bookUrl": "https://example.com/book/1",
  "tocUrl": "https://example.com/book/1/catalog",
  "chapterUrl": "https://example.com/book/1/1"
}
```

Response:

```json
{
  "sourceId": "opaque-source-id",
  "bookKey": "opaque-book-key",
  "chapterKey": "opaque-chapter-key",
  "title": "Chapter 1",
  "content": "Readable chapter text",
  "rendererMode": "http",
  "rendererProfile": "desktop",
  "finalUrl": "https://example.com/book/1/1"
}
```

### Debug

`POST /api/book-sources/debug/run`

Request:

```json
{
  "sourceId": "opaque-source-id",
  "keyword": "test",
  "bookUrl": null,
  "rendererMode": "http"
}
```

Response:

```json
{
  "ok": true,
  "finalStatus": "passed",
  "summary": {
    "resultCount": 1,
    "firstBook": "Book Name",
    "chapterCount": 10,
    "contentPreview": "Readable text"
  },
  "steps": [
    {
      "phase": "search",
      "status": "success",
      "rendererMode": "http",
      "errorCode": null
    }
  ]
}
```

Debug output should reuse the validator concept of `DebugStep`, but keep response bodies compact and redact cookies.

## Frontend Boundary

Do not change existing `/novelDetail/:id` and `/chapterDetail/:id` semantics in the same task as backend API design.

Later frontend work should add source-aware routes or mode flags because existing reader components assume local database `Long` ids and call:

- `/api/novels/{id}`
- `/api/chapters/getChaptersByNovelId/{novelId}`
- `/api/chapters/getChapterByIdApi/{id}`

Recommended later routes:

- `/source/:sourceId/search`
- `/source/:sourceId/book`
- `/source/:sourceId/chapter`

The UI can reuse visual styles and reader layout, but source ids and chapter ids must remain opaque strings, not local `Long` ids.

## Implementation Order

1. Create model DTOs and `BookSourceEntity`.
2. Add import/list/delete/validate endpoints with persistence and unit tests.
3. Add `RuleSourceRenderer` interface plus `http` placeholder implementation.
4. Add execution service skeleton for search/detail/toc/content without desktop browser.
5. Port the smallest validator parsing path needed for one real HTTP-only source.
6. Add `desktop_browser` renderer after HTTP execution is demonstrably working.
7. Add frontend source search/detail/chapter routes.
8. Run end-to-end validation with one HTTP source and one JS/CSR source.

## Open Implementation Questions

- Whether the first persisted source table should be created by JPA auto-DDL only or by checked-in SQL migration depends on the repository's current database initialization path at implementation time.
- Desktop browser implementation can use Playwright Java or a controlled local helper process. This design requires the `RuleSourceRenderer` boundary so that choice does not leak into controllers.
- If public anonymous source reading is required later, add a separate explicit public API with allowlisted source ids, rate limits, and renderer restrictions instead of adding all `/api/book-sources/**` to `URLMatcher`.
