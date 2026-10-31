param(
    [ValidateRange(1,64)][int]$Openings = 16,
    [ValidateRange(2,1000)][int]$Measurements = 20,
    [ValidateRange(1,100)][int]$Warmups = 5,
    [ValidateRange(1,64)][int]$Threads = 2,
    [string]$OutputDirectory = ''
)
$ErrorActionPreference = 'Stop'
$arenaWatch = [Diagnostics.Stopwatch]::StartNew()
$arenaRepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) { $OutputDirectory = Join-Path $arenaRepoRoot 'docs/ai-results' }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
$arenaOutputArgument = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($OutputDirectory))
$arenaTempRoot = Join-Path ([IO.Path]::GetTempPath()) ('gomoku-ai-v2-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $arenaTempRoot | Out-Null
Copy-Item -LiteralPath (Join-Path $arenaRepoRoot 'src') -Destination $arenaTempRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'AIArena.java') -Destination $arenaTempRoot
$arenaRevision = (& git -C $arenaRepoRoot rev-parse HEAD)
if ($LASTEXITCODE -ne 0) { throw 'Cannot identify source revision' }
$env:ARENA_CPU_NAME = (Get-ItemProperty -LiteralPath 'HKLM:\HARDWARE\DESCRIPTION\System\CentralProcessor\0' -Name ProcessorNameString).ProcessorNameString
Push-Location -LiteralPath $arenaTempRoot
try {
    $arenaSources = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 -d classes @arenaSources AIArena.java
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    & java '-Dstdout.encoding=UTF-8' -cp classes game.tools.AIArena --output-base64 $arenaOutputArgument --openings $Openings --threads $Threads --measurements $Measurements --warmups $Warmups --revision $arenaRevision
    if ($LASTEXITCODE -ne 0) { throw 'AI validation failed' }
} finally { Pop-Location }
& python (Join-Path $PSScriptRoot 'summarize-ai-results.py') --output $OutputDirectory
if ($LASTEXITCODE -ne 0) { throw 'Summary generation failed' }
$arenaWatch.Stop()
[IO.File]::AppendAllText((Join-Path $OutputDirectory 'environment.md'), "`n- End-to-end runner seconds (build + measurements + summaries): $($arenaWatch.Elapsed.TotalSeconds.ToString('F6',[Globalization.CultureInfo]::InvariantCulture))`n", [Text.UTF8Encoding]::new($false))
Write-Output "Results: $OutputDirectory"
Write-Output "Runner seconds: $($arenaWatch.Elapsed.TotalSeconds)"
