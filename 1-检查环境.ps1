# ============================================
# FreeNovel - 检查环境
# ============================================
#
# 使用方法：右键点击此文件 -> 使用 PowerShell 运行
#
# 功能：检查运行项目所需的软件是否已安装
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
Write-Title "FreeNovel 环境检查"

$passed = 0
$failed = 0

# --------------------------------------------
# 1. Java
# --------------------------------------------

Write-Host ""
Write-Host "  [1/4] Java" -ForegroundColor Cyan

if (Test-Command "java") {
    $javaVersionOutput = java -version 2>&1 | Select-Object -First 1
    $javaVersion = $javaVersionOutput -replace '.*"(\d+).*', '$1'

    if ([int]$javaVersion -ge 21) {
        Write-Success "Java $javaVersion (需要 21+)"
        $passed++
    } else {
        Write-Error "Java $javaVersion 版本过低 (需要 21+)"
        $failed++
    }
} else {
    Write-Error "Java 未安装"
    Write-Info "下载: https://adoptium.net/"
    $failed++
}

# --------------------------------------------
# 2. Node.js
# --------------------------------------------

Write-Host ""
Write-Host "  [2/4] Node.js" -ForegroundColor Cyan

if (Test-Command "node") {
    $nodeVersion = (node -v) -replace 'v(\d+).*', '$1'

    if ([int]$nodeVersion -ge 16) {
        Write-Success "Node.js v$nodeVersion (需要 16+)"
        $passed++
    } else {
        Write-Error "Node.js v$nodeVersion 版本过低 (需要 16+)"
        $failed++
    }
} else {
    Write-Error "Node.js 未安装"
    Write-Info "下载: https://nodejs.org/"
    $failed++
}

# --------------------------------------------
# 3. MariaDB
# --------------------------------------------

Write-Host ""
Write-Host "  [3/4] MariaDB" -ForegroundColor Cyan

if (Test-PortInUse $DB_PORT) {
    Write-Success "MariaDB 运行中 (端口 $DB_PORT)"
    $passed++
} else {
    $mariadbService = Get-Service -Name "MariaDB" -ErrorAction SilentlyContinue
    if ($mariadbService) {
        Write-Tip "MariaDB 已安装但未运行"
        Write-Info "请启动 MariaDB 服务"
    } else {
        Write-Error "MariaDB 未安装"
        Write-Info "下载: https://mariadb.org/download/"
    }
    $failed++
}

# --------------------------------------------
# 4. 配置文件
# --------------------------------------------

Write-Host ""
Write-Host "  [4/4] 配置文件" -ForegroundColor Cyan

$configOk = $true

if (Test-Path $SQL_PATH) {
    $sqlFiles = Get-ChildItem -Path $SQL_PATH -Filter "*.sql" -ErrorAction SilentlyContinue
    if ($sqlFiles.Count -gt 0) {
        Write-Success "SQL 目录: $SQL_PATH ($($sqlFiles.Count) 个文件)"
    } else {
        Write-Tip "SQL 目录存在但没有 .sql 文件"
        $configOk = $false
    }
} else {
    Write-Error "SQL 目录不存在: $SQL_PATH"
    Write-Info "请修改 config.ps1 中的 SQL_PATH"
    $configOk = $false
}

if ($configOk) {
    $passed++
} else {
    $failed++
}

# --------------------------------------------
# 结果
# --------------------------------------------

Write-Title "检查结果"

$total = $passed + $failed
Write-Host "  通过: $passed / $total" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

if ($failed -eq 0) {
    Write-Host "  所有检查通过！" -ForegroundColor Green
    Write-Host ""
    Write-Host "  下一步: 运行 2-初始化数据库.ps1" -ForegroundColor Cyan
} else {
    Write-Host "  请安装缺失的软件后重新检查" -ForegroundColor Yellow
}

Write-Host ""
Wait-Enter "按 Enter 退出"