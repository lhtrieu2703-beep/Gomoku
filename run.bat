@echo off
chcp 65001 > nul
echo Dang bien dich va khoi dong Game Co Caro AI...
javac -encoding UTF-8 -d bin src/game/model/*.java src/game/ai/*.java src/game/view/*.java src/game/*.java
if %ERRORLEVEL% equ 0 (
    start javaw -cp bin game.Main
) else (
    echo Co loi khi bien dich!
    pause
)
