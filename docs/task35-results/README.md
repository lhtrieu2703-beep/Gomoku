# Task 3.5 final evidence — 2026-10-27

Clean build: removed only the verified workspace bin and bin_test directories,
compiled every Java file under src, test and tools/audit with javac UTF-8.
Java runtime: OpenJDK 25.0.3. Resources are read from src on the classpath.

All .txt files are unedited stdout/stderr captured with PowerShell Tee-Object.
Explicit UTF-8 Java arguments must be quoted in PowerShell:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [Console]::OutputEncoding
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path 'bin_test;bin;src' --scan-class-path --details summary --disable-banner --disable-ansi-colors
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp 'bin;src' game.RuleAndDrawTest
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp 'bin;src' game.FullGomokuTestSuite
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp 'bin_test;bin;src' CurrentAuditProbe
```

- JUnit: 175/175, zero skipped/failed. Registry and localhost socket tests ran
  outside the sandbox with permission, without a PreferencesFactory override.
- Production /game/view key/value snapshot remained {soundEnabled=true}; language
  remained absent. UUID test nodes were removed and cleanup assertions passed.
  Other frame tests/probes inject in-memory Preferences.
- RuleAndDrawTest: 17/17.
- FullGomokuTestSuite: 57 declared scenarios = 54 executed + 3 PENDING;
  77/77 executed assertions. T55 resign and T56 clock have no product feature.
  T57 has no console integration scenario; F2 ordering is covered separately
  by JUnit. PENDING is excluded from PASS.
- Probe: old MOVE rejected after reset, fifth MOVE sent before result callback,
  blocked six 0 < FIVE 100000000, single three 128300 < DOUBLE_THREE 2000000.
  No OUTSIDE_PARENT. F3 tests also cover actual double three in both rules/colors.
- benchmark.txt is one measurement run, not a standardized performance claim.
- *-source.patch contains the source/test/tool delta for that commit (F5 is the
  final validation delta). Documentation, screenshots and logs are omitted from
  these source patches; git show on the local commit provides the complete diff.

Renders: ../task35-render/. Protocol sequence diagram: ../TASK35_PROTOCOL.md.
Manual checks remaining: LAN on two machines/firewall, physical audio, Windows
display scaling/DPI. No plain Minimax or tactical toggle was added.
