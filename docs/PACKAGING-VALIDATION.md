# Windows release verification — 2026-11-02

Release: `dist/20261102-230321-528/Gomoku-Windows-x64.zip` (approximately 26 MiB).

SHA-256: `33956D77C0EB8E77840CBF165ACDEE26D66CC043DB62532832B0AC27E3578BB6`.

Verified by extracting the final ZIP into a fresh directory, including the
Vietnamese project path, and launching its `Gomoku.exe`:

- Title screen and menu open successfully.
- Start an offline AI game, place a stone, and receive an AI response.
- Click **Ván Mới** and verify that the board is cleared.
- Player win counters and the draw counter appear above the board.
- The archive includes instructions, the runtime, and runtime license files.
- The application JAR excludes development test/benchmark classes and JUnit.
- The recorded ZIP checksum matches the archive.

The extracted release JAR was also run using its bundled Java runtime, with
command-line runtime binaries copied to an ASCII-only path for their unmodified
vendor launchers. `FullGomokuTestSuite` passed **77/77 assertions across 54
scenarios**. Its three pre-existing pending scenarios remain pending; they are
not counted as passes. Output is in
`build/package-verification/20261102-230321-528/rules-test.log`.

This release's LAN mode was not manually exercised between two physical machines.
Packaging script syntax and `git diff --check` passed. No gameplay source was
changed for this package.
