# FreeNovel Local Development Start Script
# Usage: Right-click -> Run with PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FreeNovel Local Dev Environment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check MariaDB container
Write-Host "Checking MariaDB container..." -ForegroundColor Yellow
$mariadbRunning = docker ps --filter "name=novel-mariadb" --format "{{.Names}}"
if ($mariadbRunning -ne "novel-mariadb") {
    Write-Host "[ERROR] MariaDB container not running" -ForegroundColor Red
    Write-Host "Please start MariaDB first:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose.local-single.yml up -d mariadb" -ForegroundColor White
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "[OK] MariaDB is running" -ForegroundColor Green
Write-Host ""

# Check if Docker app container is running (should be stopped for local dev)
$appRunning = docker ps --filter "name=novel-app" --format "{{.Names}}"
if ($appRunning -eq "novel-app") {
    Write-Host "Stopping Docker app container..." -ForegroundColor Yellow
    docker stop novel-app
    Write-Host "[OK] Docker app container stopped" -ForegroundColor Green
}

# Check Java
Write-Host "Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "[OK] $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java not found, please install JDK 17" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Node.js
Write-Host "Checking Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node -v
    Write-Host "[OK] Node.js $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Node.js not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Backend (Spring Boot)..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Start Spring Boot in background
$backendPath = "D:\Narylr\FreeNovel\novel"
Write-Host "Backend path: $backendPath" -ForegroundColor Gray

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; Write-Host 'Starting Spring Boot...' -ForegroundColor Green; mvn spring-boot:run -Pdev" -WindowStyle Normal

Write-Host "[OK] Backend starting in new window..." -ForegroundColor Green
Write-Host ""

# Wait a bit for backend to start
Write-Host "Waiting 10 seconds before starting frontend..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Frontend (Vue)..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Start Vue frontend in background
$frontendPath = "D:\Narylr\FreeNovel\free-novel-web"
Write-Host "Frontend path: $frontendPath" -ForegroundColor Gray

# Check if node_modules exists
if (-not (Test-Path "$frontendPath\node_modules")) {
    Write-Host "Installing npm dependencies (first time)..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendPath'; Write-Host 'Installing dependencies...' -ForegroundColor Yellow; npm install; Write-Host 'Starting Vue dev server...' -ForegroundColor Green; npm run serve" -WindowStyle Normal
} else {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendPath'; Write-Host 'Starting Vue dev server...' -ForegroundColor Green; npm run serve" -WindowStyle Normal
}

Write-Host "[OK] Frontend starting in new window..." -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Services Starting..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend (Spring Boot):" -ForegroundColor Cyan
Write-Host "  - Starting on: http://localhost:8081" -ForegroundColor White
Write-Host "  - Check backend window for startup logs" -ForegroundColor Gray
Write-Host ""
Write-Host "Frontend (Vue Dev Server):" -ForegroundColor Cyan
Write-Host "  - Will start on: http://localhost:8080 (default)" -ForegroundColor White
Write-Host "  - Check frontend window for dev server URL" -ForegroundColor Gray
Write-Host ""
Write-Host "Database (MariaDB in Docker):" -ForegroundColor Cyan
Write-Host "  - Running on: localhost:3306" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  - Backend takes 30-60 seconds to start" -ForegroundColor Gray
Write-Host "  - Frontend takes 10-20 seconds to start" -ForegroundColor Gray
Write-Host "  - Access the app via Frontend URL (usually http://localhost:8080)" -ForegroundColor Gray
Write-Host "  - Close the PowerShell windows to stop services" -ForegroundColor Gray
Write-Host ""

Read-Host "Press Enter to close this window"