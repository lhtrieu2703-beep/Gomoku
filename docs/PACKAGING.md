# Windows portable release

Run from PowerShell with a full Windows x64 JDK 25 or newer on PATH and the
Windows SDK Manifest Tool (`mt.exe`) installed:

```powershell
.\package-windows.ps1
```

An optional version can be supplied with `-Version 1.0.1`.

Each run creates an independent build directory under `build/package-windows/`
and a release directory under `dist/<timestamp>/` containing:

- `Gomoku/`: the portable app, including `Gomoku.exe`, `app/`, `runtime/`, and Vietnamese instructions.
- `Gomoku-Windows-x64.zip`: send this complete ZIP to testers.
- `Gomoku-Windows-x64.zip.sha256`: SHA-256 checksum of the ZIP.

The recipient extracts the ZIP and launches `Gomoku/Gomoku.exe`. The recipient
does not need Java, a JDK, admin installation, or the source code. Keep the whole
application directory together. This is a portable app with an EXE launcher.

The script compiles `game.Main` and its dependencies into a fresh directory,
copies classpath resources, packages the JAR, discovers Java module dependencies
with `jdeps`, and creates a runtime with `jlink`. `jpackage --type app-image`
creates the Windows launcher without requiring installer tooling. The SDK Manifest
Tool merges a per-process UTF-8 setting into the existing EXE manifest so the app
also launches from Vietnamese folder paths. This setting requires Windows 10
1903 or newer, or Windows 11; it does not change system locale settings.
See [Microsoft's UTF-8 manifest documentation](https://learn.microsoft.com/en-us/windows/apps/design/globalizing/use-utf8-code-page).
Runtime license files are retained under `runtime/legal`. The runtime includes its Java commands
so the packaged JAR can also be diagnosed with the bundled `runtime/bin/java.exe`.
For command-line diagnostics, use an ASCII-only extraction path: the original
Java command launchers retain their vendor binaries and manifests. The game's
`Gomoku.exe` supports Unicode paths.

Build output does not include source code, JUnit, test results, saved player
profiles, or developer settings. User preferences are stored by the running
application on each recipient's computer. No installer or signing certificate
is created by this script.
