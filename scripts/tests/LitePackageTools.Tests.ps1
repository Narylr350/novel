$modulePath = Join-Path $PSScriptRoot '..\lib\LitePackageTools.psm1'
Import-Module $modulePath -Force

Describe 'LitePackageTools' {
    It 'defines reader-only schema tables and excludes maintainer tables' {
        $defaults = Get-LitePackageDefaults -RepoRoot (Join-Path $PSScriptRoot '..\..')

        ($defaults.SchemaTables -contains 'chapter') | Should Be $true
        ($defaults.SchemaTables -contains 'novel') | Should Be $true
        ($defaults.SchemaTables -contains 'user') | Should Be $true
        ($defaults.SchemaTables -contains 'credential') | Should Be $true
        ($defaults.SchemaTables -contains 'chapter_image_links') | Should Be $true
        ($defaults.SchemaTables -contains 'terminology') | Should Be $false
        ($defaults.SchemaTables -contains 'chapter_execute') | Should Be $false
    }

    It 'builds candidate SQL that filters for usable TOC-backed novels' {
        $sql = Get-LiteCandidateNovelSql -Limit 12

        $sql | Should Match 'toc_count'
        $sql | Should Match 'chapter_count'
        $sql | Should Match 'AND IFNULL\(nc\.toc_count, 0\) > 0'
        $sql | Should Match 'LIMIT 12'
    }

    It 'builds reader-demo dump specs for the selected novels' {
        $specs = Get-LiteReaderDemoDumpSpecs -NovelIds 101, 202, 202

        ($specs | Select-Object -ExpandProperty Table) | Should Be @(
            'novel',
            'novel_tag',
            'tag',
            'novel_chapter',
            'chapter',
            'chapter_image_links'
        )

        ($specs | Where-Object Table -eq 'chapter').Where | Should Be 'novel_id IN (101,202) AND is_deleted = 0'
        ($specs | Where-Object Table -eq 'chapter').HexBlob | Should Be $true
        ($specs | Where-Object Table -eq 'tag').Where | Should Match 'tag_id'
    }

    It 'generates deterministic demo-account seed SQL' {
        $defaults = Get-LitePackageDefaults -RepoRoot (Join-Path $PSScriptRoot '..\..')
        $sql = New-LiteDemoAccountSql -Defaults $defaults

        $sql | Should Match 'INSERT INTO invitation_code'
        $sql | Should Match 'INSERT INTO user'
        $sql | Should Match ([regex]::Escape($defaults.DemoEmail))
        $sql | Should Match ([regex]::Escape($defaults.DemoInvitationCode))
        $sql | Should Match ([regex]::Escape($defaults.DemoPasswordHash))
    }
}
