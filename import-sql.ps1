# SQL Data Import Script
# Usage: Right-click -> Run with PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FreeNovel Database Import" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$sqlPath = "D:\Narylr\学习资料\sql"
$expandTablePath = "D:\Narylr\学习资料\sql\扩容表.sql"
$mainTablePath = "D:\Narylr\学习资料\sql\主表.sql"

Write-Host "SQL Path: $sqlPath" -ForegroundColor Gray

# Check SQL directory
if (-not (Test-Path $sqlPath)) {
    Write-Host "[ERROR] SQL directory not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Docker
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "[OK] Docker running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker not running" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check MariaDB
Write-Host "Checking MariaDB..." -ForegroundColor Yellow
$mariadbRunning = docker ps --filter "name=novel-mariadb" --format "{{.Names}}"
if ($mariadbRunning -ne "novel-mariadb") {
    Write-Host "Starting MariaDB..." -ForegroundColor Yellow
    docker-compose -f docker-compose.local-single.yml up -d mariadb
    Start-Sleep -Seconds 15
}
Write-Host "[OK] MariaDB running" -ForegroundColor Green
Write-Host ""

# Import small tables
Write-Host "Importing small tables..." -ForegroundColor Cyan
$smallTables = @("credential.sql", "dictionary.sql", "invitation_code.sql", "user.sql")

foreach ($table in $smallTables) {
    $filePath = Join-Path $sqlPath $table
    if (Test-Path $filePath) {
        Write-Host "  $table ..." -ForegroundColor Yellow
        Get-Content $filePath -Encoding UTF8 | docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password --default-character-set=utf8mb4 novel 2>&1 | Out-Null
        if ($?) {
            Write-Host "  [OK]" -ForegroundColor Green
        } else {
            Write-Host "  [FAILED]" -ForegroundColor Red
        }
    }
}

Write-Host ""

# Import scaling table
Write-Host "Importing scaling table (360MB)..." -ForegroundColor Cyan

if (Test-Path $expandTablePath) {
    Get-Content $expandTablePath -Encoding UTF8 | docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password --default-character-set=utf8mb4 novel
    Write-Host "[OK] Scaling table imported" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Scaling table not found" -ForegroundColor Red
}

Write-Host ""

# Import main table
Write-Host "Importing main table (20.8GB)..." -ForegroundColor Cyan
Write-Host "WARNING: Takes 30-60 minutes!" -ForegroundColor Yellow

if (Test-Path $mainTablePath) {
    $confirm = Read-Host "Import now? (y/n)"
    if ($confirm -eq 'y' -or $confirm -eq 'Y') {
        Write-Host "Importing... please wait (streaming mode)" -ForegroundColor Yellow
        Write-Host "This will take 30-60 minutes for 20GB file" -ForegroundColor Gray
        # Use cmd to stream file directly to avoid memory issues
        cmd /c "type `"$mainTablePath`" | docker exec -i novel-mariadb mariadb -uroot -pnovel_root_password --default-character-set=utf8mb4 novel"
        Write-Host "[OK] Done!" -ForegroundColor Green
    } else {
        Write-Host "Skipped" -ForegroundColor Yellow
    }
} else {
    Write-Host "[ERROR] Main table not found" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Access: http://localhost:8081" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit"
