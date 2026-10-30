param(
    [ValidateSet('all','pilot','benchmark')][string]$Phase = 'all',
    [ValidateRange(2,10000)][int]$GamesPerPair = 50,
    [ValidateRange(1,64)][int]$Threads = 2,
    [string]$OutputDirectory = ''
)
$ErrorActionPreference = 'Stop'
if ($GamesPerPair % 2 -ne 0) { throw 'GamesPerPair must be even for balanced first-player assignments.' }
$arenaRepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) { $OutputDirectory = Join-Path $arenaRepoRoot 'docs/ai-results' }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
$arenaOutputArgument = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($OutputDirectory))
$arenaTempRoot = Join-Path ([IO.Path]::GetTempPath()) ('gomoku-ai-validation-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $arenaTempRoot | Out-Null
Copy-Item -LiteralPath (Join-Path $arenaRepoRoot 'src') -Destination $arenaTempRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'AIArena.java') -Destination $arenaTempRoot
$arenaRevision = (& git -C $arenaRepoRoot rev-parse HEAD)
if ($LASTEXITCODE -ne 0) { throw 'Cannot identify source revision' }
Push-Location -LiteralPath $arenaTempRoot
try {
    $arenaSources = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 -d classes @arenaSources AIArena.java
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    # ASCII transport avoids native-launcher code-page loss in Vietnamese Windows paths.
    & java '-Dstdout.encoding=UTF-8' -cp classes game.tools.AIArena --output-base64 $arenaOutputArgument --games $GamesPerPair --threads $Threads --phase $Phase --revision $arenaRevision
    if ($LASTEXITCODE -ne 0) { throw 'AI validation failed; inspect any partial results' }
} finally { Pop-Location }
Write-Output "Results: $OutputDirectory"
Write-Output "Temporary build retained: $arenaTempRoot"
