# ============================================
# FreeNovel - 导入数据
# ============================================
#
# 使用方法：右键点击此文件 -> 使用 PowerShell 运行
#
# 功能：导入 SQL 数据文件到数据库
#
# 前提条件：
#   1. 已运行 2-初始化数据库.ps1
#   2. SQL 文件已放置到指定目录
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
Write-Title "FreeNovel 导入数据"

Write-Host "  SQL 目录: $SQL_PATH" -ForegroundColor Gray
Write-Host ""

# --------------------------------------------
# 第一步：检查 SQL 文件
# --------------------------------------------

Write-Title "第一步：检查 SQL 文件"

if (-not (Test-Path $SQL_PATH)) {
    Write-Error "SQL 目录不存在: $SQL_PATH"
    Write-Host ""
    Write-Host "  请修改 config.ps1 中的 SQL_PATH" -ForegroundColor Yellow
    Write-Host ""
    Wait-Enter "按 Enter 退出"
    exit 1
}

Write-Success "SQL 目录存在"

# 检查文件
$sqlFiles = @{
    "小表" = @("credential.sql", "dictionary.sql", "invitation_code.sql", "user.sql")
    "扩容表" = @("expand.sql")
    "主表" = @("main.sql")
}

Write-Host ""
Write-Host "  检测到的文件:" -ForegroundColor Cyan

$availableFiles = @()
foreach ($category in $sqlFiles.Keys) {
    Write-Host "  [$category]" -ForegroundColor Yellow
    foreach ($fileName in $sqlFiles[$category]) {
        $filePath = Join-Path $SQL_PATH $fileName
        if (Test-Path $filePath) {
            $fileSize = [math]::Round((Get-Item $filePath).Length / 1MB, 1)
            Write-Host "    [OK] $fileName ($fileSize MB)" -ForegroundColor Green
            $availableFiles += @{ Path = $filePath; Name = $fileName; Category = $category }
        } else {
            Write-Host "    [--] $fileName (未找到)" -ForegroundColor Gray
        }
    }
}

if ($availableFiles.Count -eq 0) {
    Write-Host ""
    Write-Error "没有找到 SQL 文件！"
    Wait-Enter "按 Enter 退出"
    exit 1
}

# --------------------------------------------
# 第二步：检查数据库
# --------------------------------------------

Write-Title "第二步：检查数据库"

Write-Step "检查 MariaDB..."
if (-not (Test-PortInUse $DB_PORT)) {
    Write-Error "MariaDB 未运行！"
    Wait-Enter "按 Enter 退出"
    exit 1
}
Write-Success "MariaDB 运行中"

# 定义导入函数
function Import-SqlFile($filePath) {
    Get-Content $filePath -Encoding UTF8 | mariadb -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" --default-character-set=utf8mb4 $DB_NAME 2>&1
    return $LASTEXITCODE -eq 0
}

function Import-SqlFileLarge($filePath) {
    cmd /c "type `"$filePath`" | mariadb -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME"
    return $LASTEXITCODE -eq 0
}

# --------------------------------------------
# 第三步：选择导入内容
# --------------------------------------------

Write-Title "第三步：选择导入内容"

Write-Host "  [1] 仅小表 (快速测试)" -ForegroundColor White
Write-Host "  [2] 小表 + 扩容表" -ForegroundColor White
Write-Host "  [3] 全部数据" -ForegroundColor White
Write-Host "  [0] 退出" -ForegroundColor Gray
Write-Host ""

$choice = Read-Host "请选择 (0-3)"

switch ($choice) {
    "0" { exit 0 }
    "1" { $importCategories = @("小表") }
    "2" { $importCategories = @("小表", "扩容表") }
    "3" { $importCategories = @("小表", "扩容表", "主表") }
    default {
        Write-Error "无效选项"
        Wait-Enter "按 Enter 退出"
        exit 1
    }
}

# --------------------------------------------
# 第四步：导入
# --------------------------------------------

Write-Title "第四步：导入数据"

$successCount = 0
$failCount = 0

foreach ($category in $importCategories) {
    Write-Host ""
    Write-Host "  导入 [$category]..." -ForegroundColor Cyan

    $filesToImport = $availableFiles | Where-Object { $_.Category -eq $category }

    foreach ($file in $filesToImport) {
        Write-Step "导入 $($file.Name)..."

        $fileSize = (Get-Item $file.Path).Length / 1MB

        if ($fileSize -gt 500) {
            Write-Info "大文件，使用流式导入..."
            $result = Import-SqlFileLarge $file.Path
        } else {
            $result = Import-SqlFile $file.Path
        }

        if ($result) {
            Write-Success "$($file.Name) 完成"
            $successCount++
        } else {
            Write-Error "$($file.Name) 失败"
            $failCount++
        }
    }
}

# --------------------------------------------
# 完成
# --------------------------------------------

Write-Title "导入完成"

Write-Host "  成功: $successCount 个文件" -ForegroundColor Green
if ($failCount -gt 0) {
    Write-Host "  失败: $failCount 个文件" -ForegroundColor Red
}
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "  下一步: 运行 4-启动项目.ps1" -ForegroundColor Green
}

Write-Host ""
Wait-Enter "按 Enter 退出"