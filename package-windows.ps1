param(
    [ValidatePattern('^\d+\.\d+\.\d+$')]
    [string]$Version = '1.0.0'
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
Push-Location -LiteralPath $projectRoot
try {
$jdkRoot = Split-Path (Split-Path (Get-Command javac.exe -ErrorAction Stop).Source)
foreach ($tool in @('javac', 'jar', 'jdeps', 'jlink', 'jpackage')) {
    if (-not (Test-Path -LiteralPath (Join-Path $jdkRoot "bin/$tool.exe"))) {
        throw "Missing $tool.exe. Install a full Windows x64 JDK 25 or newer."
    }
}
$jdkRelease = Get-Content -LiteralPath (Join-Path $jdkRoot 'release') -Raw
if ($jdkRelease -notmatch 'OS_NAME="Windows"' -or $jdkRelease -notmatch 'OS_ARCH="(amd64|x86_64)"') {
    throw 'This script creates a Windows x64 package and requires a Windows x64 JDK.'
}
$manifestTool = Get-Command mt.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -First 1
if (-not $manifestTool) {
    $sdkBin = Join-Path ${env:ProgramFiles(x86)} 'Windows Kits/10/bin'
    $manifestTool = Get-ChildItem -LiteralPath $sdkBin -Directory -Force -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName 'x64/mt.exe' } |
        Where-Object { Test-Path -LiteralPath $_ } |
        Select-Object -First 1
}
if (-not $manifestTool) {
    throw 'Windows SDK mt.exe is required to enable Vietnamese/Unicode folder paths in the launcher.'
}

# Unique directories keep earlier packages and local development builds intact.
$buildId = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$workRoot = "build/package-windows/$buildId"
$classes = Join-Path $workRoot 'classes'
$inputDir = Join-Path $workRoot 'input'
$runtime = Join-Path $workRoot 'runtime'
$outputDir = "dist/$buildId"
New-Item -ItemType Directory -Path $classes, $inputDir, $outputDir | Out-Null

Write-Host 'Compiling game and copying resources...'
# Compile the entry point and its dependencies; exclude test/benchmark programs.
# Relative native-tool arguments also support project paths with Vietnamese text.
& "$jdkRoot/bin/javac.exe" -encoding UTF-8 -d $classes -sourcepath src src/game/Main.java
if ($LASTEXITCODE -ne 0) { throw 'Game compilation failed.' }
Get-ChildItem -LiteralPath (Join-Path $projectRoot 'resources') | Copy-Item -Destination $classes -Recurse -Force
$jarPath = Join-Path $inputDir 'gomoku.jar'
& "$jdkRoot/bin/jar.exe" --create --file $jarPath --main-class game.Main -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed.' }

Write-Host 'Creating bundled Java runtime...'
$modules = & "$jdkRoot/bin/jdeps.exe" --print-module-deps $jarPath
if ($LASTEXITCODE -ne 0) { throw 'Java module analysis failed.' }
$modules = ($modules -join '').Trim()
& "$jdkRoot/bin/jlink.exe" --add-modules $modules --strip-debug --no-header-files --no-man-pages --output $runtime
if ($LASTEXITCODE -ne 0) { throw 'Java runtime creation failed.' }

Write-Host 'Creating Gomoku.exe...'
& "$jdkRoot/bin/jpackage.exe" --type app-image --name Gomoku --app-version $Version --description 'Gomoku desktop game with AI, local two-player and LAN modes' --input $inputDir --main-jar gomoku.jar --main-class game.Main --runtime-image $runtime --java-options '--enable-native-access=ALL-UNNAMED' --dest $outputDir --temp (Join-Path $workRoot 'jpackage-temp')
if ($LASTEXITCODE -ne 0) { throw 'Windows application packaging failed.' }

$appDir = Join-Path $outputDir 'Gomoku'
# Opt in to UTF-8 per process; keep the original DPI and privilege manifest.
# Without this, the Java VM cannot load from Vietnamese paths on some Windows locales.
(Get-Item -LiteralPath (Join-Path $appDir 'Gomoku.exe')).IsReadOnly = $false
& $manifestTool -nologo "-inputresource:$appDir/Gomoku.exe;#1" -manifest tools/windows-utf8.manifest "-outputresource:$appDir/Gomoku.exe;#1"
if ($LASTEXITCODE -ne 0) { throw 'Launcher UTF-8 manifest merge failed.' }
Copy-Item -LiteralPath (Join-Path $projectRoot 'docs/PLAY-WINDOWS.txt') -Destination (Join-Path $appDir 'HUONG-DAN.txt')
$zipPath = Join-Path $outputDir 'Gomoku-Windows-x64.zip'
Write-Host 'Creating shareable ZIP...'
Compress-Archive -LiteralPath $appDir -DestinationPath $zipPath -CompressionLevel Optimal
Get-FileHash -LiteralPath $zipPath -Algorithm SHA256 | ForEach-Object {
    "$($_.Hash)  Gomoku-Windows-x64.zip" | Set-Content -LiteralPath "$zipPath.sha256" -Encoding ascii
}
Write-Host "Ready to share: $(Join-Path $projectRoot $zipPath)"
Write-Host "Local launcher: $(Join-Path $projectRoot (Join-Path $appDir 'Gomoku.exe'))"
} finally {
    Pop-Location
}
