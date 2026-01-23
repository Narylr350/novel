# ============================================
# FreeNovel - 启动项目
# ============================================
#
# 使用方法：右键点击此文件 -> 使用 PowerShell 运行
#
# 前提条件：
#   1. 已安装 JDK 21+
#   2. 已安装 Node.js 16+
#   3. 已安装 MariaDB 并导入数据
#
# ============================================

# 加载配置文件
$configPath = Join-Path $PSScriptRoot "config.ps1"
if (Test-Path $configPath) {
    . $configPath
} else {
    Write-Host "[错误] 找不到配置文件 config.ps1" -ForegroundColor Red
    Read-Host "按 Enter 退出"
    exit 1
}

# ============================================
# 主程序开始
# ============================================

Clear-Host
Write-Title "FreeNovel 启动项目"

Write-Host "  项目路径: $PROJECT_ROOT" -ForegroundColor Gray
Write-Host ""

# --------------------------------------------
# 第一步：环境检查
# --------------------------------------------

Write-Title "第一步：检查运行环境"

$allChecksPassed = $true

# 检查 Java
Write-Step "检查 Java..."
if (Test-Command "java") {
    $javaVersion = (java -version 2>&1 | Select-Object -First 1) -replace '.*"(.+)".*', '$1'
    Write-Success "Java 已安装 ($javaVersion)"
} else {
    Write-Error "Java 未安装！请先运行 1-检查环境.ps1"
    $allChecksPassed = $false
}

# 检查 Node.js
Write-Step "检查 Node.js..."
if (Test-Command "node") {
    $nodeVersion = node -v
    Write-Success "Node.js 已安装 ($nodeVersion)"
} else {
    Write-Error "Node.js 未安装！请先运行 1-检查环境.ps1"
    $allChecksPassed = $false
}

# 检查 MariaDB
Write-Step "检查 MariaDB..."
if (Test-PortInUse $DB_PORT) {
    Write-Success "MariaDB 运行中 (端口 $DB_PORT)"
} else {
    Write-Error "MariaDB 未运行！请启动 MariaDB 服务"
    Write-Info "提示: 打开 '服务' (services.msc) -> 启动 MariaDB"
    $allChecksPassed = $false
}

if (-not $allChecksPassed) {
    Write-Host ""
    Write-Error "环境检查未通过，请先解决上述问题"
    Wait-Enter "按 Enter 退出"
    exit 1
}

Write-Success "环境检查通过！"

# --------------------------------------------
# 第二步：启动后端
# --------------------------------------------

Write-Title "第二步：启动后端"

Write-Step "启动 Spring Boot..."
Write-Info "端口: $BACKEND_PORT"

if (Test-PortInUse $BACKEND_PORT) {
    Write-Tip "端口 $BACKEND_PORT 已被占用，可能后端已在运行"
    $continue = Read-Host "是否继续启动前端? (y/n)"
    if ($continue -ne 'y' -and $continue -ne 'Y') {
        exit 0
    }
} else {
    $mvnCmd = "mvn"
    $mvnwPath = Join-Path $BACKEND_PATH "mvnw.cmd"
    if (Test-Path $mvnwPath) {
        $mvnCmd = ".\mvnw.cmd"
    }

    $backendCmd = @"
cd '$BACKEND_PATH'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  FreeNovel 后端' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
Write-Host '正在启动...' -ForegroundColor Green
Write-Host '首次启动需要下载依赖，请耐心等待' -ForegroundColor Yellow
Write-Host ''
$mvnCmd spring-boot:run -Pdev
"@
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd

    Write-Success "后端正在新窗口中启动..."
}

# --------------------------------------------
# 第三步：启动前端
# --------------------------------------------

Write-Title "第三步：启动前端"

Write-Step "等待后端初始化 (10秒)..."
Start-Sleep -Seconds 10

Write-Step "启动 Vue 前端..."
Write-Info "端口: $FRONTEND_PORT"

$nodeModulesPath = Join-Path $FRONTEND_PATH "node_modules"
if (-not (Test-Path $nodeModulesPath)) {
    Write-Tip "首次运行，需要安装前端依赖..."
    $frontendCmd = @"
cd '$FRONTEND_PATH'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  FreeNovel 前端' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
Write-Host '正在安装依赖...' -ForegroundColor Yellow
npm install
Write-Host ''
Write-Host '正在启动...' -ForegroundColor Green
npm run serve
"@
} else {
    $frontendCmd = @"
cd '$FRONTEND_PATH'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  FreeNovel 前端' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
Write-Host '正在启动...' -ForegroundColor Green
npm run serve
"@
}

Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd

Write-Success "前端正在新窗口中启动..."

# --------------------------------------------
# 完成
# --------------------------------------------

Write-Title "启动完成！"

Write-Host "  访问地址:" -ForegroundColor Cyan
Write-Host ""
Write-Host "    http://localhost:$FRONTEND_PORT" -ForegroundColor Green
Write-Host ""
Write-Host "  提示:" -ForegroundColor Cyan
Write-Host "    - 后端启动需要 30-60 秒" -ForegroundColor Gray
Write-Host "    - 前端启动需要 10-20 秒" -ForegroundColor Gray
Write-Host "    - 关闭窗口即可停止服务" -ForegroundColor Gray
Write-Host ""

Wait-Enter "按 Enter 关闭此窗口"