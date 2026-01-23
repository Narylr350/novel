# Check MariaDB Database Script
# Usage: Right-click this file -> Run with PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FreeNovel Database Viewer" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if container is running
$mariadbRunning = docker ps --filter "name=novel-mariadb" --format "{{.Names}}"
if ($mariadbRunning -ne "novel-mariadb") {
    Write-Host "[ERROR] MariaDB container is not running" -ForegroundColor Red
    Write-Host "Please run start.ps1 first to start the project" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "[OK] MariaDB container is running" -ForegroundColor Green
Write-Host ""

# Show all databases
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All Databases:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
docker exec novel-mariadb mariadb -uroot -pnovel_root_password -e "SHOW DATABASES;"
Write-Host ""

# Show tables in novel database
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tables in novel database:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
docker exec novel-mariadb mariadb -uroot -pnovel_root_password -e "USE novel; SHOW TABLES;"
Write-Host ""

# Show table statistics
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Table Statistics:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
docker exec novel-mariadb mariadb -uroot -pnovel_root_password novel -e "
SELECT 
    table_name AS 'Table Name',
    table_rows AS 'Row Count',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'novel'
ORDER BY table_rows DESC;
"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Quick Commands:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Enter database CLI: " -NoNewline -ForegroundColor Yellow
Write-Host "docker exec -it novel-mariadb mariadb -uroot -pnovel_root_password novel" -ForegroundColor White
Write-Host ""
Write-Host "View table structure: " -NoNewline -ForegroundColor Yellow
Write-Host "docker exec novel-mariadb mariadb -uroot -pnovel_root_password novel -e 'DESCRIBE tablename;'" -ForegroundColor White
Write-Host ""
Write-Host "Query data example: " -NoNewline -ForegroundColor Yellow
Write-Host "docker exec novel-mariadb mariadb -uroot -pnovel_root_password novel -e 'SELECT * FROM user LIMIT 10;'" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"