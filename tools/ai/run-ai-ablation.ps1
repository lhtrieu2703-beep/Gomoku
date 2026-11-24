param(
    [ValidateSet('search-only','full')][string]$Mode = 'search-only',
    [ValidateRange(1,1000)][int]$Measurements = 20,
    [ValidateRange(0,100)][int]$Warmups = 5,
    [string]$OutputDirectory = '',
    [string]$Positions = '',
    [ValidateRange(1,30000)][int]$TimeoutMs = 30000
)
$ErrorActionPreference = 'Stop'
$ablationRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) { $OutputDirectory = Join-Path $ablationRoot 'docs/ai-results-ablation' }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
$ablationOutputArgument = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($OutputDirectory))
$ablationTempRoot = Join-Path ([IO.Path]::GetTempPath()) ('gomoku-ablation-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $ablationTempRoot | Out-Null
Copy-Item -LiteralPath (Join-Path $ablationRoot 'src') -Destination $ablationTempRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'AIArena.java'), (Join-Path $PSScriptRoot 'AIAlphaBetaAblation.java') -Destination $ablationTempRoot
$ablationRevision = (& git -C $ablationRoot rev-parse HEAD)
if ($LASTEXITCODE -ne 0) { throw 'Cannot identify source revision' }
$ablationDirty = @(& git -C $ablationRoot status --porcelain -- src tools/ai)
if ($ablationDirty.Count -gt 0) { $ablationRevision += ' (working tree changes in src/tools/ai)' }
$env:ABLATION_CPU_NAME = (Get-ItemProperty -LiteralPath 'HKLM:\HARDWARE\DESCRIPTION\System\CentralProcessor\0' -Name ProcessorNameString).ProcessorNameString.Trim()
$env:ABLATION_PHYSICAL_CORES = (Get-CimInstance Win32_Processor | Measure-Object -Property NumberOfCores -Sum).Sum
Push-Location -LiteralPath $ablationTempRoot
try {
    $ablationSources = @(Get-ChildItem -LiteralPath 'src/game/ai','src/game/model' -Recurse -Filter '*.java' | ForEach-Object FullName)
    & javac -encoding UTF-8 -d classes @ablationSources AIArena.java AIAlphaBetaAblation.java
    if ($LASTEXITCODE -ne 0) { throw 'Ablation compilation failed' }
    $ablationArguments = @('-Xms256m','-Xmx1g','-Dstdout.encoding=UTF-8','-cp','classes','game.tools.AIAlphaBetaAblation',
        '--mode',$Mode,'--measurements',$Measurements,'--warmups',$Warmups,'--output-dir-base64',$ablationOutputArgument,
        '--timeout-ms',$TimeoutMs,'--revision',$ablationRevision)
    if ($Positions) { $ablationArguments += @('--positions',$Positions) }
    & java @ablationArguments
    $ablationExit = $LASTEXITCODE
    if ($ablationExit -eq 2) { Write-Warning "TIMEOUT: partial data retained at $OutputDirectory" }
    elseif ($ablationExit -ne 0) { throw "Ablation failed with exit code $ablationExit" }
} finally { Pop-Location }
$ablationSummary = Join-Path $PSScriptRoot 'summarize-ai-ablation.py'
if (Test-Path -LiteralPath $ablationSummary) {
    & python $ablationSummary --output-dir $OutputDirectory
    if ($LASTEXITCODE -ne 0 -and $ablationExit -ne 2) { throw 'Ablation validation/summary reported an anomaly; inspect its report' }
}
Write-Output "Results: $OutputDirectory"
Write-Output "Build copy: $ablationTempRoot"
if ($ablationExit -eq 2) { exit 2 }
