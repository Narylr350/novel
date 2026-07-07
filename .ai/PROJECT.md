# FreeNovel Project Baseline

## 1. Goal

FreeNovel 是面向韩轻小说阅读、采集、翻译和维护运营的平台。

当前阶段目标：补齐书源接入的硬骨头——desktop_browser renderer（webView:true / JS 渲染正文）和 cookie 支持，让图片站、CSR 站、需登录态站点也能通过规则书源接入。

工作方式保持小步、可验证、少改动。先调研渲染路径再做实现，不把未验证方案写成已确认事实。

## 2. Users and Scenarios

主要用户：

- 读者：在 FreeNovel 网站阅读本地库和书源内容。
- 维护者：导入、调试、验证书源，扩展站点内容来源。
- 开发者：维护 Spring Boot 后端、Vue 前端、数据库、Docker/Compose 和书源执行能力。

主要场景：

- 使用现有阅读网站看本地库里的小说。
- 导入书源后，通过同一个网站访问外部来源内容（含需要 JS 渲染或 cookie 的站点）。
- 普通站点优先走 http 模式规则书源。
- webView:true / CSR 站点走 desktop_browser 模式。
- 对复杂站点继续使用代码型精确爬虫。

## 3. MVP

本阶段 MVP = 补齐书源硬骨头最小闭环：

- desktop_browser renderer 能对一个 webView:true 真实书源跑通"搜索 -> 详情 -> 目录 -> 正文"，正文实际可读。
- 渲染结果必须标注实际 renderer（desktop_browser），不能冒充 http 或 Legado App 实测。
- cookie 基础支持：能持久化某书源/域名的 cookie，让需 cookie 的书源正文能取到。
- 不破坏现有 http 模式书源和本地库阅读。
- 桌面浏览器渲染必须有超时、并发限制、页面/上下文释放、内网和本地地址访问防护。

## 4. Inputs and Outputs

主要输入：

- FreeNovel 源码：`novel/`、`free-novel-web/`
- 书源 JSON / Legado 规则
- 现有 validator / 书源执行器项目：`D:\Narylr\阅读书源生成技能`（参考）
- MariaDB 数据库
- 外部小说站点 HTTP / 桌面浏览器渲染 / JS 执行结果 / cookie

主要输出：

- FreeNovel 后端 `booksource` 模块的 desktop_browser renderer 和 cookie 能力
- 现有阅读前端可消费的 webView:true 书源搜索/目录/正文数据
- 维护者可判断书源是否可用的验证结果（区分 `http` / `desktop_browser` / `needs_app_review`）
- 必要的错误归因和失败提示

## 5. Non-goals

当前阶段不做：

- 不重写整个 FreeNovel。
- 不替换现有 reader 前端。
- 不做 OCR fallback（保持未来可能，本阶段不做）。
- 不处理验证码、付费墙、DRM、强 Cloudflare 这类硬限制，除非后续单独立项。
- 不做 Android WebView / Android Probe 依赖。
- 不做独立 sidecar 服务作为长期主方案。
- 不做书源缓存层（本阶段只做 renderer + cookie，缓存层留后续阶段）。
- 不做调试工作台的大改（已有调试入口，本阶段只补 renderer/cookie 相关调试能力）。
- 不把未验证的浏览器渲染路径写成已确认事实。
- 不维护旧 docs/ 工作流（docs/ 目录当前不存在）。
- 不把 validator 原样粗暴复制进业务层后不拆边界。
- 不把外部数据库包、外部分发包或未跟踪运行资产写成仓库内已保证事实。

## 6. Seed Tasks

第一批启动任务（服务本阶段 MVP）：

1. 调研 desktop_browser renderer 实现路径并做最小验证：候选含 Playwright Java / Selenium / CEF / 直接驱动 Edge/Chromium；评估维度含超时、并发、上下文释放、内网防护、JS 执行、正文提取、资源占用。产出实现路径推荐 + 一个能渲染真实 webView:true 书源正文的最小验证。
2. 在 `booksource/render` 下实现 `DesktopBrowserRuleSourceRenderer` 接口与最小实现（基于 Seed Task 1 结论），区分 desktop/mobile profile，标注实际 renderer。
3. 在 `booksource` 下实现 cookie 基础支持：`BookSourceCookieEntity` / `BookSourceCookieRepository` / `BookSourceCookieService`，支持按书源/域名持久化和查询 cookie。
4. 接通 desktop_browser renderer 到书源执行链路（搜索 -> 详情 -> 目录 -> 正文），前端复用现有阅读页面消费 webView:true 书源内容。
5. 端到端验证：导入一个 webView:true 真实书源 -> 搜索 -> 详情 -> 目录 -> 正文实际可读；cookie 支持验证；渲染结论区分 `http` / `desktop_browser` / `needs_app_review`。

后续 Next 写在 commit message，不回写到本文件。
