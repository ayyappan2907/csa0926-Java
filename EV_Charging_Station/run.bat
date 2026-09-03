@echo off
title EV Charging Station System - SIMATS Engineering
echo ========================================================
echo   EV Charging Station Slot Booking and Billing System
echo   SIMATS Engineering - CSA0926 Java Programming
echo ========================================================
echo.
echo [1/2] Compiling Java source files...
if not exist out mkdir out
javac -d out -sourcepath src src/evcharging/app/EVChargingApp.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo [OK] Compilation successful!
echo.
echo [2/2] Launching Application GUI...
java -cp out evcharging.app.EVChargingApp
pause
