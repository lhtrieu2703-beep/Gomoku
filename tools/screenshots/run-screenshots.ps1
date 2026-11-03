param([string] $OutputDirectory = 'docs/screenshots')
$ErrorActionPreference = 'Stop'
$screenshotProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$allowedImageRoot = [IO.Path]::GetFullPath((Join-Path $screenshotProjectRoot 'docs/screenshots'))
$requestedOutput = if ([IO.Path]::IsPathRooted($OutputDirectory)) {
    [IO.Path]::GetFullPath($OutputDirectory)
} else { [IO.Path]::GetFullPath((Join-Path $screenshotProjectRoot $OutputDirectory)) }
if ($requestedOutput -ne $allowedImageRoot -and
        -not $requestedOutput.StartsWith($allowedImageRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Images must stay inside docs/screenshots/.'
}
$relativeOutput = $requestedOutput.Substring($screenshotProjectRoot.Length + 1).Replace('\', '/')
if ($relativeOutput -match '[^\x20-\x7E]') { throw 'Use an ASCII output subdirectory to avoid Windows Java launcher code-page loss.' }
foreach ($name in @('01_title.png','02_menu.png','03_setup_dialog.png','04_board_playing.png','05_settings.png','06_rules.png','07_lan_dialog.png')) {
    if (Test-Path -LiteralPath (Join-Path $requestedOutput $name)) { throw ('Refusing to overwrite existing image: ' + $name) }
}
$runId = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(7)).ToString('yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 6)
$relativeRun = 'tools/screenshots/runs/' + $runId
$absoluteRun = Join-Path $screenshotProjectRoot $relativeRun
$relativeClasses = $relativeRun + '/classes'
New-Item -ItemType Directory -Path (Join-Path $screenshotProjectRoot $relativeClasses) | Out-Null
$javaPath = (Get-Command java -CommandType Application | Select-Object -First 1).Source
$javacPath = (Get-Command javac -CommandType Application | Select-Object -First 1).Source
$runLogPath = Join-Path $absoluteRun 'runner.log'
Push-Location -LiteralPath $screenshotProjectRoot
try {
    $snapshots = @(Get-ChildItem -LiteralPath 'src','resources' -Recurse -File | ForEach-Object {
        [PSCustomObject]@{ Path = $_.FullName.Substring($screenshotProjectRoot.Length + 1); SHA256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash }
    })
    $snapshots | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath (Join-Path $absoluteRun 'source-sha256.json') -Encoding UTF8
    @('DATE_ASIA_SAIGON: ' + [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(7)).ToString('o'),
      'JAVA: ' + $javaPath, 'JAVAC: ' + $javacPath,
      'No uiScale override, no product source edits, only one capture JVM launch.') |
        Set-Content -LiteralPath $runLogPath -Encoding UTF8
    $sources = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName.Substring($screenshotProjectRoot.Length + 1) })
    $compileArguments = @('-encoding','UTF-8','-d',$relativeClasses) + $sources + @('tools/screenshots/CaptureScreenshots.java')
    Add-Content -LiteralPath $runLogPath -Encoding UTF8 -Value ('COMPILE: javac ' + ($compileArguments -join ' '))
    & $javacPath @compileArguments
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed; screenshot tool was not launched.' }
    $javaArguments = @('-Djava.awt.headless=false','-Dfile.encoding=UTF-8','-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8',
        '-Djdk.net.hosts.file=tools/screenshots/loopback.hosts', '-cp', ($relativeClasses + ';resources'),
        'CaptureScreenshots', $relativeOutput, $relativeRun)
    # Start-Process receives one Windows argument string; quote every argument that might contain spaces.
    $argumentString = ($javaArguments | ForEach-Object { '"' + $_ + '"' }) -join ' '
    Add-Content -LiteralPath $runLogPath -Encoding UTF8 -Value ('CAPTURE_COMMAND: java ' + $argumentString)
    $process = Start-Process -FilePath $javaPath -ArgumentList $argumentString -WorkingDirectory $screenshotProjectRoot `
        -WindowStyle Hidden -PassThru -Wait -RedirectStandardOutput (Join-Path $absoluteRun 'capture.log') `
        -RedirectStandardError (Join-Path $absoluteRun 'capture.stderr.log')
    Add-Content -LiteralPath $runLogPath -Encoding UTF8 -Value ('CAPTURE_EXIT_CODE: ' + $process.ExitCode)
    foreach ($snapshot in $snapshots) {
        $currentHash = (Get-FileHash -LiteralPath $snapshot.Path -Algorithm SHA256).Hash
        if ($currentHash -ne $snapshot.SHA256) { throw ('Product/resource changed: ' + $snapshot.Path) }
    }
    Add-Content -LiteralPath $runLogPath -Encoding UTF8 -Value 'SOURCE_AND_RESOURCE_HASHES: unchanged'
    Write-Output ('Evidence: ' + $relativeRun)
    Write-Output ('Images: ' + $relativeOutput)
    Get-Content -LiteralPath (Join-Path $absoluteRun 'capture.log') -Encoding UTF8
    if ($process.ExitCode -ne 0) {
        Get-Content -LiteralPath (Join-Path $absoluteRun 'capture.stderr.log') -Encoding UTF8
        throw ('Capture tool exit code ' + $process.ExitCode + '; no rerun attempted.')
    }
} finally { Pop-Location }
