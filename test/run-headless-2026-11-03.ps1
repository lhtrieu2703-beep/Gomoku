$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$runRoot = Join-Path $PSScriptRoot 'review-2026-11-03'
$logPath = Join-Path $projectRoot 'docs/junit-run-2026-11-03.log'
if (Test-Path -LiteralPath $logPath) {
    if ([System.IO.File]::ReadAllText($logPath).Contains('TEST_EXECUTION_NUMBER:')) {
        throw 'JUnit execution was already attempted. Refusing to execute the suite a second time.'
    }
    if (-not [System.IO.File]::ReadAllText($logPath).Contains('RUNNER_ERROR:')) {
        throw 'An existing log has no confirmed bootstrap failure. Refusing to overwrite evidence.'
    }
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Write-RunLog([string] $message) {
    [System.IO.File]::AppendAllText($logPath, $message + [Environment]::NewLine, $utf8)
}
function Invoke-LoggedCommand([string] $executable, [string[]] $commandArguments) {
    # All current arguments are relative paths or Java/JUnit options. No shell is used.
    foreach ($argument in $commandArguments) {
        if ($argument.Contains('"') -or $argument.EndsWith('\')) {
            throw 'Unexpected quote or trailing backslash in a command argument.'
        }
    }
    $quotedArguments = @($commandArguments | ForEach-Object { '"' + $_ + '"' })
    Write-RunLog ('COMMAND: ' + $executable + ' ' + ($quotedArguments -join ' '))
    $start = New-Object System.Diagnostics.ProcessStartInfo
    $start.FileName = $executable
    $start.Arguments = $quotedArguments -join ' '
    $start.WorkingDirectory = $projectRoot
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.StandardOutputEncoding = $utf8
    $start.StandardErrorEncoding = $utf8
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $start
    try {
        [void] $process.Start()
        $stdout = $process.StandardOutput.ReadToEndAsync()
        $stderr = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        Write-RunLog 'STDOUT:'
        Write-RunLog ($stdout.GetAwaiter().GetResult())
        Write-RunLog 'STDERR:'
        Write-RunLog ($stderr.GetAwaiter().GetResult())
        $code = $process.ExitCode
        Write-RunLog ('EXIT_CODE: ' + $code)
        return $code
    } finally {
        $process.Dispose()
    }
}

Push-Location -LiteralPath $projectRoot
try {
    Write-RunLog 'ENTRY_COMMAND: powershell -NoProfile -ExecutionPolicy Bypass -File test/run-headless-2026-11-03.ps1'
    Write-RunLog ('START_TIME_ASIA_SAIGON: ' + [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(7)).ToString('o'))
    Write-RunLog ('WORKING_DIRECTORY: ' + $projectRoot)
    Write-RunLog 'SCOPE: all existing and added pure-logic JUnit cases for Board, ScoreTracker, MinimaxAI, HeuristicEvaluator, and socket-free NetworkManager messages.'
    Write-RunLog 'EXCLUSIONS: Swing/GUI hooks and methods, real sockets, and classes outside the five requested targets. See INVENTORY.md and REPORT.md beside the test output.'
    Write-RunLog 'BUILD: javac commands from build.ps1; only -d destinations relocated under test/ to respect the write constraint. All src/ and all test/ Java sources are compiled.'
    Write-RunLog 'JUNIT: bundled junit-platform-console-standalone-1.10.2.jar; manifest Engine-Version-junit-jupiter=5.10.2.'
    $java = (Get-Command java -CommandType Application | Select-Object -First 1).Source
    $javac = (Get-Command javac -CommandType Application | Select-Object -First 1).Source
    if ((Invoke-LoggedCommand $java @('-version')) -ne 0) { throw 'java -version failed' }
    $sourceOutput = 'test/review-2026-11-03/classes'
    $testOutput = 'test/review-2026-11-03/test-classes'
    New-Item -ItemType Directory -Path $sourceOutput, $testOutput -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' |
        ForEach-Object { $_.FullName.Substring($projectRoot.Length + 1) })
    if ((Invoke-LoggedCommand $javac (@('-encoding', 'UTF-8', '-d', $sourceOutput) + $sources)) -ne 0) {
        throw 'Product compilation failed; no test execution attempted.'
    }
    Get-ChildItem -LiteralPath 'resources' | Copy-Item -Destination $sourceOutput -Recurse -Force
    $tests = @(Get-ChildItem -LiteralPath 'test' -Recurse -Filter '*.java' |
        ForEach-Object { $_.FullName.Substring($projectRoot.Length + 1) })
    if ((Invoke-LoggedCommand $javac (@('-encoding', 'UTF-8', '-cp', ('lib/*;' + $sourceOutput), '-d', $testOutput) + $tests)) -ne 0) {
        throw 'Test compilation failed; no test execution attempted.'
    }
    $junitArguments = @(
        '-Djava.awt.headless=true', '-Dfile.encoding=UTF-8', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
        '-jar', 'lib/junit-platform-console-standalone-1.10.2.jar', 'execute',
        '--class-path', ($testOutput + ';' + $sourceOutput),
        '--select-class', 'game.BoardWinTest',
        '--select-class', 'game.F3HeuristicTest',
        '--select-class', 'game.ai.AITacticsTest',
        '--select-method', 'game.Task4bGameplayTest#resettingNumbersDoesNotAllowUndoToRecordAnOldResultAgain',
        '--select-class', 'game.model.BoardModelContractTest',
        '--select-class', 'game.model.ScoreTrackerContractTest',
        '--select-class', 'game.ai.MinimaxEasyContractTest',
        '--select-class', 'game.ai.HeuristicOrderingTest',
        '--select-class', 'game.network.NetworkMessageTest',
        '--config', 'junit.jupiter.execution.parallel.enabled=false',
        '--reports-dir', 'test/review-2026-11-03/xml',
        '--details', 'tree', '--disable-banner', '--disable-ansi-colors', '--fail-if-no-tests'
    )
    Write-RunLog 'TEST_EXECUTION_NUMBER: 1 (only JUnit execute invocation in this script)'
    $junitExitCode = Invoke-LoggedCommand $java $junitArguments
    Write-RunLog ('END_TIME_ASIA_SAIGON: ' + [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(7)).ToString('o'))
    Write-RunLog 'XML_REPORT_DIRECTORY: test/review-2026-11-03/xml'
    Write-RunLog 'FAILURES_POLICY: retain failing tests; do not modify production or rerun.'
    Write-Output ('JUnit exit code: ' + $junitExitCode)
    Write-Output ('Full log: ' + $logPath)
    exit $junitExitCode
} catch {
    Write-RunLog ('RUNNER_ERROR: ' + $_.Exception.ToString())
    throw
} finally {
    Pop-Location
}
