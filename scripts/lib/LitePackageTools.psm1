Set-StrictMode -Version Latest

function Get-LitePackageDefaults {
    [CmdletBinding()]
    param(
        [string]$RepoRoot = (Split-Path -Parent $PSScriptRoot)
    )

    $resolvedRepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
    $outputDirectory = Join-Path $resolvedRepoRoot 'sql\lite'

    [pscustomobject]@{
        OutputDirectory       = $outputDirectory
        DemoEmail             = 'demo_reader@lite.local'
        DemoPassword          = 'ReaderPass1'
        DemoPasswordHash      = '$2a$10$XI9xiL.62HGa1Cc0NS/PcOTkyTcoBuPqGWNJd5hZWBDR3/FUWbtRm'
        DemoInvitationCode    = 'LITE-DEMO-20260404'
        DemoInvitationCodeId  = 900000001L
        DemoUserId            = 900000001L
        SchemaTables          = @(
            'chapter',
            'chapter_comment',
            'chapter_image_links',
            'comment',
            'credential',
            'dictionary',
            'favorite_groups',
            'favorites',
            'invitation_code',
            'notes',
            'novel',
            'novel_chapter',
            'novel_tag',
            'reading_record',
            'tag',
            'user'
        )
        SystemSeedTables      = @('dictionary')
        ReaderDemoTables      = @(
            'novel',
            'novel_tag',
            'tag',
            'novel_chapter',
            'chapter',
            'chapter_image_links'
        )
    }
}

function Join-SqlNumericList {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [long[]]$Values
    )

    if (-not $Values -or $Values.Count -eq 0) {
        throw 'At least one numeric ID is required.'
    }

    ($Values | Sort-Object -Unique | ForEach-Object { [string]$_ }) -join ','
}

function Get-LiteCandidateNovelSql {
    [CmdletBinding()]
    param(
        [int]$Limit = 20
    )

    if ($Limit -lt 1) {
        throw 'Limit must be at least 1.'
    }

@"
SELECT
  n.id,
  n.title,
  n.author_name,
  IFNULL(ch.chapter_count, 0) AS chapter_count,
  IFNULL(nc.toc_count, 0) AS toc_count,
  IFNULL(img.image_link_count, 0) AS image_link_count,
  IFNULL(cm.comment_count, 0) AS comment_count
FROM novel n
LEFT JOIN (
  SELECT novel_id, COUNT(*) AS chapter_count
  FROM chapter
  WHERE is_deleted = 0
  GROUP BY novel_id
) ch ON ch.novel_id = n.id
LEFT JOIN (
  SELECT novel_id, COUNT(*) AS toc_count
  FROM novel_chapter
  GROUP BY novel_id
) nc ON nc.novel_id = n.id
LEFT JOIN (
  SELECT nc.novel_id, COUNT(*) AS image_link_count
  FROM chapter_image_links cil
  JOIN novel_chapter nc ON nc.chapter_true_id = cil.chapter_true_id
  GROUP BY nc.novel_id
) img ON img.novel_id = n.id
LEFT JOIN (
  SELECT c.novel_id, COUNT(*) AS comment_count
  FROM comment cm
  JOIN chapter c ON c.id = cm.chapter_id
  GROUP BY c.novel_id
) cm ON cm.novel_id = n.id
WHERE n.is_deleted = 0
  AND IFNULL(ch.chapter_count, 0) > 0
  AND IFNULL(nc.toc_count, 0) > 0
ORDER BY
  CASE WHEN IFNULL(ch.chapter_count, 0) = IFNULL(nc.toc_count, 0) THEN 0 ELSE 1 END,
  IFNULL(ch.chapter_count, 0) DESC,
  n.id ASC
LIMIT $Limit;
"@
}

function Get-LiteReaderDemoDumpSpecs {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [long[]]$NovelIds,

        [switch]$IncludeComments
    )

    $joinedIds = Join-SqlNumericList -Values $NovelIds
    $specs = @(
        [pscustomobject]@{ Table = 'novel'; Where = "id IN ($joinedIds)"; HexBlob = $false }
        [pscustomobject]@{ Table = 'novel_tag'; Where = "novel_id IN ($joinedIds)"; HexBlob = $false }
        [pscustomobject]@{ Table = 'tag'; Where = "id IN (SELECT DISTINCT tag_id FROM novel_tag WHERE novel_id IN ($joinedIds))"; HexBlob = $false }
        [pscustomobject]@{ Table = 'novel_chapter'; Where = "novel_id IN ($joinedIds)"; HexBlob = $false }
        [pscustomobject]@{ Table = 'chapter'; Where = "novel_id IN ($joinedIds) AND is_deleted = 0"; HexBlob = $true }
        [pscustomobject]@{ Table = 'chapter_image_links'; Where = "chapter_true_id IN (SELECT chapter_true_id FROM novel_chapter WHERE novel_id IN ($joinedIds))"; HexBlob = $false }
    )

    if ($IncludeComments) {
        $specs += [pscustomobject]@{ Table = 'comment'; Where = "chapter_id IN (SELECT id FROM chapter WHERE novel_id IN ($joinedIds) AND is_deleted = 0)"; HexBlob = $false }
        $specs += [pscustomobject]@{ Table = 'chapter_comment'; Where = "novel_id IN ($joinedIds)"; HexBlob = $false }
    }

    $specs
}

function New-LiteDemoAccountSql {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [pscustomobject]$Defaults
    )

@"
-- Reader-mode bootstrap account. Change this password after first deployment.
INSERT INTO invitation_code (id, code, used, bound_email, user_id)
VALUES ($($Defaults.DemoInvitationCodeId), '$($Defaults.DemoInvitationCode)', 1, '$($Defaults.DemoEmail)', $($Defaults.DemoUserId))
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  used = VALUES(used),
  bound_email = VALUES(bound_email),
  user_id = VALUES(user_id);

INSERT INTO user (id, email, password, invitation_code_id, point, upload, hide_read_books)
VALUES ($($Defaults.DemoUserId), '$($Defaults.DemoEmail)', '$($Defaults.DemoPasswordHash)', $($Defaults.DemoInvitationCodeId), 0, 0, 0)
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  password = VALUES(password),
  invitation_code_id = VALUES(invitation_code_id),
  point = VALUES(point),
  upload = VALUES(upload),
  hide_read_books = VALUES(hide_read_books);
"@
}

function Resolve-MySqlDumpPath {
    [CmdletBinding()]
    param(
        [string]$ExplicitPath
    )

    if ($ExplicitPath) {
        if (-not (Test-Path -LiteralPath $ExplicitPath)) {
            throw "mysqldump not found at '$ExplicitPath'."
        }
        return (Resolve-Path -LiteralPath $ExplicitPath).Path
    }

    $command = Get-Command mysqldump.exe -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidates = @(
        'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe',
        'D:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe'
    )

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw 'Unable to find mysqldump.exe. Install MySQL client tools or pass -MySqlDumpPath.'
}

function Resolve-MariaDbCliPath {
    [CmdletBinding()]
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
        'D:\Program Files\MariaDB 12.1\bin\mariadb.exe',
        'D:\Program Files\MariaDB 11.8\bin\mariadb.exe',
        'C:\Program Files\MariaDB 12.1\bin\mariadb.exe',
        'C:\Program Files\MariaDB 11.8\bin\mariadb.exe'
    )

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw 'Unable to find mariadb.exe. Install MariaDB client or pass -MariaDbExePath.'
}

Export-ModuleMember -Function @(
    'Get-LitePackageDefaults',
    'Join-SqlNumericList',
    'Get-LiteCandidateNovelSql',
    'Get-LiteReaderDemoDumpSpecs',
    'New-LiteDemoAccountSql',
    'Resolve-MySqlDumpPath',
    'Resolve-MariaDbCliPath'
)
