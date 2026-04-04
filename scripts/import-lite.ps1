[CmdletBinding()]
param(
    [string]$LiteDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) 'sql\lite'),

    [string]$Database = 'novel',

    [string]$ServerHost = '127.0.0.1',

    [int]$Port = 3306,

    [string]$User = 'root',

    [string]$Password = 'novel_root_password',

    [string]$MariaDbExePath
)

$ErrorActionPreference = 'Stop'

$resolvedLiteDirectory = (Resolve-Path -LiteralPath $LiteDirectory).Path
$schemaFile = Join-Path $resolvedLiteDirectory 'schema.sql'
$systemSeedFile = Join-Path $resolvedLiteDirectory 'seed-system-lite.sql'
$readerDemoFile = Join-Path $resolvedLiteDirectory 'seed-reader-demo.sql'

foreach ($requiredFile in @($schemaFile, $systemSeedFile, $readerDemoFile)) {
    if (-not (Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
        throw "Required lite package file not found: $requiredFile"
    }
}

$importScript = Join-Path $PSScriptRoot 'import-local-sql.ps1'

Write-Host "Lite package directory: $resolvedLiteDirectory"

& $importScript -SqlFile $schemaFile -Database $Database -ServerHost $ServerHost -Port $Port -User $User -Password $Password -MariaDbExePath $MariaDbExePath -EnsureDatabase
& $importScript -SqlFile $systemSeedFile -Database $Database -ServerHost $ServerHost -Port $Port -User $User -Password $Password -MariaDbExePath $MariaDbExePath
& $importScript -SqlFile $readerDemoFile -Database $Database -ServerHost $ServerHost -Port $Port -User $User -Password $Password -MariaDbExePath $MariaDbExePath

Write-Host 'Lite package import completed.'
