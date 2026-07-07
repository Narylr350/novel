# FreeNovel Tech Direction

## Tech Direction

保持当前仓库事实，不做换栈：

- 后端：`novel`，Java 21，Spring Boot 4.0.0，Maven，Spring Web，Spring Data JPA，Actuator，MariaDB JDBC。
- 前端：`free-novel-web`，Vue 3，Vue Router 4，Vue CLI 5，Element Plus，Axios，CryptoJS，Tailwind/PostCSS。
- 数据库：MariaDB/MySQL 风格，保留单数据库和双数据库运行模式。
- 本地默认端口：Web `8080`，Backend `8081`。
- Docker/Compose：继续使用仓库已有 compose 文件，不新增运行入口，除非修复配置时确实需要并同步说明。

## 已确认的书源接入方向（本阶段扩展）

- 书源执行能力在 FreeNovel 后端 `booksource` 模块内，复用现有用户、阅读、前端和部署路径。
- `D:\Narylr\阅读书源生成技能` 作为现有实现参考和可迁移能力来源。
- Legado 书源里的 `webView:true` 在 FreeNovel 内解释为"需要浏览器渲染正文"的兼容信号；不依赖 Android WebView / Android Probe。
- 正文渲染内部区分 `http`、`desktop_browser` 等执行模式；桌面浏览器模式可按需使用 desktop 或 mobile profile，但结果必须标注实际 renderer，不能冒充 Legado App 实测。
- cookie 支持在 `booksource` 模块内实现，按书源/域名持久化。
- Java package root：`com.wtl.novel.booksource`；API root：`/api/book-sources`；前端路由前缀后续用 `/source/...` 或 `/book-source/...`，不用 `/legado/...`（沿用上一阶段已确认的命名边界）。

## 待确认（Seed Task 1 调研后补充到本文件）

- desktop_browser renderer 的具体浏览器驱动库选择（Playwright Java / Selenium / CEF / 直接驱动 Edge/Chromium）。
- 该选择确定后，补充 renderer 实现细节（超时、并发上限、上下文释放策略、内网/本地地址防护实现、desktop vs mobile profile 切换方式）到本文件。

在 Seed Task 1 确认前，不写暂定技术方向；实现路径以调研结论为准。

## 暂不采用

- 独立 sidecar 服务作为长期主方案。
- 单独做一个新阅读网站替代 FreeNovel 前端。
- Android WebView / Android Probe 作为依赖。
- OCR fallback（保持未来可能，本阶段不做）。
