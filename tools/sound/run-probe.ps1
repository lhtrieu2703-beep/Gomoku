param(
    [ValidatePattern('^[a-z0-9-]+$')][string] $Label = 'before',
    [int] $IntervalMs = 200,
    [switch] $DisableDiagnostics,
    [switch] $TenOnly
)
$ErrorActionPreference = 'Stop'
$soundProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$relativeRun = 'tools/sound/' + $Label
$runDirectory = Join-Path $soundProjectRoot $relativeRun
if (Test-Path -LiteralPath $runDirectory) { throw ('Probe output already exists: ' + $Label) }
New-Item -ItemType Directory -Path $runDirectory | Out-Null
$javaPath = (Get-Command java -CommandType Application | Select-Object -First 1).Source
$javacPath = (Get-Command javac -CommandType Application | Select-Object -First 1).Source
$classes = $relativeRun + '/classes'
Push-Location -LiteralPath $soundProjectRoot
try {
    & $javacPath -encoding UTF-8 -d $classes src/game/view/SoundManager.java tools/sound/SoundPlaybackProbe.java
    if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
    @(
        ('TIME_ASIA_SAIGON: ' + [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(7)).ToString('o'))
    ) | Set-Content -LiteralPath (Join-Path $runDirectory 'command.log') -Encoding UTF8
    $start = New-Object System.Diagnostics.ProcessStartInfo
    $start.FileName = $javaPath
    $diagnosticsValue = if ($DisableDiagnostics) { 'false' } else { 'true' }
    $start.Arguments = '-Djava.awt.headless=true -Dgomoku.sound.diagnostics=' + $diagnosticsValue + ' -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp "' + $classes + '" game.view.SoundPlaybackProbe ' + $IntervalMs + ' ' + $diagnosticsValue
    if ($TenOnly) { $start.Arguments += ' ten-only' }
    Add-Content -LiteralPath (Join-Path $runDirectory 'command.log') -Encoding UTF8 -Value ('COMMAND: ' + $javaPath + ' ' + $start.Arguments)
    $start.WorkingDirectory = $soundProjectRoot
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $start
    try {
        [void] $process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        [IO.File]::WriteAllText((Join-Path $runDirectory 'probe.stdout.log'), $stdout.GetAwaiter().GetResult(), $utf8)
        [IO.File]::WriteAllText((Join-Path $runDirectory 'probe.stderr.log'), $stderr.GetAwaiter().GetResult(), $utf8)
        $probeExit = $process.ExitCode
    } finally { $process.Dispose() }
    Add-Content -LiteralPath (Join-Path $runDirectory 'command.log') -Encoding UTF8 -Value ('EXIT_CODE: ' + $probeExit)
    Get-Content -LiteralPath (Join-Path $runDirectory 'probe.stdout.log') -Encoding UTF8
    if ($probeExit -ne 0) { throw ('Probe failed: ' + $probeExit) }
} finally { Pop-Location }
