# ============================================
# FreeNovel - 配置文件
# ============================================
#
# ★★★ 首次使用请修改下方的 SQL_PATH ★★★
#
# 路径格式：使用正斜杠 / 或双反斜杠 \\
# 路径中避免包含中文和空格
#
# ============================================

# --------------------------------------------
# 路径配置
# --------------------------------------------

# 项目根目录 (自动检测，一般无需修改)
$script:PROJECT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $script:PROJECT_ROOT) {
    $script:PROJECT_ROOT = "."
}

# 后端目录
$script:BACKEND_PATH = "$script:PROJECT_ROOT/novel"

# 前端目录
$script:FRONTEND_PATH = "$script:PROJECT_ROOT/free-novel-web"

# 数据文件目录
$script:APP_PATH = "$script:PROJECT_ROOT/app"

# ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
# SQL 文件目录 - 首次使用必须修改！
# 请改为你存放 SQL 文件的目录路径
# ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
$script:SQL_PATH = "D:/FreeNovel/03-sql"

# --------------------------------------------
# 数据库配置
# --------------------------------------------

$script:DB_HOST = "localhost"
$script:DB_PORT = "3306"
$script:DB_NAME = "novel"
$script:DB_USER = "root"
$script:DB_PASSWORD = "novel_root_password"

# --------------------------------------------
# 服务端口
# --------------------------------------------

$script:BACKEND_PORT = 8081    # 后端端口
$script:FRONTEND_PORT = 8080   # 前端端口

# ============================================
# 以下内容无需修改
# ============================================

# 输出函数
function Write-Title($text) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step($text) {
    Write-Host "[*] $text" -ForegroundColor Yellow
}

function Write-Success($text) {
    Write-Host "[OK] $text" -ForegroundColor Green
}

function Write-Error($text) {
    Write-Host "[X] $text" -ForegroundColor Red
}

function Write-Info($text) {
    Write-Host "    $text" -ForegroundColor Gray
}

function Write-Tip($text) {
    Write-Host "[i] $text" -ForegroundColor Magenta
}

# 检查命令是否存在
function Test-Command($command) {
    $null = Get-Command $command -ErrorAction SilentlyContinue
    return $?
}

# 等待用户回车
function Wait-Enter($message = "按 Enter 键继续...") {
    Write-Host ""
    Read-Host $message | Out-Null
}

# 检查端口是否被占用
function Test-PortInUse($port) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    return $null -ne $connection
}