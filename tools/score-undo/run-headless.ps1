$ErrorActionPreference = 'Stop'
$scoreRepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$scoreTestRoot = Join-Path $scoreRepoRoot 'test'
$original = Join-Path $scoreTestRoot 'run-headless-2026-11-03.ps1'
$text = [IO.File]::ReadAllText($original)
if (Test-Path -LiteralPath (Join-Path $scoreRepoRoot 'test/review-after-fix-score-undo')) {
    throw 'Final output already exists; refusing to rerun.'
}
$text = $text.Replace('$PSScriptRoot', '$scoreTestRoot')
$text = $text.Replace('docs/junit-run-2026-11-03.log', 'docs/junit-run-after-fix-score-undo.log')
$text = $text.Replace('review-2026-11-03', 'review-after-fix-score-undo')
$text = $text.Replace('SCOPE: all existing and added pure-logic JUnit cases for Board, ScoreTracker, MinimaxAI, HeuristicEvaluator, and socket-free NetworkManager messages.',
    'SCOPE: original 158 pure-logic cases plus UndoScoreTest, UndoParityTest, and ScoreTrackerRevokeTest; all run headless.')
$text = $text.Replace('EXCLUSIONS: Swing/GUI hooks and methods, real sockets, and classes outside the five requested targets. See INVENTORY.md and REPORT.md beside the test output.',
    'EXCLUSIONS: original excluded cases remain excluded except the two explicitly authorized BoardPanel fixture classes; no JFrame or real socket tests selected. See docs/SCORE_UNDO_FIX_2026-11-03.md.')
$text = $text.Replace("'--config', 'junit.jupiter.execution.parallel.enabled=false',", @"
'--select-class', 'game.UndoScoreTest',
        '--select-class', 'game.UndoParityTest',
        '--select-class', 'game.model.ScoreTrackerRevokeTest',
        '--config', 'junit.jupiter.execution.parallel.enabled=false',
"@)
$text = $text.Replace("Write-RunLog 'ENTRY_COMMAND: powershell -NoProfile -ExecutionPolicy Bypass -File test/run-headless-2026-11-03.ps1'",
    "Write-RunLog 'ENTRY_COMMAND: powershell -NoProfile -ExecutionPolicy Bypass -File tools/score-undo/run-headless.ps1'" + [Environment]::NewLine +
    "    Write-RunLog 'SCOPE_EXTENSION: baseline 158 + 17 existing offline Swing-component fixture tests (no JFrame/socket) + 15 new pure ScoreTracker invocations; unchanged original runner file'")
Invoke-Expression $text
