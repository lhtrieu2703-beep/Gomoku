param([string]$OutputDirectory = '')
$ErrorActionPreference = 'Stop'
$ablationTestRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) { $OutputDirectory = Join-Path $ablationTestRoot 'docs/ai-results-ablation/junit' }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
if (Test-Path -LiteralPath $OutputDirectory) { throw 'JUnit output directory already exists; choose a new directory' }
New-Item -ItemType Directory -Path $OutputDirectory | Out-Null
$ablationTestCopy = Join-Path ([IO.Path]::GetTempPath()) ('gomoku-ablation-junit-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $ablationTestCopy | Out-Null
foreach ($entry in @('src','test','resources','lib','build.ps1')) {
    Copy-Item -LiteralPath (Join-Path $ablationTestRoot $entry) -Destination $ablationTestCopy -Recurse
}
$ablationTestRevision = & git -C $ablationTestRoot rev-parse HEAD
if ($LASTEXITCODE -ne 0) { throw 'Cannot identify JUnit source revision' }
$ablationTestDirty = @(& git -C $ablationTestRoot status --porcelain -- src test tools/ai)
if ($ablationTestDirty.Count -gt 0) { $ablationTestRevision += ' (working tree changes in src/test/tools/ai)' }
$ablationTestDate = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(7)).ToString('o')
$ablationTestLog = Join-Path $OutputDirectory 'junit-run.log'
$ablationBuildLog = Join-Path $OutputDirectory 'build.log'
$ablationTestArgs = @('-Djava.awt.headless=false','-Dfile.encoding=UTF-8','-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8',
    '-jar','lib/junit-platform-console-standalone-1.10.2.jar','execute','--class-path','bin_test;bin',
    '--scan-class-path','--config','junit.jupiter.execution.parallel.enabled=false',
    '--reports-dir','junit-xml','--details','tree','--disable-banner','--disable-ansi-colors','--fail-if-no-tests')
Push-Location -LiteralPath $ablationTestCopy
try {
    & ./build.ps1 *> $ablationBuildLog
    if ($LASTEXITCODE -ne 0) { throw 'JUnit build failed' }
    & java @ablationTestArgs *> $ablationTestLog
    $ablationTestExit = $LASTEXITCODE
    Copy-Item -LiteralPath (Join-Path $ablationTestCopy 'junit-xml') -Destination $OutputDirectory -Recurse
} finally { Pop-Location }
$ablationTestText = [IO.File]::ReadAllText($ablationTestLog)
$ablationTestCounts = [ordered]@{}
foreach ($label in @('found','skipped','started','aborted','successful','failed')) {
    $match = [regex]::Match($ablationTestText, '\[\s*(\d+) tests ' + $label + '\s*\]')
    if (-not $match.Success) { throw "JUnit footer missing tests $label" }
    $ablationTestCounts[$label] = [int]$match.Groups[1].Value
}
$ablationTestResult = [ordered]@{
    timestamp_asia_saigon = $ablationTestDate
    revision = $ablationTestRevision
    exit_code = $ablationTestExit
    scope = 'Full recursive build.ps1; JUnit --scan-class-path; no exclusions; sequential; headless=false; external copy'
    working_copy = $ablationTestCopy
    counts = $ablationTestCounts
}
$ablationTestResult | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $OutputDirectory 'junit-results.json') -Encoding utf8
$ablationTestEnvironment = @(
    '# JUnit ablation run', '', "- Timestamp (Asia/Saigon): $ablationTestDate", "- Revision: $ablationTestRevision",
    "- External copy: $ablationTestCopy", '- Entire src/test/resources/lib plus unchanged build.ps1 copied before compiling.',
    '- Full classpath scan, no excluded tests/classes/packages. UI tests run with headless=false.',
    '- Existing render tests write only inside the temporary copy; historical evidence in the repository is untouched.',
    '- JUnit Platform Console 1.10.2 / Jupiter 5.10.2 from bundled lib.',
    '- This is a NEW execution milestone; do not add its counts to the 531 runs from 12/11/2026.',
    '- Command (inside the copy): java ' + ($ablationTestArgs -join ' '),
    "- Exit code: $ablationTestExit"
)
$ablationTestEnvironment | Set-Content -LiteralPath (Join-Path $OutputDirectory 'junit-environment.md') -Encoding utf8
Get-Content -LiteralPath $ablationTestLog -Tail 18
Write-Output "JUnit evidence: $OutputDirectory"
if ($ablationTestExit -ne 0) { exit $ablationTestExit }
