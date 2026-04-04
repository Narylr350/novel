[CmdletBinding()]
param(
    [int]$Limit = 20,

    [string]$Database = 'novel',

    [string]$ServerHost = '127.0.0.1',

    [int]$Port = 3306,

    [string]$User = 'root',

    [string]$Password = 'novel_root_password',

    [string]$MariaDbExePath
)

$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'lib\LitePackageTools.psm1') -Force

$resolvedMariaDbExe = Resolve-MariaDbCliPath -ExplicitPath $MariaDbExePath
$sql = Get-LiteCandidateNovelSql -Limit $Limit

Write-Host "MariaDB client: $resolvedMariaDbExe"
Write-Host "Target database: $Database"
Write-Host "Candidate limit: $Limit"

& $resolvedMariaDbExe `
    --default-character-set=utf8mb4 `
    -h $ServerHost `
    -P $Port `
    -u $User `
    "--password=$Password" `
    $Database `
    -e $sql

if ($LASTEXITCODE -ne 0) {
    throw "Candidate-novel probe failed with exit code $LASTEXITCODE."
}
