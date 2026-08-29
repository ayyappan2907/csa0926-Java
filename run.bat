@echo off
echo ==========================================
echo   Factory Auto Chain - Automation Game
echo ==========================================
echo.
echo Compiling...
javac -cp "lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-nop.jar" -d out -sourcepath src src/main/Main.java
if %ERRORLEVEL% neq 0 (
    echo COMPILATION FAILED!
    pause
    exit /b 1
)
echo Compilation successful!
echo.
echo Starting Factory Auto Chain...
java -cp "out;lib/sqlite-jdbc.jar;lib/slf4j-api.jar;lib/slf4j-nop.jar" main.Main
pause
