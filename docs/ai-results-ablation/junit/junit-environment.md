# JUnit ablation run

- Timestamp (Asia/Saigon): 2026-11-24T11:59:27.7485248+07:00
- Revision: 472e533003fda9682a51cf2ae2c1ce0a42226a78 (working tree changes in src/test/tools/ai)
- External copy: C:\Users\Acer\AppData\Local\Temp\gomoku-ablation-junit-71d793e196064cc8918b03cf00fec7ef
- Entire src/test/resources/lib plus unchanged build.ps1 copied before compiling.
- Full classpath scan, no excluded tests/classes/packages. UI tests run with headless=false.
- Existing render tests write only inside the temporary copy; historical evidence in the repository is untouched.
- JUnit Platform Console 1.10.2 / Jupiter 5.10.2 from bundled lib.
- This is a NEW execution milestone; do not add its counts to the 531 runs from 12/11/2026.
- Command (inside the copy): java -Djava.awt.headless=false -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin_test;bin" --scan-class-path --config junit.jupiter.execution.parallel.enabled=false --reports-dir junit-xml --details tree --disable-banner --disable-ansi-colors --fail-if-no-tests
- Exit code: 0
