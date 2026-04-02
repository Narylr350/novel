# FreeNovel - 韩轻小说翻译阅读平台

一个功能完整的韩语/日语小说自动翻译与在线阅读平台，集成了爬虫、翻译、阅读、管理等功能。

## 下载地址

**百度网盘**: [点击下载](https://pan.baidu.com/s/1FrFeUCpffEYaJoo1BNzQAQ?pwd=qpid)
**提取码**: `qpid`

---

## 部署方式

| 方式 | 推荐度 | 说明 |
|------|:------:|------|
| [Docker 部署](#-docker-部署推荐) | ⭐⭐⭐ | 当前推荐方式，仓库根目录旧脚本已移除 |
| [本地运行](#-本地运行手动) | ⭐⭐ | 适合需要直接调试前后端源码的维护者 |

---

## 📦 本地运行（手动）

仓库根目录旧的 PowerShell 辅助脚本已停止维护，不再作为支持的启动方式。

### 1. 安装必需软件

| 软件 | 版本要求 | 安装包 |
|------|---------|--------|
| Java | 21+ | `OpenJDK21U-jdk_x64_windows_hotspot_21.0.4_7.msi` |
| Node.js | 20+ | `node-v20.11.1-x64.msi` |
| MariaDB | 11+ | `mariadb-11.4.4-winx64.msi` |

**MariaDB 安装注意事项**：
- 安装时会要求设置 root 密码，**请记住这个密码**
- 勾选 "Use UTF8 as default server's character set"

### 2. 初始化数据库并导入数据

使用 MariaDB 客户端手动导入 `sql/` 目录中的数据文件，建议导入顺序如下：

| 文件 | 大小 | 预计耗时 |
|------|------|---------|
| credential.sql | 小 | 几秒 |
| dictionary.sql | 小 | 几秒 |
| invitation_code.sql | 小 | 几秒 |
| user.sql | 小 | 几秒 |
| expand.sql | ~360MB | 3-5 分钟 |
| main.sql | ~20GB | 4-6 小时 |

### 3. 启动后端

```powershell
cd novel
mvn spring-boot:run -Pdev
```

默认端口：`8081`

### 4. 启动前端

```powershell
cd free-novel-web
npm install
npm run serve
```

默认端口：`8080`

### 5. 访问系统

浏览器访问 **http://localhost:8080**

---

## 🐳 Docker 部署（推荐）

适合有 Docker 经验的用户，或需要在服务器上部署的场景。

### 前置要求

- Docker Desktop 20.10+
- 至少 25GB 可用空间
- 下载 SQL 数据文件

### 部署步骤

#### 步骤一：启动服务

```powershell
docker-compose -f docker-compose.local-single.yml up -d
```

#### 步骤二：导入数据库

```powershell
docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password novel < your-file.sql
```

#### 步骤三：访问系统

- 前端页面：`http://localhost:8080`
- 后端 API：`http://localhost:8081`

### Docker Compose 配置

| 配置文件 | 说明 |
|---------|------|
| `docker-compose.local-single.yml` | 本地单数据库（推荐） |
| `docker-compose.local-dual.yml` | 本地双数据库（读写分离） |
| `docker-compose.external-single.yml` | 外部单数据库 |
| `docker-compose.external-dual.yml` | 外部双数据库 |

### Docker 镜像

```bash
# 开发版（包含爬虫和翻译功能）
docker pull mattgideon/freenovel:v1.0.17-dev

# 生产版（轻量级）
docker pull mattgideon/freenovel:v1.0.16-prod
```

---

## 常见问题

### PowerShell 脚本无法运行？

以管理员身份打开 PowerShell，执行：
```powershell
Set-ExecutionPolicy RemoteSigned
```

### MariaDB 连接失败？

1. 检查 MariaDB 服务是否启动（Win+R → services.msc → 找 MariaDB）
2. 检查密码是否正确
3. 检查端口 3306 是否被占用

### 导入数据很慢？

20GB 的主表需要 4-6 小时，这是正常的。使用 SSD 硬盘会更快。

### 启动后无法访问？

1. 检查后端窗口是否有报错
2. 等待 "Started NovelApplication" 出现
3. 检查端口 8080、8081 是否被占用

### 端口被占用？

```powershell
# 查看占用端口的程序
netstat -ano | findstr :8080

# 结束占用程序
taskkill /PID 进程号 /F
```

---

## 功能介绍

- **自动爬虫**：支持 NovelPia、Syosetu、BookToki 等平台
- **AI 翻译**：集成多种翻译引擎，自动翻译韩语/日语小说
- **在线阅读**：优化的阅读体验，支持笔记、评论、收藏
- **小说导出**：一键导出小说为 TXT 文件
- **阅读APP书源**：支持阅读3.0等APP，通过书源导入使用
- **管理后台**：爬虫管理、翻译控制、任务调度

---

## 目录结构

### 网盘完整包

```
FreeNovel/
├── 01-installer/
│   ├── OpenJDK21U-jdk_x64_windows_hotspot_21.0.4_7.msi
│   ├── node-v20.11.1-x64.msi
│   └── mariadb-11.4.4-winx64.msi
│
├── 02-source/
│   └── FreeNovel.zip
│
├── 03-sql/
│   ├── credential.sql
│   ├── dictionary.sql
│   ├── invitation_code.sql
│   ├── user.sql
│   ├── expand.sql              (~360MB)
│   └── main.sql                (~20GB)
│
└── README.txt
```

### 项目源码

```
FreeNovel/
├── novel/                    # 后端项目 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/wtl/novel/
│   │       ├── Controller/   # 控制器
│   │       ├── Service/      # 服务层
│   │       ├── entity/       # 实体类
│   │       └── translator/   # 翻译引擎
│   └── src/main/resources/
│
├── free-novel-web/           # 前端项目 (Vue 3)
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   └── api/
│   └── public/
│
└── docker-compose*.yml       # Docker 配置
```

---

## 技术栈

- **后端**：Spring Boot 4.0 + JPA + MariaDB
- **前端**：Vue 3 + Element Plus + Vite
- **数据库**：MariaDB 11+
- **部署**：Docker + Docker Compose（可选）

---

## 联系方式

如有问题，请在 GitHub Issues 中反馈。
