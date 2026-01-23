# ============================================
# FreeNovel - 查看数据库
# ============================================
#
# 使用方法：右键点击此文件 -> 使用 PowerShell 运行
#
# 功能：查看数据库状态和数据统计
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
Write-Title "FreeNovel 查看数据库"

# 检查数据库
Write-Step "检查 MariaDB..."

if (-not (Test-PortInUse $DB_PORT)) {
    Write-Error "MariaDB 未运行！"
    Wait-Enter "按 Enter 退出"
    exit 1
}
Write-Success "MariaDB 运行中"

# 定义查询函数
function Invoke-Query($query) {
    mariadb -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" $DB_NAME -e "$query" 2>$null
}

# 显示数据表
Write-Title "数据表"

$tables = Invoke-Query "SHOW TABLES;"
if ($tables) {
    $tables | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
} else {
    Write-Tip "数据库为空"
    Write-Info "请先运行 3-导入数据.ps1"
}

# 显示统计
Write-Title "数据统计"

$statsQuery = @"
SELECT
    table_name AS '表名',
    table_rows AS '行数',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'MB'
FROM information_schema.tables
WHERE table_schema = '$DB_NAME'
ORDER BY table_rows DESC;
"@

$stats = Invoke-Query $statsQuery
if ($stats) {
    $stats | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
}

# 总大小
Write-Title "存储大小"

$sizeQuery = "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024 / 1024, 2) FROM information_schema.tables WHERE table_schema = '$DB_NAME';"
$totalSize = Invoke-Query $sizeQuery
if ($totalSize) {
    $sizeValue = ($totalSize | Select-Object -Last 1).Trim()
    Write-Host "  总大小: $sizeValue GB" -ForegroundColor Green
}

# 命令提示
Write-Title "常用命令"

Write-Host "  进入数据库:" -ForegroundColor Yellow
Write-Host "  mariadb -u $DB_USER -p $DB_NAME" -ForegroundColor White
Write-Host ""

Wait-Enter "按 Enter 退出"