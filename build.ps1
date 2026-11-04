$ErrorActionPreference = 'Stop'
$buildRootPath = (Get-Location).Path
$sources = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName.Substring($buildRootPath.Length + 1) })
if (-not (Test-Path -LiteralPath 'bin')) { New-Item -ItemType Directory -Path 'bin' | Out-Null }
& javac -encoding UTF-8 -d bin @sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
if (Test-Path -LiteralPath 'resources') {
    Get-ChildItem -LiteralPath 'resources' | Copy-Item -Destination 'bin' -Recurse -Force
}
if (Test-Path -LiteralPath 'test') {
    $tests = @(Get-ChildItem -LiteralPath 'test' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName.Substring($buildRootPath.Length + 1) })
    & javac -encoding UTF-8 -cp 'lib/*;bin' -d bin_test @tests
    if ($LASTEXITCODE -ne 0) { throw 'test javac failed' }
}
