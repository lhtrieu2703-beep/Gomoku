param([string]$OutputDirectory = '')
$ErrorActionPreference = 'Stop'
$boardRepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$boardTempRoot = Join-Path ([IO.Path]::GetTempPath()) ('gomoku-board-probe-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $boardTempRoot | Out-Null
foreach($boardItem in @('src','test','resources','lib','build.ps1')) {
    Copy-Item -LiteralPath (Join-Path $boardRepoRoot $boardItem) -Destination $boardTempRoot -Recurse
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'BoardPaintAgent.java') -Destination $boardTempRoot
if([string]::IsNullOrWhiteSpace($OutputDirectory)) { $OutputDirectory=Join-Path $boardTempRoot 'results' }
$OutputDirectory=[IO.Path]::GetFullPath($OutputDirectory)
Push-Location -LiteralPath $boardTempRoot
try {
    & ./build.ps1
    if($LASTEXITCODE -ne 0) { throw 'Build failed' }
    & javac -encoding UTF-8 -d probe_bin BoardPaintAgent.java
    if($LASTEXITCODE -ne 0) { throw 'Paint counter compilation failed (JDK 24+ required)' }
    'Premain-Class: game.tools.BoardPaintAgent' | Set-Content -Encoding ascii manifest.mf
    & jar --create --file board-agent.jar --manifest manifest.mf -C probe_bin .
    if($LASTEXITCODE -ne 0) { throw 'Paint counter packaging failed' }
    New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
    $boardFailures=0
    foreach($boardScale in @('1.0','1.25','1.5','2.0')) {
        # Pass an ASCII relative path to Java to avoid Windows launcher code-page loss.
        & java "-Dsun.java2d.uiScale=$boardScale" '-Dstdout.encoding=UTF-8' '-javaagent:board-agent.jar' -cp 'probe_bin;bin_test;bin' game.BoardDisplayProbe "scale-$boardScale" *> "scale-$boardScale.txt"
        if($LASTEXITCODE -ne 0) { $boardFailures++ }
        Write-Output "Scale $boardScale exit=$LASTEXITCODE"
        Copy-Item -LiteralPath "scale-$boardScale.txt" -Destination $OutputDirectory
        Copy-Item -LiteralPath "scale-$boardScale" -Destination $OutputDirectory -Recurse
    }
    Write-Output "Evidence: $OutputDirectory"
    if($boardFailures -ne 0) { throw "$boardFailures scales failed; inspect evidence" }
} finally { Pop-Location }
