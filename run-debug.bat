@echo off
chcp 65001 > nul
echo [DEBUG] Dang bien dich Game Gomoku...
javac -encoding UTF-8 -d bin src/game/model/*.java src/game/ai/*.java src/game/network/*.java src/game/view/*.java src/game/*.java
if %ERRORLEVEL% neq 0 (
    echo Co loi khi bien dich!
    pause
    exit /b 1
)
if exist resources xcopy resources bin /E /I /Y > nul
if "%~1"=="" (
    echo [DEBUG] Chay voi scale mac dinh cua he thong
    java -cp bin game.Main
) else (
    echo [DEBUG] Chay voi scale %~1
    java -Dsun.java2d.uiScale=%~1 -cp bin game.Main
)
echo.
echo [DEBUG] Game da thoat. Ma thoat: %ERRORLEVEL%
pause
