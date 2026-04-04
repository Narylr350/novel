# FreeNovel

韩轻小说翻译阅读平台维护仓库，当前工作重点是把遗留项目整理到“能理解、能启动、能维护”的状态，而不是继续堆新功能。

## Current Status

- 后端：`novel`，Spring Boot 4 + Java 21
- 前端：`free-novel-web`，Vue 3 + Vue CLI 5
- 数据：MariaDB，SQL 导入文件位于 `sql/`
- 运行目录：`app/`

当前仓库已经停用根目录旧 PowerShell 启动脚本。维护时请以源码、compose 文件和 `docs/` 下的说明为准。

## Supported Startup Paths

### 1. Local Development

适合直接维护源码时使用。

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

默认访问地址：

- Web: `http://localhost:8080`
- Backend: `http://localhost:8081`

运行模式：

- `dev` 配置默认是 `maintainer mode`，会显示上传、汉化、消息、爬虫等维护入口
- `prod` 配置默认是 `reader mode`，界面只保留读书所需的轻量功能
- 如需覆盖默认值，可设置环境变量 `APP_UI_MODE=reader|maintainer`

### 2. Docker Compose

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

- `docker-compose.local-single.yml` 和 `docker-compose.local-dual.yml` 现在默认使用当前仓库根目录的 [Dockerfile](/D:/Narylr/FreeNovel/Dockerfile) 构建应用镜像，适合维护源码时使用
- `docker-compose.external-single.yml` 和 `docker-compose.external-dual.yml` 仍然保留为镜像部署路径
- compose 运行时继续挂载仓库下的 `app/` 目录作为运行目录

## Database Import

大体量 SQL 数据仍需手动导入，位于 `sql/` 目录。仓库现在提供一个本地 Windows MariaDB 的安全导入入口：

```powershell
.\scripts\import-local-sql.ps1 -SqlFile .\sql\dictionary.sql -EnsureDatabase
```

推荐用法：

```powershell
.\scripts\import-local-sql.ps1 -SqlFile .\sql\main.sql
.\scripts\import-local-sql.ps1 -SqlFile .\sql\expand.sql
```

如需覆盖默认本地参数：

```powershell
.\scripts\import-local-sql.ps1 `
  -SqlFile .\sql\main.sql `
  -Database novel `
  -ServerHost 127.0.0.1 `
  -Port 3306 `
  -User root `
  -Password novel_root_password
```

说明：

- 该脚本固定走已验证的 `mariadb.exe` stdin 导入路径
- 不再推荐直接使用 `mysql.exe < file.sql` 作为默认本地导入方式
- 数据库模式说明见 `docs/engineering/database.md`

## Build Commands

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

## Maintainer Notes

- 当前产品默认分成两种表面形态：
  - `reader mode`：给小白和本地读者部署使用
  - `maintainer mode`：给开发者和站点维护者使用
- 第一阶段只做前端显隐和路由收口，后端维护接口仍然保留注册。
- `novel/pom.xml` 是 Java 版本的源码真相，当前为 Java 21。
- 前端构建工具是 Vue CLI，不是 Vite。
- 当前依赖在 Node 24 下可以构建，但会出现 engine warning；维护时优先使用 Node 20 LTS。
- 旧 PowerShell 辅助脚本已经退役，不再作为支持的启动入口。
- `HELP.md` 现在只保留为简短维护备注，长期说明以 `README.md` 和 `docs/` 为准。

## Repository Layout

```text
FreeNovel/
├── novel/                     # Spring Boot backend
├── free-novel-web/            # Vue 3 + Vue CLI frontend
├── scripts/                   # supported local maintenance helpers
├── sql/                       # database bootstrap and imports
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
