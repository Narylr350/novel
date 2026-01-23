# ============================================
# FreeNovel - 初始化数据库
# ============================================
#
# 使用方法：右键点击此文件 -> 使用 PowerShell 运行
#
# 功能：创建数据库和用户
#
# 前提条件：
#   1. 已安装 MariaDB
#   2. MariaDB 服务已启动
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
Write-Title "FreeNovel 初始化数据库"

Write-Host "  此脚本将:" -ForegroundColor Cyan
Write-Host "    1. 创建 novel 数据库" -ForegroundColor Gray
Write-Host "    2. 创建数据库用户" -ForegroundColor Gray
Write-Host ""

# --------------------------------------------
# 第一步：检查 MariaDB
# --------------------------------------------

Write-Title "第一步：检查 MariaDB"

Write-Step "检查服务状态..."

if (Test-PortInUse $DB_PORT) {
    Write-Success "MariaDB 运行中 (端口 $DB_PORT)"
} else {
    Write-Error "MariaDB 未运行！"
    Write-Host ""
    Write-Host "  启动方法:" -ForegroundColor Yellow
    Write-Host "  打开 '服务' (services.msc) -> 启动 MariaDB" -ForegroundColor Gray
    Write-Host ""
    Wait-Enter "按 Enter 退出"
    exit 1
}

# 检查命令行工具
Write-Step "检查命令行工具..."
$dbCmd = $null
if (Test-Command "mariadb") {
    $dbCmd = "mariadb"
    Write-Success "mariadb 命令可用"
} elseif (Test-Command "mysql") {
    $dbCmd = "mysql"
    Write-Success "mysql 命令可用"
} else {
    Write-Error "找不到数据库命令行工具！"
    Write-Host ""
    Write-Host "  解决方法:" -ForegroundColor Yellow
    Write-Host "  将 MariaDB 安装目录的 bin 文件夹添加到系统 PATH" -ForegroundColor Gray
    Write-Host "  例如: C:\Program Files\MariaDB 11.x\bin" -ForegroundColor Gray
    Write-Host ""
    Wait-Enter "按 Enter 退出"
    exit 1
}

# --------------------------------------------
# 第二步：连接数据库
# --------------------------------------------

Write-Title "第二步：连接数据库"

Write-Host "  请输入 MariaDB 的 root 密码" -ForegroundColor Cyan
Write-Host "  (安装 MariaDB 时设置的密码)" -ForegroundColor Gray
Write-Host ""

$rootPassword = Read-Host "root 密码" -AsSecureString
$rootPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($rootPassword))

Write-Step "测试连接..."
$testResult = & $dbCmd -h localhost -P $DB_PORT -u root -p"$rootPasswordPlain" -e "SELECT 1" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Error "连接失败！请检查密码是否正确"
    Wait-Enter "按 Enter 退出"
    exit 1
}
Write-Success "连接成功"

# --------------------------------------------
# 第三步：创建数据库
# --------------------------------------------

Write-Title "第三步：创建数据库"

# 检查数据库是否已存在
Write-Step "检查 novel 数据库..."
$dbExists = & $dbCmd -h localhost -P $DB_PORT -u root -p"$rootPasswordPlain" -e "SHOW DATABASES LIKE 'novel'" 2>&1

if ($dbExists -match "novel") {
    Write-Tip "novel 数据库已存在"
    $overwrite = Read-Host "是否重新创建? (会清空数据) (y/n)"
    if ($overwrite -eq 'y' -or $overwrite -eq 'Y') {
        & $dbCmd -h localhost -P $DB_PORT -u root -p"$rootPasswordPlain" -e "DROP DATABASE novel" 2>&1 | Out-Null
    } else {
        Write-Info "保留现有数据库"
    }
}

Write-Step "创建数据库..."
& $dbCmd -h localhost -P $DB_PORT -u root -p"$rootPasswordPlain" -e "CREATE DATABASE IF NOT EXISTS novel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>&1 | Out-Null
Write-Success "数据库创建成功"

Write-Step "创建用户..."
$createUserSql = @"
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON novel.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
"@
& $dbCmd -h localhost -P $DB_PORT -u root -p"$rootPasswordPlain" -e $createUserSql 2>&1 | Out-Null
Write-Success "用户创建成功"

# --------------------------------------------
# 完成
# --------------------------------------------

Write-Title "初始化完成！"

Write-Host "  数据库信息:" -ForegroundColor Cyan
Write-Host "    数据库: novel" -ForegroundColor White
Write-Host "    用户名: $DB_USER" -ForegroundColor White
Write-Host "    密码:   $DB_PASSWORD" -ForegroundColor White
Write-Host ""
Write-Host "  下一步: 运行 3-导入数据.ps1" -ForegroundColor Green
Write-Host ""

Wait-Enter "按 Enter 退出"