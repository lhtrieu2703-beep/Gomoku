# Task 4 — final validation

Base: main 6c0b4c8, containing Task 3.5 through 3f07d02. Work is local on
task4-start-screen. No network protocol/rule/AI implementation changes.

Baseline logs are baseline-*.txt: JUnit 175/175, rules 17/17, console 77/77
assertions, 54 executed scenarios and 3 PENDING. Final logs are junit-tree.txt,
rule-and-draw.txt and full-suite.txt. These are unedited stdout/stderr captured
with PowerShell Tee-Object, including JUnit's own display-name abbreviation.

Final clean build removed only verified workspace bin/bin_test, then rebuilt
all Java files: 21 src, 19 test and 2 tools/audit. OpenJDK 25.0.3, UTF-8.
Resource classpath includes src. Final JUnit: 211/211, zero skipped/failed.
The 36 new invocations are 17 StartScreen/name, 3 Preferences, 9 lifecycle,
6 layout and 1 queued placement sound regression. The existing 175 remain.

Registry/socket tests ran outside the sandbox with permission. Real Preferences
tests used UUID nodes under /test/task4 and /test/task3, removed them with
removeNode and verified production /game/view against the before snapshot:
{soundEnabled=true}. Neither playerName nor language was added to production.
Other frame/render tests use in-memory Preferences. No PreferencesFactory bypass.

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [Console]::OutputEncoding
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path 'bin_test;bin;src' --scan-class-path --details tree --disable-banner --disable-ansi-colors
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp 'bin;src' game.RuleAndDrawTest
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp 'bin;src' game.FullGomokuTestSuite
```

Renders: ../task4-render/start/ and ../task4-render/game/. Rendering applies the
window's default DPI transform so text measurements match Swing layout. Bounds
cover both cards, all three locales, direct startup and transitions on Metal
and the Windows Look & Feel. These do not replace manual display scaling tests.

Full console suite: 57 declared = 54 executed + 3 PENDING; 77 assertions pass.
T55 resign, T56 clock and T57 console integration remain PENDING as in Task 3.5.
No PENDING is counted as PASS. The source patch excludes docs/screenshots/logs;
git show on the local commit provides the complete diff.

Decisions/manual checklist and card diagram: ../TASK4_START_SCREEN.md.
