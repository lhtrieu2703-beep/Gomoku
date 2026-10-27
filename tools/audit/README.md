# Audit probes

Run from the repository root with compiled production classes in bin:

```powershell
javac -encoding UTF-8 -cp bin -d bin_test tools/audit/CurrentAuditProbe.java tools/audit/AuditBenchmark.java
java '-Dstdout.encoding=UTF-8' -cp 'bin_test;bin' CurrentAuditProbe
java '-Dstdout.encoding=UTF-8' -cp 'bin_test;bin' AuditBenchmark
```

CurrentAuditProbe uses localhost sockets, EDT callbacks, in-memory Preferences
and offscreen rendering of Swing content using the system Look & Feel.
It prints observed behavior rather than asserting that the unresolved bugs pass.
Images are written to bin_test. No app Preferences are changed.

AuditBenchmark uses fixed seeds, 3 warmups and 15 measured searches per
configuration; then measures 10 Hard turns through BoardPanel/SwingWorker.
Its EDT heartbeat does not represent a full visible-window rendering benchmark.
Sound is disabled. It checks legal returned moves and unchanged move counts.

Production behavior is not changed by these tools. They are excluded from
the normal test source compilation and do not increase the JUnit test count.
