# FreeNovel

FreeNovel 是一个面向韩轻小说阅读、采集、翻译和维护运营的项目。

这个仓库现在主要用于：
- 源码维护
- 功能推进
- 缺陷修复
- 运行和部署对齐

正式分发已经独立处理，不再依赖仓库内的发布目录或打包说明。

## 下载分发包

如果你只是想直接使用已经整理好的分发版本，不需要先研究源码仓库。

复制下面这段内容后打开百度网盘 App，操作更方便：

```text
链接:https://pan.baidu.com/s/10yTVZbjWdGdVY3xCT5v4sw?pwd=lupt --来自百度网盘超级会员V5的分享
```

网页版也可以直接打开：
- [百度网盘分发链接](https://pan.baidu.com/s/10yTVZbjWdGdVY3xCT5v4sw?pwd=lupt)

说明：
- 分发包是当前对外使用的主入口
- 源码仓库不再承担“下载后直接运行的发布包”角色
- 如果只是部署或试用，优先使用上面的分发包，而不是从仓库根目录找历史发布脚本

## 仓库用途

这个仓库现在服务的是发布后的主线工作，而不是发布阶段清理。

当前重心：
- 优化读者端体验和核心阅读链路
- 补强直接支撑内容获取、翻译和运营效率的维护者能力
- 修复阻塞功能推进的运行时、鉴权、接口和部署问题
- 保持文档、代码和实际运行方式一致

## 当前技术栈

- 后端：`novel`，Spring Boot 4 + Java 21
- 前端：`free-novel-web`，Vue 3 + Vue CLI 5
- 运行目录：`app/`
- 默认本地端口：
  - Web: `http://localhost:8080`
  - Backend: `http://localhost:8081`

## 源码启动

### 本地开发

适合直接维护源码。

环境基线：

| 软件 | 建议版本 |
|------|----------|
| Java | 21+ |
| Node.js | 20.x LTS |
| MariaDB | 11+ |

后端启动：

```powershell
cd novel
mvn spring-boot:run -Pdev
```

前端启动：

```powershell
cd free-novel-web
npm install
npm run serve
```

运行模式：
- `dev` 默认偏维护者界面
- `prod` 默认偏读者界面
- 如需覆盖，可设置 `APP_UI_MODE=reader|maintainer`

### Docker Compose

仓库根目录没有通用的 `docker-compose.yml`。请显式使用下面四个文件之一：

| 文件 | 用途 |
|------|------|
| `docker-compose.local-single.yml` | 本地单数据库 |
| `docker-compose.local-dual.yml` | 本地双数据库 |
| `docker-compose.external-single.yml` | 外部单数据库 |
| `docker-compose.external-dual.yml` | 外部双数据库 |

本地单数据库示例：

```powershell
docker compose -f docker-compose.local-single.yml up --build -d
```

说明：
- `docker-compose.local-single.yml` 和 `docker-compose.local-dual.yml` 使用仓库根目录的 `Dockerfile` 构建当前源码
- `docker-compose.external-single.yml` 和 `docker-compose.external-dual.yml` 仍保留为镜像部署路径
- Compose 运行时继续挂载仓库下的 `app/` 目录作为运行目录

## 构建命令

前端构建：

```powershell
cd free-novel-web
npm install
npm run build
```

后端打包：

```powershell
cd novel
mvn clean package -DskipTests -Pdev
```

根目录 `Dockerfile` 会先构建 Vue CLI 前端，再把产物打进 Spring Boot 应用静态资源目录。

## 维护说明

- 这个仓库默认服务的是“功能推进 + 源码维护”，不是“发布包生产线”
- `release/`、根目录中文 `.cmd` 包装脚本、源码包分发树等历史发布面不再是当前仓库的主入口
- Node 24 目前仍可能构建成功，但依赖会报 engine warning；维护时优先使用 Node 20 LTS
- `HELP.md` 只保留简短维护备注，长期说明以 `README.md` 和 `docs/` 为准

## 仓库结构

```text
FreeNovel/
├── novel/                     # Spring Boot backend
├── free-novel-web/            # Vue 3 + Vue CLI frontend
├── app/                       # runtime logs, temp files, uploaded files
├── docs/                      # maintenance context and task records
├── Dockerfile                 # local image build path
└── docker-compose*.yml        # supported compose entrypoints
```

## Canonical Docs

- 项目概览：`docs/context/project-overview.md`
- 当前路线：`docs/context/development-roadmap.md`
- 技术基线：`docs/context/tech-stack.md`
- 数据库说明：`docs/engineering/database.md`
- 运行说明：`docs/engineering/runtime-operations.md`
