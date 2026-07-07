# FreeNovel Tech Direction

## Tech Direction

保持当前仓库事实，不做换栈：

- 后端：`novel`，Java 21，Spring Boot 4.0.0，Maven，Spring Web，Spring Data JPA，Actuator，MariaDB JDBC。
- 前端：`free-novel-web`，Vue 3，Vue Router 4，Vue CLI 5，Element Plus，Axios，CryptoJS，Tailwind/PostCSS。
- 数据库：MariaDB/MySQL 风格，保留单数据库和双数据库运行模式。
- 本地默认端口：Web `8080`，Backend `8081`。
- Docker/Compose 路径已移除，不再维护；部署统一走源码构建或外部镜像。

## 已确认的书源接入方向（本阶段扩展）

- 书源执行能力在 FreeNovel 后端 `booksource` 模块内，复用现有用户、阅读、前端和部署路径。
- `D:\Narylr\阅读书源生成技能` 作为现有实现参考和可迁移能力来源。
- Legado 书源里的 `webView:true` 在 FreeNovel 内解释为"需要浏览器渲染正文"的兼容信号；不依赖 Android WebView / Android Probe。
- 正文渲染内部区分 `http`、`desktop_browser` 等执行模式；桌面浏览器模式可按需使用 desktop 或 mobile profile，但结果必须标注实际 renderer，不能冒充 Legado App 实测。
- cookie 支持在 `booksource` 模块内实现，按书源/域名持久化。
- Java package root：`com.wtl.novel.booksource`；API root：`/api/book-sources`；前端路由前缀后续用 `/source/...` 或 `/book-source/...`，不用 `/legado/...`（沿用上一阶段已确认的命名边界）。

## desktop_browser renderer 实现方案（Seed Task 1 已确认）

**选定方案：Microsoft Playwright for Java v1.61.0**

Maven 坐标：
```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.61.0</version>
</dependency>
```

选型理由（4 候选对比后确认）：
- **Browser → BrowserContext → Page 三级模型**：Browser 单例管理，每请求创建轻量 BrowserContext，Context 间完全隔离（cookie/localStorage/session），并发开销远低于 Selenium 的多 Driver 进程模型。
- **四层超时体系**：导航超时、操作超时、单项超时、JS 等待，可每方法单独设置。
- **自动等待**：`waitForLoadState(NETWORKIDLE)` 等内置等待，降低 CSR 页面渲染完成判断复杂度。
- **资源占用**：10 并发约 450MB（Browser 单例 + 10 轻量 Context），Selenium 同条件需 10 个独立进程约 4GB。
- **try-with-resources 释放**：BrowserContext 和 Page 实现 AutoCloseable，上下文释放最可靠。
- **Microsoft 官方维护**，社区活跃，Java 8+ 兼容（满足 Java 21）。

已排除方案：
- Selenium 4 v4.45.0：多 Driver 进程开销大，无 Context 级隔离。
- JCEF：依赖 AWT/Swing 窗口，不适合 headless 服务端。
- CDP4J：2023 年后无更新，维护风险高。

**最小验证结果**（`PlaywrightFeasibilityTest`，6 项全通过）：
- 基础渲染：导航 + title/h1 提取 ✅
- JS 执行：`page.evaluate()` 注入并提取 DOM ✅
- 超时控制：1ms 超时正确抛出 `PlaywrightException` ✅
- 自动等待：`NETWORKIDLE` 等待成功 ✅
- 内网防护：`page.route()` 拦截器注册成功 ✅
- Context 隔离：两 Context cookie 不互通 ✅

## desktop_browser renderer 实现细节

基于选型确认，renderer 实现遵循以下约束：

**Bean 生命周期**：
- `Playwright` 和 `Browser` 作为 Spring 单例 bean（`@Bean(destroyMethod = "close")`），整个应用生命周期共享。
- 每次渲染请求创建独立 `BrowserContext`（try-with-resources），请求结束自动释放。
- `Page` 在 Context 内创建，随 Context 一起释放。

**超时控制**：
- 导航超时：默认 30s，可通过 `RenderRequest.timeoutMillis()` 覆盖。
- 等待超时：`waitForLoadState(NETWORKIDLE)` 默认 15s。
- 全局超时上限：单次渲染不超过 60s（防止书源规则死循环）。

**并发管理**：
- Browser 单例，多请求通过独立 BrowserContext 并发。
- 并发上限通过外部线程池/信号量控制（默认上限待 Seed Task 2 实现时确认，初定 5-10）。
- 不在多线程间共享 BrowserContext 或 Page。

**内网/本地地址访问防护**：
- 在 BrowserContext 创建时注册 `page.route("**/*", ...)` 拦截器。
- 拦截规则：`localhost`、`127.0.0.1`、`127.0.0.0/8`、`10.0.0.0/8`、`172.16.0.0/12`、`192.168.0.0/16`、`169.254.0.0/16`（链路本地）。
- 命中拦截规则的请求 `route.abort()`，非内网请求 `route.resume()`。

**desktop vs mobile profile 切换**：
- 通过 `Browser.NewContextOptions().setUserAgent(...)` 和 viewport 设置区分 desktop/mobile profile。
- desktop: 桌面 UA + 1920x1080 viewport。
- mobile: 移动 UA + 390x844 viewport（iPhone 14 尺寸）。
- 渲染结果在 `RenderedPage.rendererProfile()` 标注实际使用的 profile。

**renderer mode 路由**：
- `BookSourceExecutionService` 需改造：按 `webView` 信号路由到 `desktop_browser` renderer，否则走 `http` renderer。
- 当前 `BookSourceExecutionService.fetch()` 硬编码 `"http"` mode（第 179-180 行），需改为按书源 `chapterUrl` 是否含 `webView` 判断。
- `RuleSourceRenderer` 单 bean 注入需改为按 mode 路由（`Map<String, RuleSourceRenderer>` 或工厂）。
- `RenderedPage.rendererMode()` 填 `"desktop_browser"`，不冒充 `"http"` 或 Legado App 实测。

## novalpie.cc 书源参考（Seed Task 5 验证目标）

已有现成书源规则（来源：`D:\Narylr\阅读书源生成技能\validator\examples\sources\novalpie-com.json`）：

- **搜索**：`/api/search/index.php?q={{key}}&page={{page}}&limit=20&scope=all&match_type=fuzzy&sort_by=relevance&sort_order=desc&adult_filter=all` — JSON API，http 模式
- **详情**：`/api/novel/detail.php?id={$.id}` — JSON API，http 模式
- **目录**：`/api/chapter/list.php?novel_id={$.id}` — JSON API，http 模式
- **正文**：`/reader?novel={novel_id}&chapter={chapter_id}` + `webView:true` — **需要 desktop_browser renderer**
- **登录**：`https://novalpie.cc/login`，cookie 含 `auth_token`，`enabledCookieJar: true`
- **正文提取 webJs**：在 `/reader` 页面等待 `#chapter-{chapterId}` 或 `.chapter-item` 元素出现，提取 `div:not([class])` 的 innerHTML（正文容器）
- **chapterUrl 格式**：`{{var m = baseUrl.match(/novel_id=(\d+)/); '/reader?novel=' + m[1] + '&chapter=' + result.id}},{"\"webView\":true}"`

这确认了 renderer 路由的实际场景：搜索/详情/目录走 http，正文走 desktop_browser。正文 webJs 需要在 `page.evaluate()` 中执行，等待特定 DOM 元素出现后提取内容。

登录态验证流程（来源：`novalpie-login.json`）：
1. 匿名跑搜索→详情→目录→正文边界（正文预期停在登录态）
2. 获取登录 cookie（`auth_token`）
3. 注入 cookie 到 BrowserContext
4. 再跑正文，预期取到实际内容
5. 检查正文不为空、不是登录墙、不串章

## 暂不采用

- 独立 sidecar 服务作为长期主方案。
- 单独做一个新阅读网站替代 FreeNovel 前端。
- Android WebView / Android Probe 作为依赖。
- OCR fallback（保持未来可能，本阶段不做）。
