# FreeNovel Project Baseline

## 1. Goal

FreeNovel 的长期目标是维护成一个小说阅读与内容接入平台。

当前确认方向：

- 现有网页继续作为主要阅读入口。
- 后端继续负责小说、章节、用户、收藏、笔记、评论、历史等核心功能。
- 新增规则书源接入能力：允许导入 Legado / 阅读式书源，通过后端执行书源规则获取搜索、详情、目录、正文，并复用现有前端阅读体验。
- 精确爬虫、规则书源、后续 OCR fallback 分别服务不同站点类型。

工作方式应保持小步、可验证、少改动。书源接入要先做最小闭环，再扩展调试、缓存和维护者体验。

## 2. Users and Scenarios

主要用户：

- 读者：在 FreeNovel 网站里搜索、阅读、收藏、记录进度。
- 维护者：导入、调试、验证书源，扩展站点内容来源。
- 开发者：维护 Spring Boot 后端、Vue 前端、数据库、Docker/Compose 和书源执行能力。

主要场景：

- 使用现有阅读网站看本地库里的小说。
- 导入书源后，通过同一个网站访问外部来源内容。
- 对复杂站点继续使用代码型精确爬虫。
- 对普通站点优先尝试规则书源。
- 对图片站或强反爬站点，未来考虑 OCR fallback。

## 3. MVP

第一阶段 MVP 是做出“书源接入到 FreeNovel”的可验证最小闭环：

- 保持现有 reader 网站可用。
- 后端新增书源导入和管理的最小能力。
- 后端能执行一个书源的基础链路：搜索 -> 详情 -> 目录 -> 正文。
- 前端复用现有阅读页面或做最小改造，让书源内容能进入可阅读体验。
- 保留现有数据库小说阅读能力，不破坏原有本地库。
- 书源调试和验证能力可以先做维护者入口，不急着做完整用户产品化。

## 4. Inputs and Outputs

主要输入：

- FreeNovel 源码：`novel/`、`free-novel-web/`
- 书源 JSON / Legado 规则
- 现有 validator / 书源执行器项目：`D:\Narylr\阅读书源生成技能`
- MariaDB 数据库
- 外部小说站点 HTTP / 桌面浏览器渲染 / JS 执行结果

主要输出：

- FreeNovel 后端中的书源导入、执行、调试能力
- 现有阅读前端可消费的搜索、目录、正文数据
- 维护者可判断书源是否可用的验证结果
- 必要的缓存、错误归因和失败提示

## 5. Non-goals

当前默认不做：

- 不重写整个 FreeNovel。
- 不替换现有 reader 前端。
- 不把 validator 原样粗暴复制进业务层后不拆边界。
- 不为了书源接入先做大规模架构重构。
- 不恢复旧 docs / task / index 重流程。
- 不把旧 `D:\FreeNovel` 分发包维护作为当前主线。
- 不承诺所有书源都稳定可用。
- 不处理验证码、付费墙、DRM、强 Cloudflare 这类硬限制，除非后续单独立项。
- 不把外部数据库包、外部分发包或未跟踪运行资产写成仓库内已保证事实。

## 6. Tech Direction

技术方向保持当前仓库事实，不做换栈：

- 后端：`novel`，Java 21，Spring Boot 4.0.0，Maven，Spring Web，Spring Data JPA，Actuator，MariaDB JDBC。
- 前端：`free-novel-web`，Vue 3，Vue Router 4，Vue CLI 5，Element Plus，Axios，CryptoJS，Tailwind/PostCSS。
- 数据库：MariaDB/MySQL 风格，保留单数据库和双数据库运行模式。
- 本地默认端口：Web `8080`，Backend `8081`。
- Docker/Compose：继续使用仓库已有 compose 文件，不新增运行入口，除非修复配置时确实需要并同步说明。

已确认的书源接入方向：

- 书源执行能力要接入 FreeNovel 后端，形成项目内能力。
- `D:\Narylr\阅读书源生成技能` 作为现有实现参考和可迁移能力来源。
- 接入方式分阶段推进：先定义后端边界和最小 API，再接入一个书源闭环，再考虑调试工作台和缓存。
- 当前采用方案：在 FreeNovel 后端内新增规则书源模块，复用现有用户、阅读、前端和部署路径。
- Legado 书源里的 `webView:true` 在 FreeNovel 内解释为“需要浏览器渲染正文”的兼容信号；MVP 优先使用 Windows/桌面 Chromium 或 Edge 渲染，不依赖 Android WebView / Android Probe。
- 正文渲染内部区分 `http`、`desktop_browser` 等执行模式；桌面浏览器模式可按需使用 desktop 或 mobile profile，但结果必须标注实际 renderer，不能冒充 Legado App 实测。

暂不采用：

- 独立 sidecar 服务作为长期主方案。
- 单独做一个新阅读网站替代 FreeNovel 前端。
- Android WebView / Android Probe 作为 MVP 必需依赖。

## 7. Constraints and Working Rules

技术约束：

- API 改动必须同时检查后端 controller/interceptor 和前端 Axios/signature 调用。
- 大多数 `/api/**` 端点受自定义签名/鉴权影响，书源 API 也要明确哪些公开、哪些需要维护者权限。
- scheduler、crawler、translation、upload、sitemap、file-import 属于高副作用区域，改动必须谨慎验证。
- 书源执行涉及外部 I/O、JS、cookie、登录态和反爬，必须先小闭环验证，不把猜测写成已完成。
- 桌面浏览器渲染必须有超时、并发限制、页面/上下文释放、内网和本地地址访问防护，并在错误归因中记录实际 renderer。
- 配置或部署改动必须同时检查源码配置、环境示例、Dockerfile、Compose 和相关说明，避免只改一处。
- 前端改动优先复用现有组件和视觉风格。
- Node 维护基线优先使用 Node 20 LTS；Node 24 的依赖警告先按环境风险处理，不直接判断为源码损坏。
- 不引入新框架，除非用户明确批准。
- 不修改无关模块，不做未请求重构。
- 结构性改动前先说明范围，不能把测试版或临时入口混进正式路径。
- 不把未验证路径说成已通过。
- 服务用完关闭，不保持后台运行。
- 后续交接以 `.ai/PROJECT.md` 和 commit message 为准，不维护旧 docs 工作流。

建议后续按需使用的执行层 skills：

- `diagnosing-bugs` / `systematic-debugging`：用于 bug、启动失败、测试失败、异常行为诊断。
- `verification-before-completion`：用于声明修复完成前做最小必要验证。
- `webapp-testing`：仅当前端行为需要浏览器确认时使用。
- `frontend-design`：仅当 UI 修复涉及界面质量时使用，并保持现有风格。
- `project-work`：作为后续日常维护入口。
- `project-finish`：一轮工作结束、准备提交前用于收尾检查和生成提交信息。

## 8. Validation

验证原则保持轻量、贴近改动：

- 后端 bug 或配置改动：运行能覆盖该问题的 Maven 构建、启动检查、接口检查或日志检查。
- 前端 bug 或配置改动：运行能覆盖该问题的 build、lint 或浏览器手动验证。
- Docker/Compose 改动：至少检查 compose 配置能解析；如本地条件允许，再启动验证。
- 书源接入：必须至少验证一个真实书源的搜索、详情、目录、正文链路。
- 带 `webView:true` 或需要 JS 渲染的正文，优先用 Windows/桌面浏览器渲染验证；验证结论必须区分 `http`、`desktop_browser`、`needs_app_review`。
- 外部站点失败时，要记录是网络、规则、登录态、验证码、CSR、正文抽取还是编码问题。
- 不能只看“接口 200”，正文要确认实际可读。
- 文档整理：核对文档描述是否与当前源码、配置和命令一致。
- 如果本地数据库、外部服务或登录态不可用，明确说明阻塞点，不把未验证路径写成已通过。
- 不要求生成截图、AI logs、专门测试文档或任务记录，除非用户单独要求。

## 9. Seed Tasks

第一批任务：

1. 梳理 `D:\Narylr\阅读书源生成技能` validator 的可迁移边界：导入、搜索、详情、目录、正文、cookie、JS、错误归因。
2. 在 FreeNovel 后端设计最小规则书源模块和 API：source import、source list、debug run、reader search/detail/toc/content，并包含 `http` / `desktop_browser` renderer 边界。
3. 接入一个最小书源执行闭环，不先做 UI 大改；需要 JS 渲染正文时走 Windows/桌面浏览器模式。
4. 让现有前端阅读链路能消费书源内容，优先复用当前详情/章节页面。
5. 跑一轮端到端验证：导入书源 -> 搜索 -> 打开详情 -> 打开章节 -> 正文可读。
