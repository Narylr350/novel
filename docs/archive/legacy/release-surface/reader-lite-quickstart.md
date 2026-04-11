# Reader Lite Quickstart

Archived on `2026-04-09`. This document describes a release-era/source-package workflow and is preserved for traceability, not as current repository guidance.

## Who This Is For
Use this guide if you only want a local FreeNovel reader site and do not need crawler, translation, upload, or maintainer features.

If you want the most beginner-friendly click-by-click source-package guide, use:
- [小白使用说明（源码包）](/D:/Narylr/FreeNovel/docs/archive/legacy/release-surface/小白使用说明-源码包.md)

This path installs the repository-owned `lite` package:
- reader-focused schema
- one demo account
- three demo novels
- no large maintainer-side tables such as `terminology`

## What You Need

| Software | Recommended Version |
|----------|---------------------|
| Java | 21+ |
| Node.js | 20.x LTS |
| MariaDB | 11+ |

This source-package path now assumes the backend jar is already bundled under:
- `release\backend\free-novel.jar`

Default local endpoints:
- Web: `http://localhost:8080`
- Backend: `http://localhost:8081`
- Database: `127.0.0.1:3306`

Default local database credentials used by the repository scripts:
- Database: `novel_lite`
- User: `root`
- Password: `novel_root_password`

If your local MariaDB settings differ, pass your own values to the install script.

## Fast Path

From the repository root:

```powershell
.\scripts\install-lite-local.ps1 -LiteDirectory .\sql\lite -Database novel_lite
```

Then start the backend:

```powershell
$env:SPRING_PROFILES_ACTIVE='prod'
$env:APP_UI_MODE='reader'
$env:SPRING_DATASOURCE_URL='jdbc:mariadb://127.0.0.1:3306/novel_lite?useUnicode=true&characterEncoding=utf8mb4'
$env:SPRING_DATASOURCE_USERNAME='root'
$env:SPRING_DATASOURCE_PASSWORD='novel_root_password'
java -jar .\release\backend\free-novel.jar
```

In a second terminal, start the frontend:

```powershell
cd free-novel-web
npm install
npm run serve
```

Open:
- [http://localhost:8080](http://localhost:8080)

If you prefer root-level click entrypoints instead of PowerShell commands, use:
- `01-检查运行环境.cmd`
- `02-安装轻量验证包.cmd`
- `03-启动后端.cmd`
- `04-启动前端.cmd`

## Demo Account
- Email: `demo_reader@lite.local`
- Password: `ReaderPass1`

Change this password after the first successful login on any persistent deployment.

## What Is Included In The First Lite Baseline
Current demo novels:
- `353498 / 绿皮`
- `353474 / 祸世末子的回归`
- `353487 / 亲手培养的女团`

Current package files under `sql/lite/`:
- `schema.sql`
- `seed-system-lite.sql`
- `seed-reader-demo.sql`
- `package-manifest.json`
- `README.md`

## If You Need To Regenerate The Lite Package
If you are maintaining the repository and want to rebuild the shipped `lite` package from a healthy full database:

```powershell
.\scripts\export-lite-package.ps1
```

That command refreshes all `sql/lite/` package files in one pass.

## Large Full-Data Packages
The repository intentionally does not require the historical full SQL dumps for this quickstart.

Large-table or full-data packages are planned to be distributed separately from the repository because they are too large for a normal clone/download workflow.

Reserved distribution slot:
- Baidu Netdisk link: `TODO`
- Extraction code: `TODO`
- Package notes: `TODO`

Do not use those larger packages unless you are explicitly trying to run a maintainer or full-content deployment.

When you move from validation to real reading content, use the repository-owned `reader full` package flow. Do not go back to the old one-shot import of:
- `main.sql`
- `expand.sql`

The default end-user reading path no longer includes these maintainer-heavy tables:
- `terminology`
- `chapter_execute`
- `chapter_error_execute`

## Common Problems

### MariaDB TLS/SSL Error On Local Import
If the MariaDB client reports `TLS/SSL error: no credentials`, use the repository scripts instead of running raw commands by hand. The maintained import path now disables client SSL negotiation automatically.

### Frontend Opens But Reader Data Is Empty
Make sure the backend is using the same database you installed the lite package into. The quickstart backend command above pins the datasource explicitly for that reason.

### I Want The Full Historical Data
Do not import `main.sql` or `expand.sql` for the quickstart path. Wait for the later split-package/full-package workflow.
