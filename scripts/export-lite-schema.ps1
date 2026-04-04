[CmdletBinding()]
param(
    [string]$Database = 'novel',

    [string]$ServerHost = '127.0.0.1',

    [int]$Port = 3306,

    [string]$User = 'root',

    [string]$Password = 'novel_root_password',

    [string]$MySqlDumpPath,

    [string]$OutputPath
)

$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSScriptRoot 'lib\LitePackageTools.psm1') -Force

$defaults = Get-LitePackageDefaults -RepoRoot (Split-Path -Parent $PSScriptRoot)
$resolvedDumpPath = Resolve-MySqlDumpPath -ExplicitPath $MySqlDumpPath

if (-not $OutputPath) {
    $OutputPath = Join-Path $defaults.OutputDirectory 'schema.sql'
}

$resolvedOutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$outputDirectory = Split-Path -Parent $resolvedOutputPath
if (-not (Test-Path -LiteralPath $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}

$arguments = @(
    '--default-character-set=utf8mb4',
    '--column-statistics=0',
    '--no-data',
    '--single-transaction',
    '--skip-lock-tables',
    '--skip-comments',
    '--skip-dump-date',
    '--set-charset',
    '-h', $ServerHost,
    '-P', $Port,
    '-u', $User,
    "--password=$Password",
    $Database
) + $defaults.SchemaTables

Write-Host "mysqldump: $resolvedDumpPath"
Write-Host "Output file: $resolvedOutputPath"
Write-Host "Schema tables: $($defaults.SchemaTables -join ', ')"

$schemaSql = & $resolvedDumpPath @arguments
if ($LASTEXITCODE -ne 0) {
    throw "Schema export failed with exit code $LASTEXITCODE."
}

[System.IO.File]::WriteAllText($resolvedOutputPath, $schemaSql, [System.Text.UTF8Encoding]::new($false))
Write-Host 'Schema export completed.'
