param([switch] $FinalReview)
$ErrorActionPreference = 'Stop'
$soundRepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$soundOriginalTestRoot = Join-Path $soundRepoRoot 'test'
$originalScript = Join-Path $soundOriginalTestRoot 'run-headless-2026-11-03.ps1'
$originalHash = (Get-FileHash -LiteralPath $originalScript -Algorithm SHA256).Hash
$scriptText = [IO.File]::ReadAllText($originalScript)
$buildLabel = if ($FinalReview) { 'review-after-fix-sound-final' } else { 'review-after-fix-sound' }
$logName = if ($FinalReview) { 'docs/junit-run-after-fix-sound-final.log' } else { 'docs/junit-run-after-fix-sound.log' }
$commandSuffix = if ($FinalReview) { ' -FinalReview' } else { '' }
# -FinalReview is reserved for an explicitly authorized additional full-suite run.
if (Test-Path -LiteralPath (Join-Path $soundRepoRoot ('test/' + $buildLabel))) {
    throw 'After-fix build directory already exists; refusing to reuse outputs or rerun.'
}
# Execute the same script, keeping the original file and all selectors unchanged.
# Only bind its script-root context and redirect log/build/XML destinations in memory.
$scriptText = $scriptText.Replace('$PSScriptRoot', '$soundOriginalTestRoot')
$scriptText = $scriptText.Replace('docs/junit-run-2026-11-03.log', $logName)
$scriptText = $scriptText.Replace('review-2026-11-03', $buildLabel)
$scriptText = $scriptText.Replace(
    "Write-RunLog 'ENTRY_COMMAND: powershell -NoProfile -ExecutionPolicy Bypass -File test/run-headless-2026-11-03.ps1'",
    "Write-RunLog 'ENTRY_COMMAND: powershell -NoProfile -ExecutionPolicy Bypass -File tools/sound/run-headless-after-fix.ps1$commandSuffix'" + [Environment]::NewLine +
    "    Write-RunLog 'ORIGINAL_SCRIPT: test/run-headless-2026-11-03.ps1; unchanged SHA256=$originalHash; same 158-case selectors'" + [Environment]::NewLine +
    "    Write-RunLog 'ADAPTATION: script-root binding and new log/build/XML destinations only; no test source or selector changes'"
)
if ((Get-FileHash -LiteralPath $originalScript -Algorithm SHA256).Hash -ne $originalHash) {
    throw 'Original test runner changed unexpectedly.'
}
Invoke-Expression $scriptText
