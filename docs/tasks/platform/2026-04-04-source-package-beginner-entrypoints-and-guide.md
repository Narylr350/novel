# Task

## Scope
Add beginner-friendly source-package entrypoints for local FreeNovel usage so a non-technical Windows user can:
- check the local environment
- install the `lite` validation package
- start the backend
- start the frontend
- install the `reader full` package after validation

This task also adds a dedicated Chinese beginner guide for the source-package distribution path.

## Out of Scope
- Shipping pre-bundled Java/Node/MariaDB environment archives
- Reworking Docker packaging
- Changing crawler, translation, OCR, or maintainer features
- Browser-facing UI changes
- Replacing the existing English engineering docs with Chinese copies across the whole repository

## Plan
- Create a platform task record for the source-package beginner entrypoint work.
- Add internal PowerShell helper scripts for environment checks, backend startup, frontend startup, and reader-full installation.
- Add root-level Chinese `.cmd` wrappers that map directly to the supported beginner actions.
- Add small automated checks that the new beginner entrypoints exist and point to the expected maintained scripts.
- Write a Chinese beginner guide that explains the exact click/order flow for the source-package distribution.
- Update the root README and relevant engineering docs so the supported end-user path matches the new beginner entrypoints.
- Run script validation and scope-check the resulting git diff.

## Design
The repository now has stable `lite` and `reader full` database packaging, but the current supported entrypoints are still maintenance-oriented PowerShell commands. That is acceptable for contributors, but not for a beginner who downloads a source-package zip and just wants to run the site locally.

The approved packaging shape for this task is:
- keep the maintained implementation in PowerShell
- expose beginner-friendly root-level `.cmd` launchers with Chinese names
- keep each launcher focused on one action
- document the flow in Chinese as an operation guide, not as a developer manual

### Entry Point Model
The beginner-visible root entrypoints should be:
- `01-检查运行环境.cmd`
- `02-安装轻量验证包.cmd`
- `03-启动后端.cmd`
- `04-启动前端.cmd`
- `05-安装正式阅读包.cmd`

These wrappers should:
- run from the repository root
- call a maintained PowerShell script under `scripts/`
- keep the console open on success/failure so users can read errors

### Internal Script Model
New or wrapped PowerShell entrypoints should cover:
- environment checks
- reader-mode backend startup
- frontend startup
- reader-full installation

The backend startup path should default to a reader-facing database target and be friendly to the current two-database-package model:
- prefer `novel_reader_full` if it exists
- otherwise fall back to `novel_lite`

That keeps the beginner path simple:
1. validate with `lite`
2. start the app
3. later install `reader full`
4. start again without needing a second backend launcher

### Documentation Model
The root `README.md` should stay short and act as a routing document.

The main beginner instructions should live in a dedicated Chinese guide under `docs/engineering/`, focused on:
- what to prepare
- which script to click
- what normal output looks like
- what URL to open
- which demo account to use
- how to move from `lite` validation to `reader full`
- where the future Baidu Netdisk packages will be placed

This guide should intentionally avoid deep internal terms like schema/seed/manifest unless they are necessary for recovery instructions.

## File Structure
- `docs/tasks/platform/2026-04-04-source-package-beginner-entrypoints-and-guide.md`: active task record
- `docs/tasks/platform/INDEX.md`: platform module progress
- `README.md`: root routing entry for new users
- `docs/engineering/reader-lite-quickstart.md`: nearby reader quickstart that should cross-link, not be replaced
- `docs/engineering/runtime-operations.md`: runtime guidance should mention the supported beginner entrypoints
- `docs/engineering/database.md`: database packaging doc should mention the reader-full beginner install entrypoint if changed
- `docs/engineering/小白使用说明-源码包.md`: new Chinese beginner operation guide
- `scripts/check-local-environment.ps1`: environment check helper
- `scripts/start-reader-backend.ps1`: reader-mode backend launcher
- `scripts/start-web-frontend.ps1`: frontend launcher
- `scripts/install-reader-full-local.ps1`: beginner-friendly reader-full installer
- `scripts/tests/DistributionEntrypoints.Tests.ps1`: checks for beginner entrypoint wiring
- `01-检查运行环境.cmd`: root beginner wrapper
- `02-安装轻量验证包.cmd`: root beginner wrapper
- `03-启动后端.cmd`: root beginner wrapper
- `04-启动前端.cmd`: root beginner wrapper
- `05-安装正式阅读包.cmd`: root beginner wrapper

## Review
Pre-implementation review was completed in conversation.

Confirmed decisions:
- first public-facing instructions target the source-package workflow, not a pre-bundled environment archive
- beginner entrypoints should be separate actions, not one giant control script
- root-level script names should be in Chinese so their purpose is obvious to non-technical users
- PowerShell remains the implementation surface, with `.cmd` wrappers for beginner UX

## Implementation
Implemented a beginner-facing source-package entrypoint layer without replacing the maintained PowerShell script surface.

Added internal helper scripts:
- `scripts/check-local-environment.ps1`
  - checks Java, Node.js, npm, MariaDB client, and packaged backend jar availability
  - checks local connectivity for ports `3306`, `8080`, and `8081`
- `scripts/start-reader-backend.ps1`
  - starts the backend in reader mode from the packaged jar `release\backend\free-novel.jar`
  - prefers `novel_reader_full` if it exists
  - falls back to `novel_lite`
- `scripts/start-web-frontend.ps1`
  - installs frontend dependencies on first run if needed
  - starts `npm run serve`
- `scripts/install-reader-full-local.ps1`
  - wraps the maintained `import-reader-full.ps1` flow with beginner-friendly defaults
- `scripts/build-source-package-distribution.ps1`
  - assembles a Baidu-Netdisk-oriented distribution tree under `release\distribution\`
  - stages:
    - `01-源码包\FreeNovel-源码包`
    - `02-环境安装包`
    - `03-SQL-轻量验证包`
    - `04-SQL-正式阅读包`
    - `05-说明文件`
    - `06-SQL-历史维护大表（可选）`
  - excludes local-only validation progress files from the staged `reader full` SQL directory

Added root-level Chinese `.cmd` wrappers:
- `01-检查运行环境.cmd`
- `02-安装轻量验证包.cmd`
- `03-启动后端.cmd`
- `04-启动前端.cmd`
- `05-安装正式阅读包.cmd`

Each wrapper:
- runs from the repository root
- calls the maintained PowerShell entrypoint
- leaves the console open via `pause`

Added automated checks in:
- `scripts/tests/DistributionEntrypoints.Tests.ps1`

Added beginner documentation:
- `docs/engineering/小白使用说明-源码包.md`

Updated routing and engineering docs so the supported source-package flow is now visible from the repository root and nearby operations docs.

Adjusted the beginner distribution assumptions:
- Maven is no longer required for the beginner source-package path
- the source-package now expects a prebuilt backend jar at `release\backend\free-novel.jar`
- beginner installation docs now explicitly document:
  - recommended unzip path
  - required SQL file locations
  - MariaDB port/password choices
  - what to do when local database settings differ from the scripted defaults

## Validation
Completed validation:

- `Invoke-Pester -Path .\scripts\tests\DistributionEntrypoints.Tests.ps1`
- `Invoke-Pester -Path .\scripts\tests\LitePackageTools.Tests.ps1`
- `.\scripts\check-local-environment.ps1`
- packaged backend jar copied to:
  - `release\backend\free-novel.jar`
- staged distribution tree assembly:
  - `.\scripts\build-source-package-distribution.ps1 -Clean`
  - produced:
    - `release\distribution\01-源码包\FreeNovel-源码包`
    - `release\distribution\02-环境安装包`
    - `release\distribution\03-SQL-轻量验证包`
    - `release\distribution\04-SQL-正式阅读包`
    - `release\distribution\05-说明文件`
    - `release\distribution\06-SQL-历史维护大表（可选）`
  - verified:
    - packaged backend jar exists under staged source package
    - staged `reader full` directory contains only package files and no `.reader-full-import-progress-*.json` local validation remnants
- packaged-jar startup smoke test:
  - `.\scripts\start-reader-backend.ps1 -Database novel_lite_release_validation`
  - executed through a temporary PowerShell process with `SERVER_PORT=19081`
  - startup log retained under:
    - `docs/testing/ai-logs/beginner-backend-jar-19081.out.log`
    - `docs/testing/ai-logs/beginner-backend-jar-19081.err.log`

Observed environment-check result on the current machine:
- Java found
- Node.js found
- npm found
- MariaDB client found
- packaged backend jar found
- database port `3306` reachable
- web port `8080` already in use
- backend port `8081` already in use
- script ended with `Environment check passed.`

Observed packaged-jar startup result on the current machine:
- backend started successfully on port `19081`
- startup log reached:
  - `Tomcat started on port 19081`
  - `Started NovelApplication`
- `DatabaseInitializer` ran in `reader` mode against `novel_lite_release_validation`

## Documentation Sync
- Updated: `docs/tasks/platform/2026-04-04-source-package-beginner-entrypoints-and-guide.md`
- Updated: `docs/tasks/platform/INDEX.md`
- Updated: `README.md`
- Updated: `docs/engineering/小白使用说明-源码包.md`
- Updated: `docs/engineering/reader-lite-quickstart.md`
- Updated: `docs/engineering/runtime-operations.md`
- Updated: `docs/engineering/database.md`
- Checked with no change needed: `docs/context/development-roadmap.md`

## Risks
- Windows batch wrappers can hide PowerShell execution-policy failures if not written carefully.
- The backend launcher relies on local database naming conventions unless the user overrides them.
- A source-package guide is still more fragile than a future pre-bundled environment package.

## Follow-ups
- Add a future environment-bundled distribution guide if the Baidu Netdisk package line becomes the primary public path.
- If the source-package path becomes the primary public distribution, consider a dedicated release folder that contains only the supported beginner entrypoints and docs.
- Consider a more guided startup status page or launcher only after the current split-script baseline proves stable.
