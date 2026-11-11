@echo off
title Naukri Daily Job Automation Runner (Java)
cd /d "%~dp0"

echo ===================================================
echo Starting Daily Naukri Job Automation (Java)
echo ===================================================

if not exist ".env" (
    echo [ERROR] .env file not found! Copy .env.example to .env and configure credentials.
    pause
    exit /b 1
)

if not exist "target\naukri-automation.jar" (
    echo [INFO] Building executable JAR...
    call mvn clean package -DskipTests
)

java -jar target\naukri-automation.jar

echo.
echo Execution completed.
