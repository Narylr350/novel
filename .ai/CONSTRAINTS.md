# FreeNovel Constraints and Working Rules

## 技术约束

- API 改动必须同时检查后端 controller/interceptor 和前端 Axios/signature 调用。
- 大多数 `/api/**` 端点受自定义签名/鉴权影响，书源 API 也要明确哪些公开、哪些需要维护者权限。
- scheduler、crawler、translation、upload、sitemap、file-import 属于高副作用区域，改动必须谨慎验证。
- 韩国小说站爬虫（novelpia 等）不再维护，已由 fork 站 novalpie.cc 接管；不在此方向做新功能或修复。
- 书源执行涉及外部 I/O、JS、cookie、登录态和反爬，必须先小闭环验证，不把猜测写成已完成。
- 桌面浏览器渲染必须有超时、并发限制、页面/上下文释放、内网和本地地址访问防护，并在错误归因中记录实际 renderer。
- cookie 涉及凭据存储，按书源/域名隔离，不混用，不写入日志。
- 配置或部署改动必须同时检查源码配置、环境示例和相关说明，避免只改一处。
- 前端改动优先复用现有组件和视觉风格。
- Node 维护基线优先使用 Node 20 LTS；Node 24 的依赖警告先按环境风险处理，不直接判断为源码损坏。
- 不引入新框架，除非用户明确批准。
- 不修改无关模块，不做未请求重构。
- 结构性改动前先说明范围，不能把测试版或临时入口混进正式路径。
- 不把未验证路径说成已通过。
- 服务用完关闭，不保持后台运行。
- 后续交接以 `.ai/` 基线文件和 commit message 为准。

## Git Hygiene

- `novel/target/`、`free-novel-web/node_modules/`、`free-novel-web/dist/`、`app/logs/`、`app/tmp/`、`app/file/`、`novel/app/`、`sql/` 不进 Git（已在 .gitignore）。
- `.opencode/`、`.idea/`、`.vscode/`、`docs/`、`AGENTS.md`、`CLAUDE.md`、`AI_CONTEXT.md` 不进 Git（已在 .gitignore）。
- `.ai/` 下基线文件（PROJECT.md / TECH.md / CONSTRAINTS.md / VALIDATION.md）进 Git；`.ai/` 下本地缓存、运行记录、草稿、`*.local.*` 不进 Git。
- Docker/Compose 文件已从仓库移除，不再维护；不恢复 Dockerfile / docker-compose*.yml。

## Workflow Authority

- 无旧 AI 工作流文件竞争（项目根无 AGENTS.md / CLAUDE.md / AI_CONTEXT.md）。
- 现有 `.ai/PROJECT.md`（原单文件格式）已被本次 re-init 拆分为 4 文件基线替换。
- README 和 HELP.md 的失准引用（docs/ Canonical Docs、Docker Compose 章节）已在本轮维护修复；以 README 为启动和环境指导事实源。
- `.ai/book-source-module-api-design.md` 是上一阶段模块设计参考，保留作历史参考，不作本阶段事实源；本阶段新设计决策写入 `.ai/TECH.md` 或 commit message。
- 不恢复旧 docs / task / index 重流程。

## 接入的 skill

执行层（project-work 中使用）：

- `diagnosing-bugs` / `systematic-debugging`：用于浏览器渲染、cookie、书源执行相关 bug 和异常行为诊断。严格型 skill，按流程走，不跳步骤。
- `verification-before-completion`：用于声明完成前做最小必要验证，尤其书源正文实际可读性验证（"不能只看接口 200"）。

finish 层（project-finish 生成 commit message 前使用）：

- `verification-before-completion`：提交前对 diff 做最小验证检查。

未接入（可按需在 project-work 中临时调用，不作为基线接入）：

- `codebase-design`、`prototype`、`webapp-testing`、`frontend-design` 等。

管理层入口 `project-work` / `project-finish` 是本 pack 默认工作流，不单列为 skill。

## 验证环境

- 项目需要：Maven（后端构建）、Node 20 LTS（前端构建）、MariaDB 11+（数据库）、Java 21（运行时）。
- 桌面浏览器渲染验证需本机有 Chromium/Edge（具体驱动由 Seed Task 1 确认）。
- 环境不可用时处理：AI 先尝试可逆操作（启动服务、设环境变量）；需管理员权限、装全局软件、需凭据时才提示用户。
- 浏览器验证不稳时优先用构建产物静态证据 + 后端日志/接口返回内容验证。
