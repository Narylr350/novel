# FreeNovel Project Baseline

## 1. Goal

FreeNovel 当前目标是维护现有小说阅读、采集、翻译和运营项目，确保主要功能能够正常使用。

本轮基线聚焦四类工作：修复 bug、修复配置问题、消除不合理硬编码、整理现有文档与实际运行方式的不一致。

工作方式应保持小步、可验证、少改动，不做重构式扩张。

## 2. Users and Scenarios

主要用户：

- 读者：使用网页阅读小说、搜索、查看详情、登录后使用收藏、笔记、评论、历史、导出等功能。
- 维护者：使用采集、章节同步、翻译配置、上传、词库、爬虫任务和运行状态相关功能。
- 开发/运维者：在本地或 Docker/Compose 环境中启动、排查、修复和部署项目。

主要场景：

- 本地源码维护：后端 `novel`，前端 `free-novel-web`。
- 本地运行验证：后端默认 `http://localhost:8081`，前端默认 `http://localhost:8080`。
- Docker/Compose 运行：使用仓库已有 compose 文件。
- 文档整理：让 README、环境示例、运行说明和现有 docs 中的长期说明与当前源码保持一致。

## 3. MVP

第一阶段 MVP 是让项目进入“能持续修、能判断是否正常”的维护状态：

- 识别并修复阻塞核心功能使用的 bug。
- 对齐当前实际使用的环境变量、启动命令、Docker/Compose、Spring 配置和前端配置。
- 清理会影响运行、部署或功能判断的硬编码值。
- 整理现有文档中明显过期、互相冲突或误导维护的内容。
- 每个修复完成后，用最直接的命令或手动检查确认相关功能没有继续失败。

## 4. Inputs and Outputs

主要输入：

- 后端源码：`novel/`
- 前端源码：`free-novel-web/`
- 运行目录：`app/`
- 根目录运行与部署文件：`.env.example`、`Dockerfile*`、`docker-compose*.yml`
- 后端配置与环境示例：`novel/src/main/resources/*`、`novel/.env*.example`
- 前端配置：`free-novel-web/package.json`、Vue CLI/路由/API 相关源码
- 现有文档：`README.md`、`HELP.md`、`AI_CONTEXT.md`、`docs/context/`、`docs/engineering/`、必要的 `docs/tasks/` 历史记录
- 数据库和外部依赖：MariaDB、本地或外部数据库、可能存在的外部数据导入资源

主要输出：

- 修复后的后端、前端、配置或文档改动。
- 与当前源码一致的环境示例和运行说明。
- 去除或替换明显不合理硬编码后的代码或配置。
- 简短说明每次修复做了什么、如何验证、还剩什么问题。

## 5. Non-goals

当前默认不做：

- 不重写整个项目。
- 不迁移前端或后端技术栈。
- 不引入新框架、新服务拆分或新发布系统。
- 不恢复历史 release/package 分发流程作为源码仓库主入口。
- 不建立新的验证文档体系。
- 不为了流程要求补齐截图、AI logs、测试规范文件或任务记录模板。
- 不把原项目复杂任务文档流程作为当前轻量维护工作的硬性要求。
- 不做与“修 bug、配置、硬编码、文档整理”无关的大规模重构或视觉改版。
- 不把外部数据库包、外部分发包或未跟踪运行资产写成仓库内已保证事实。

## 6. Tech Direction

技术方向保持当前仓库事实，不做换栈：

- 后端：`novel`，Java 21，Spring Boot 4.0.0，Maven，Spring Web，Spring Data JPA，Actuator，MariaDB JDBC。
- 前端：`free-novel-web`，Vue 3，Vue Router 4，Vue CLI 5，Element Plus，Axios，CryptoJS，Tailwind/PostCSS。
- 数据库：MariaDB/MySQL 风格，保留单数据库和双数据库运行模式。
- 本地默认端口：Web `8080`，Backend `8081`。
- Docker/Compose：继续使用仓库已有 compose 文件，不新增运行入口，除非修复配置时确实需要并同步说明。
- 修复策略：优先小范围修复真实问题，避免借修复名义进行宽泛重构。

## 7. Constraints and Working Rules

技术约束：

- API 改动必须同时检查后端 controller/interceptor 和前端 Axios/signature 调用。
- 大多数 `/api/**` 端点受自定义签名/鉴权影响，不能只看单个 controller。
- scheduler、crawler、translation、upload、sitemap、file-import 属于高副作用区域，改动必须谨慎验证。
- 配置或部署改动必须同时检查源码配置、环境示例、Dockerfile、Compose 和相关说明，避免只改一处。
- 前端改动优先复用现有组件和视觉风格。
- Node 维护基线优先使用 Node 20 LTS；Node 24 的依赖警告先按环境风险处理，不直接判断为源码损坏。
- 不引入新框架，除非用户明确批准。
- 不修改无关模块，不做未请求重构。
- 不继承原项目“完成任务必须更新任务文档、索引、截图、AI logs”等重流程；只有当本次改动确实影响现有长期文档时，才同步整理相关文档。

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
- 文档整理：核对文档描述是否与当前源码、配置和命令一致。
- 硬编码清理：确认替换后的配置来源存在，默认值合理，原功能路径仍能运行。
- 如果本地数据库、外部服务或登录态不可用，明确说明阻塞点，不把未验证路径写成已通过。
- 不要求生成截图、AI logs、专门测试文档或任务记录，除非用户单独要求。

## 9. Seed Tasks

第一批任务：

1. 做一轮轻量全链路问题梳理：定位当前阻塞功能可用性的 bug、配置漂移、硬编码点和文档冲突，不做重流程审计文档。
2. 修复 runtime/config 漂移：对齐 `.env.example`、`novel/.env.example`、`novel/.env.dual.example`、Compose、Spring 配置和 README 中实际使用的变量名、镜像版本、启动说明。
3. 修复后端启动和健康检查路径：确认 MariaDB 前置条件、Maven profile、Spring Boot 4 配置项和 `/actuator/health` 可达性。
4. 修复前端基础可用性问题：处理 `npm run lint` 当前失败、构建警告边界，并验证读者端首页、列表、详情、阅读链路。
5. 清理影响运行和维护判断的硬编码：优先处理端口、域名、API 地址、数据库连接、镜像版本、任务开关、路径等会造成环境漂移或部署误判的硬编码。
