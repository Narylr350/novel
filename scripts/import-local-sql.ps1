[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$SqlFile,

    [string]$Database = "novel",

    [string]$ServerHost = "127.0.0.1",

    [int]$Port = 3306,

    [string]$User = "root",

    [string]$Password = "novel_root_password",

    [string]$MariaDbExePath,

    [switch]$EnsureDatabase
)

$ErrorActionPreference = "Stop"

function Resolve-MariaDbExe {
    param(
        [string]$ExplicitPath
    )

    if ($ExplicitPath) {
        if (-not (Test-Path -LiteralPath $ExplicitPath)) {
            throw "MariaDB client not found at '$ExplicitPath'."
        }
        return (Resolve-Path -LiteralPath $ExplicitPath).Path
    }

    $command = Get-Command mariadb.exe -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidates = @(
        "D:\Program Files\MariaDB 12.1\bin\mariadb.exe",
        "D:\Program Files\MariaDB 11.8\bin\mariadb.exe",
        "C:\Program Files\MariaDB 12.1\bin\mariadb.exe",
        "C:\Program Files\MariaDB 11.8\bin\mariadb.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw "Unable to find mariadb.exe. Install MariaDB client or pass -MariaDbExePath."
}

function Invoke-MariaDbClient {
    param(
        [string]$ExePath,
        [string[]]$Arguments,
        [string]$InputFile
    )

    # Use the client process stdin directly instead of PowerShell redirection.
    # This is the import path validated in this repository for Windows MariaDB.
    $process = Start-Process `
        -FilePath $ExePath `
        -ArgumentList $Arguments `
        -RedirectStandardInput $InputFile `
        -Wait `
        -PassThru `
        -NoNewWindow

    if ($process.ExitCode -ne 0) {
        throw "MariaDB client exited with code $($process.ExitCode)."
    }
}

$resolvedSqlFile = (Resolve-Path -LiteralPath $SqlFile).Path
if (-not (Test-Path -LiteralPath $resolvedSqlFile -PathType Leaf)) {
    throw "SQL file not found: $SqlFile"
}

$resolvedMariaDbExe = Resolve-MariaDbExe -ExplicitPath $MariaDbExePath
$clientArgs = @(
    "--default-character-set=utf8mb4",
    "-h$ServerHost",
    "-P$Port",
    "-u$User",
    "-p$Password"
)

Write-Host "MariaDB client: $resolvedMariaDbExe"
Write-Host "SQL file: $resolvedSqlFile"
Write-Host "Target database: $Database"
Write-Host "Target server: $ServerHost`:$Port"

if ($EnsureDatabase) {
    $bootstrapFile = [System.IO.Path]::GetTempFileName()
    try {
        $bootstrapSql = "CREATE DATABASE IF NOT EXISTS ``$Database`` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
        [System.IO.File]::WriteAllText($bootstrapFile, $bootstrapSql, [System.Text.UTF8Encoding]::new($false))
        Write-Host "Ensuring database exists..."
        Invoke-MariaDbClient -ExePath $resolvedMariaDbExe -Arguments $clientArgs -InputFile $bootstrapFile
    }
    finally {
        if (Test-Path -LiteralPath $bootstrapFile) {
            Remove-Item -LiteralPath $bootstrapFile -Force
        }
    }
}

Write-Host "Starting import..."
Invoke-MariaDbClient -ExePath $resolvedMariaDbExe -Arguments ($clientArgs + @($Database)) -InputFile $resolvedSqlFile
Write-Host "Import completed."
