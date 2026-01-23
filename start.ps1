# FreeNovel Project Start Script
# Usage: Right-click this file -> Run with PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FreeNovel Project Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Docker status
Write-Host "Checking Docker status..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "[OK] Docker is running" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Docker is not running, please start Docker Desktop first" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""

# Check local MySQL service
Write-Host "Checking local MySQL service..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -eq "Running") {
    Write-Host "MySQL80 is running, need to stop to free port 3306" -ForegroundColor Yellow
    $stopMySQL = Read-Host "Stop MySQL80 service? (y/n)"
    if ($stopMySQL -eq 'y' -or $stopMySQL -eq 'Y') {
        Write-Host "Stopping MySQL80..." -ForegroundColor Yellow
        Stop-Service MySQL80
        Write-Host "[OK] MySQL80 stopped" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Cannot start project, port 3306 is occupied" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} else {
    Write-Host "[OK] Port 3306 is available" -ForegroundColor Green
}

Write-Host ""

# Start project
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting FreeNovel..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Using config: docker-compose.local-single.yml" -ForegroundColor Yellow
Write-Host "Starting services..." -ForegroundColor Yellow
Write-Host ""

docker-compose -f docker-compose.local-single.yml up -d

Write-Host ""

# Wait for services
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Check service status
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Service Status" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
docker ps --filter "name=novel"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Startup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Access URL: " -NoNewline -ForegroundColor Cyan
Write-Host "http://localhost:8081" -ForegroundColor Green
Write-Host ""
Write-Host "View logs: " -NoNewline -ForegroundColor Cyan
Write-Host "docker logs -f novel-app" -ForegroundColor White
Write-Host ""
Write-Host "Stop service: " -NoNewline -ForegroundColor Cyan
Write-Host "docker-compose -f docker-compose.local-single.yml down" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"