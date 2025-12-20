<div align="center">

# 拼好书 (FreeNovel)

韩轻小说自动翻译与阅读平台

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/docker-ready-brightgreen.svg)](https://hub.docker.com/r/mattgideon/freenovel)

</div>

> ⚠️ **部署步骤**：  
> 1. 📦 **拉取 Docker 镜像 / 启动 Docker** → 2. ⭐ **导入数据库** → 3. 🎉 **访问系统**  
> ⚠️ **脚本使用**：所有 PowerShell 脚本使用前必须修改路径，详见 **[脚本路径修改](#📝-快速参考脚本路径修改)**

---

## 📖 项目简介

项目源于 [ygfhgf213/novel](https://github.com/ygfhgf213/novel)

拼好书是一个功能完整的韩语/日语小说自动翻译与在线阅读平台，集成了爬虫、翻译、阅读、管理等功能。

**核心特色**：
- 🕷️ **自动爬虫**：支持 NovelPia、Syosetu、BookToki 等平台
- 🤖 **AI 翻译**：集成多种翻译引擎，自动翻译韩语/日语小说
- 📚 **在线阅读**：优化的阅读体验，支持笔记、评论、收藏
- 🎨 **字体混淆**：自动生成混淆字体防止爬虫
- 💾 **导出功能**：一键导出小说为 TXT 文件
- 🔧 **管理后台**：爬虫管理、翻译控制、任务调度

### 技术栈

**后端**：Spring Boot 3.x + JPA + MariaDB  
**前端**：Vue 3 + Element Plus + Vite  
**存储**：Cloudflare R2 (可选)  
**部署**：Docker + Docker Compose

---

## 🚀 快速开始

### 📋 部署检查清单

在开始部署前，请确保完成以下准备工作：

- [ ] **Docker 环境**：已安装 Docker 和 Docker Compose
- [ ] **SQL 文件**：已下载数据库备份文件（通过百度网盘）
- [ ] **存储空间**：至少 25GB 可用空间（用于数据库和文件）
- [ ] **内存要求**：Docker 至少分配 4GB 内存
- [ ] **路径修改**：已修改 `import-sql.ps1` 中的 SQL 路径
- [ ] **字体文件**（可选）：已下载并放入 `app/file/` 目录

### 📦 步骤一：拉取 Docker 镜像

**这是第一步！**必须先拉取镜像或使用 Docker Compose 启动，因为数据库在 Docker 中。

#### 方式一：拉取预构建镜像（可选）

如果你想使用预构建的镜像：

```bash
# 开发版（包含完整爬虫和翻译功能）
docker pull mattgideon/freenovel:v1.0.17-dev

# 或生产版（轻量级）
docker pull mattgideon/freenovel:v1.0.16-prod
```

#### 方式二：直接使用 Docker Compose（推荐）

不需要手动拉取镜像，Docker Compose 会自动处理。

**1. 创建必要目录**

```bash
mkdir -p app/tmp app/file app/logs
```

**2. 准备 SQL 文件**

从百度网盘下载 SQL 文件：
```
链接: https://pan.baidu.com/s/1SveAZ9NqvQSBXuk01xnvPw?pwd=cf44 
提取码: cf44
```

将文件放到一个目录，例如 `D:/data/sql/`

**3. 修改导入脚本路径**

打开 `import-sql.ps1`，修改第 9-11 行：

```powershell
$sqlPath = "D:/data/sql"                    # 改为你的 SQL 文件夹路径
$expandTablePath = "D:/data/sql/扩容表.sql"  # 扩容表路径
$mainTablePath = "D:/data/sql/主表.sql"      # 主表路径
```

⚠️ **建议使用全英文路径**，避免中文编码问题

---

### 💾 步骤二：启动 Docker 服务

**必须先启动 Docker 容器，因为数据库在 Docker 中配置！**

#### 使用 start.ps1 一键启动（推荐）

项目提供了 `start.ps1` 脚本自动处理所有启动步骤：

```powershell
# Windows 下右键点击 start.ps1 → “使用 PowerShell 运行”
# 或在 PowerShell 中执行：
.\start.ps1
```

脚本会自动：
1. 检查 Docker 是否运行
2. 选择部署模式（本地数据库/外部数据库）
3. 启动 MariaDB 数据库容器
4. 等待数据库初始化
5. 启动应用容器

#### 或手动启动

选择合适的 docker-compose 文件：

```bash
# 本地单数据库模式（推荐新手使用）
docker-compose -f docker-compose.local-single.yml up -d

# 或本地双数据库模式（读写分离）
docker-compose -f docker-compose.local-dual.yml up -d

# 或外部数据库模式（需要配置 .env）
docker-compose -f docker-compose.external-single.yml up -d
```

等待 15-30 秒让数据库完全启动。

检查服务状态：
```bash
docker ps
```

应该看到 `novel-mariadb` 容器正在运行。

---

### ⭐ 步骤三：导入数据库

**现在数据库容器已经运行，可以导入 SQL 数据了！**

**这是项目部署的关键步骤！** 由于项目使用了大量现有数据，需要导入 SQL 文件到数据库。

#### 方式一：使用一键导入脚本（推荐）

运行导入脚本：

```powershell
# Windows 下右键点击 import-sql.ps1 → “使用 PowerShell 运行”
# 或在 PowerShell 中执行：
.\import-sql.ps1
```

脚本会自动：
1. 检查 Docker 和 MariaDB 是否运行
2. 导入小表（credential, dictionary, user, invitation_code）
3. 导入扩容表（约 360MB）
4. 提示是否导入主表（约 20GB，需要 4 小时左右）

**导入时间预估：**
- 小表：1-2 分钟
- 扩容表：3-5 分钟
- 主表：4 小时左右（取决于硬盘速度）

---

#### 方式二：手动导入

**1. 确认数据库已启动**

```bash
docker ps | grep novel-mariadb
```

**2. 导入小表**

```bash
# 导入单个 SQL 文件
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < credential.sql
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < dictionary.sql
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < user.sql
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < invitation_code.sql
```

**3. 导入大表（分批导入）**

```bash
# 导入扩容表（360MB）
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < 扩容表.sql

# 导入主表（20GB，需要耐心等待）
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < 主表.sql
```

**⚠️ 常见问题：**

1. **导入失败：“找不到文件”**
   - 检查 SQL 文件路径是否正确
   - 在 PowerShell 中运行时，需要在 SQL 文件所在目录执行命令

2. **导入卡住不动**
   - 大文件导入需要很长时间，请耐心等待
   - 可以打开另一个终端查看数据库日志：`docker logs -f novel-mariadb`

3. **编码问题**
   - 确保 SQL 文件是 UTF-8 编码
   - 如果中文乱码，尝试添加：`--default-character-set=utf8mb4`

4. **内存不足**
   - 20GB 文件导入可能需要调整 Docker 内存限制
   - 建议至少分配 4GB 内存给 Docker

---

#### 方式三：使用数据库客户端（图形化）

**1. 连接数据库**

使用 Navicat、DBeaver、HeidiSQL 等工具连接：
- 主机：`localhost`
- 端口：`3306`
- 用户名：`root`
- 密码：`novel_root_password`
- 数据库：`novel`

**2. 导入 SQL 文件**

在客户端中选择“导入”功能，选择你的 SQL 文件即可。

---

#### 验证导入是否成功

```bash
# 进入数据库
docker exec -it novel-mariadb mariadb -uroot -pnovel_root_password novel

# 查看表数量
SHOW TABLES;

# 查看部分表的数据量
SELECT COUNT(*) FROM novel;
SELECT COUNT(*) FROM chapter;
SELECT COUNT(*) FROM user;

# 退出
exit;
```

如果能看到数据，说明导入成功！

---

### 🎉 步骤四：访问系统

导入数据库后，系统就可以使用了！

**访问地址：**
- 前端页面：`http://localhost:8080`
- 后端 API：`http://localhost:8081`

**默认账号：**
- 管理员：通过数据库导入的用户账号

---

## 📖 Docker 部署模式详解

项目提供了 4 种 Docker Compose 配置文件，适合不同场景：

| 配置文件 | 说明 | 适用场景 |
|---------|------|----------|
| `docker-compose.local-single.yml` | 本地单数据库 | 测试、开发环境（推荐新手） |
| `docker-compose.local-dual.yml` | 本地双数据库（读写分离） | 性能测试 |
| `docker-compose.external-single.yml` | 外部单数据库 | 使用云数据库 |
| `docker-compose.external-dual.yml` | 外部双数据库 | 生产环境（读写分离） |

### 镜像版本

**开发版** (包含完整爬虫和翻译功能)：
```bash
docker pull mattgideon/freenovel:v1.0.17-dev
```

**生产版** (轻量级，不含定时任务)：
```bash
docker pull mattgideon/freenovel:v1.0.16-prod
```

### 环境配置（可选）

复制示例配置：
```bash
cp .env.example .env
```

编辑 `.env` 文件（或直接在 docker-compose.yml 中配置）：

```bash
# 应用配置
SERVER_PORT=8081
DATABASE_MODE=single  # single 或 dual

# 数据库配置（使用本地 Docker 数据库时）
MYSQL_PRIMARY_PASSWORD=your_secure_password

# 外部数据库配置（使用外部数据库时）
PRIMARY_DB_URL=jdbc:mariadb://your-host:3306/novel?...
PRIMARY_DB_USERNAME=your_username
PRIMARY_DB_PASSWORD=your_password

# 功能开关
TASK_SCHEDULER_ENABLED=true          # 启用定时任务
TASK_NOVELPIA_TASK2_ENABLED=true     # 启用翻译任务
TASK_NOVELPIA_TASK3_ENABLED=true     # 启用下载任务

# 存储配置
CLOUDFLARE_R2_ENABLED=false          # 是否使用 R2 存储
PROXY_CLIENT=false                   # 是否启用代理
```

**注意**：使用 `start.ps1` 脚本启动时，会使用默认配置，无需修改 `.env`。

---

## 📋 主要功能

### 1. 爬虫管理

支持以下平台的小说爬取：
- **NovelPia**（韩国）：需配置 Cookie
- **Syosetu**（日本）：直接爬取
- **BookToki**（韩国）：通过油猴脚本辅助

**使用方式**：
1. 登录后点击菜单 → **🕷️ 爬虫管理**
2. 查看爬虫状态、待处理章节
3. 手动触发下载或配置自动任务
4. 可单独关闭 AI 翻译，只使用爬虫功能

### 2. 小说阅读

- 在线浏览章节列表
- 优化的阅读界面
- 自动记录阅读进度
- 支持笔记和评论
- **导出功能**：点击小说详情页的 **"导出小说"** 按钮，下载完整 TXT

### 3. 小说搜索与收藏

- 搜索韩语/日语小说
- 添加到收藏分组
- 查看阅读历史
- 标签分类管理

### 4. 用户上传

支持上传格式：
- TXT 文件
- EPUB 文件
- ZIP 压缩包

### 5. AI 翻译

- 自动翻译待处理章节
- 支持多种翻译引擎
- 术语表自定义
- 错误重试机制

---

## ⚙️ 配置说明

### 环境变量列表

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| **应用配置** | | |
| `SERVER_PORT` | 应用服务端口 | 8081 |
| `DATABASE_MODE` | 数据库模式 (single/dual) | single |
| `FILE_UPLOAD_STORAGE_DIR` | 文件存储目录 | /app/file/ |
| **数据库配置** | | |
| `MYSQL_PRIMARY_PASSWORD` | 本地主数据库密码 | novel_password |
| `PRIMARY_DB_URL` | 外部主数据库连接 | - |
| `PRIMARY_DB_USERNAME` | 外部主数据库用户名 | - |
| `PRIMARY_DB_PASSWORD` | 外部主数据库密码 | - |
| `PRIMARY_DB_POOL_SIZE` | 主数据库连接池大小 | 15 |
| **定时任务配置** | | |
| `TASK_SCHEDULER_ENABLED` | 启用任务调度器 | true |
| `TASK_NOVELPIA_TASK2_ENABLED` | 启用翻译任务 | true |
| `TASK_NOVELPIA_TASK3_ENABLED` | 启用下载任务 | true |
| `TASK_NOVELPIA_PHOTO_ENABLED` | 启用图片下载 | true |
| `TASK_SITEMAP_HTML_GENERATOR_ENABLED` | 生成 SEO 静态页 | false |
| **其他配置** | | |
| `CLOUDFLARE_R2_ENABLED` | 启用 R2 云存储 | false |
| `PROXY_CLIENT` | 启用代理 | false |
| `SITEMAP_RUN` | 生成站点地图 | false |

### 外部数据库配置示例

#### 单数据库模式

编辑 `.env` 文件：
```bash
DATABASE_MODE=single
PRIMARY_DB_URL=jdbc:mariadb://your-db-host:3306/novel?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&connectTimeout=30000&socketTimeout=60000
PRIMARY_DB_USERNAME=novel_user
PRIMARY_DB_PASSWORD=your_secure_password
PRIMARY_DB_POOL_SIZE=15
PRIMARY_DB_MIN_IDLE=5
```

启动：
```bash
docker-compose -f docker-compose.external-single.yml up -d
```

#### 双数据库模式（读写分离）

```bash
DATABASE_MODE=dual

# 主数据库（写入）
PRIMARY_DB_URL=jdbc:mariadb://primary-host:3306/novel?...
PRIMARY_DB_USERNAME=primary_user
PRIMARY_DB_PASSWORD=primary_pass

# 从数据库（读取）
SECONDARY_DB_URL=jdbc:mariadb://secondary-host:3306/novel?...
SECONDARY_DB_USERNAME=secondary_user
SECONDARY_DB_PASSWORD=secondary_pass
```

启动：
```bash
docker-compose -f docker-compose.external-dual.yml up -d
```

---

## 🛠️ 开发指南

### 本地开发环境

#### 前置要求
- JDK 17+
- Node.js 16+
- MariaDB 10.6+ (或 Docker)
- Maven 3.8+

---

### 方式一：使用一键启动脚本（推荐）

项目提供了自动化启动脚本，无需手动启动前后端。

#### 使用 `start-local.ps1` 脚本

**1. 修改脚本路径**

打开 `start-local.ps1`，修改第 58 和 76 行的路径为你的项目路径：

```powershell
# 第 58 行 - 后端路径
$backendPath = "D:\Narylr\FreeNovel\novel"  # 改为你的实际路径

# 第 76 行 - 前端路径
$frontendPath = "D:\Narylr\FreeNovel\free-novel-web"  # 改为你的实际路径
```

**⚠️ 重要：**
- 使用双反斜杠 `\\` 或正斜杠 `/`
- **建议使用全英文路径**，避免中文编码问题

**2. 确保数据库已启动**

```bash
# 启动 MariaDB 容器
docker-compose -f docker-compose.local-single.yml up -d mariadb
```

**3. 运行启动脚本**

```powershell
# Windows 下右键点击 start-local.ps1 → “使用 PowerShell 运行”
# 或在 PowerShell 中执行：
.\start-local.ps1
```

脚本会自动：
1. 检查 Java、Node.js 和 MariaDB
2. 在新窗口启动后端（Spring Boot）
3. 等待 10 秒后启动前端（Vue）
4. 自动安装 npm 依赖（首次运行）

**启动后访问：**
- 前端：`http://localhost:8080` （Vue 开发服务器）
- 后端 API：`http://localhost:8081`

**停止服务：**
- 关闭两个 PowerShell 窗口即可

---

### 方式二：手动启动

#### 后端开发

```bash
cd novel

# 配置数据库连接
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
# 编辑 application-dev.properties 配置数据库

# 启动后端
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 前端开发

```bash
cd free-novel-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问：`http://localhost:5173`

#### 构建生产版本

```bash
# 后端
cd novel
mvn clean package -Pprod

# 前端
cd free-novel-web
npm run build
```

---

## 📚 使用教程

### 添加小说

**方式一：通过搜索添加**
1. 点击「添加小说」
2. 输入 NovelPia 或 Syosetu 的小说 ID
3. 点击搜索
4. 选择小说添加到收藏

**方式二：上传本地文件**
1. 点击「上传」
2. 选择 TXT、EPUB 或 ZIP 文件
3. 填写小说信息
4. 上传完成

### 配置爬虫

1. 进入「爬虫管理」页面
2. 查看当前爬虫状态
3. 添加 NovelPia Cookie（如需要）
4. 手动触发下载或等待定时任务
5. 可选择关闭 AI 翻译，只使用爬虫下载原文

### 下载小说（只有本地开发启动才能有该功能）

1. 打开小说详情页（如 `http://localhost:8081/novelDetail/123`）
2. 点击「**下载小说**」按钮
3. 等待导出完成，自动下载 TXT 文件

### 管理术语表

1. 进入小说详情页
2. 点击「AI 术语」或「用户术语」
3. 添加自定义翻译规则
4. 术语将在翻译时自动应用

---

## 🔧 常见问题

### 📝 快速参考：脚本路径修改

项目提供了多个 PowerShell 脚本，**使用前必须修改路径**：

| 脚本文件 | 需要修改的路径 | 行号 | 说明 |
|----------|----------------|------|------|
| `import-sql.ps1` | `$sqlPath`<br>`$expandTablePath`<br>`$mainTablePath` | 9-11 | SQL 文件夹路径 |
| `start-local.ps1` | `$backendPath`<br>`$frontendPath` | 58, 76 | 项目路径 |
| `start.ps1` | 无需修改 | - | Docker 启动脚本 |
| `check-database.ps1` | 无需修改 | - | 数据库检查脚本 |

**路径修改规则：**
```powershell
# ✅ 正确示例
$sqlPath = "D:/data/sql"                  # 正斜杠
$sqlPath = "D:\\data\\sql"              # 双反斜杠

# ❌ 错误示例
$sqlPath = "D:\data\sql"                 # 单反斜杠（会报错）
$sqlPath = "D:\数据\文件"              # 中文路径（可能有编码问题）
```

**💡 建议：**
- 使用全英文路径
- 避免路径中包含空格
- 修改后保存文件

---

### Q: 启动后无法访问？

**A**: 
1. 检查容器是否正常运行：`docker ps`
2. 查看日志：`docker logs novel-app`
3. 确认端口未被占用：`netstat -an | grep 8081`

### Q: 数据库连接失败？

**A**: 
1. 检查数据库配置是否正确
2. 确认数据库已启动：`docker ps | grep mariadb`
3. 测试连接：使用 `check-database.ps1` 脚本

### Q: 爬虫无法下载？

**A**: 
1. 检查定时任务是否启用：`TASK_SCHEDULER_ENABLED=true`
2. 查看爬虫管理页面的状态
3. NovelPia 需要配置有效的 Cookie
4. 检查代理配置（如需要）

### Q: 翻译不工作？

**A**: 
1. 确认翻译任务已启用：`TASK_NOVELPIA_TASK2_ENABLED=true`
2. 检查数据库中 `dictionary` 表的 `executeTr` 配置
3. 查看日志中的错误信息

### Q: 如何清理 HTML 文件？

**A**: 
app/file/html 目录下的 HTML 是 SEO 静态页，如不需要可：
1. 删除目录：`rm -rf app/file/html`
2. 关闭生成：设置 `TASK_SITEMAP_HTML_GENERATOR_ENABLED=false`

### Q: 如何备份数据？

**A**: 
```bash
# 备份数据库
docker exec novel-mariadb mysqldump -u root -p novel > backup.sql

# 备份文件
tar -czf app-backup.tar.gz app/
```

---

## 📦 项目结构

```
FreeNovel/
├── novel/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/wtl/novel/
│   │       ├── Controller/   # 控制器
│   │       ├── Service/      # 服务层
│   │       ├── entity/       # 实体类
│   │       ├── repository/   # 数据访问
│   │       ├── translator/   # 翻译引擎
│   │       ├── siteMap/      # SEO 生成
│   │       └── util/         # 工具类
│   └── src/main/resources/
│       └── application*.properties  # 配置文件
├── free-novel-web/          # 前端项目
│   ├── src/
│   │   ├── components/      # Vue 组件
│   │   ├── router/          # 路由配置
│   │   └── api/             # API 接口
│   └── public/              # 静态资源
├── app/                     # 运行时数据
│   ├── tmp/                 # 临时文件
│   ├── file/                # 存储文件
│   └── logs/                # 日志文件
├── docker-compose*.yml      # Docker 配置
└── Dockerfile*              # Docker 镜像
```