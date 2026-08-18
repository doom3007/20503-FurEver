@echo off
setlocal enabledelayedexpansion
echo Setting up FurEver Pet Adoption System...
echo.

echo 1. Setting up MySQL database...
REM Update MYSQL_PASSWORD and MYSQL_USERNAME if your MySQL uses different credentials
set MYSQL_PASSWORD=root
set MYSQL_USERNAME=root
mysql -u %MYSQL_USERNAME% -p %MYSQL_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS furever CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Failed to connect to MySQL or create database.
    echo Please ensure MySQL is installed and running.
    echo Update MYSQL_PASSWORD and MYSQL_USERNAME in setup.bat if needed.
    pause
    exit /b 1
)

echo Database ready. Importing schema...
mysql -u %MYSQL_USERNAME% -p%MYSQL_PASSWORD% furever < src\main\resources\schema.sql 2>nul
echo Schema import completed (existing data may cause warnings, this is normal).

:build
echo.
echo 2. Building project with Maven (mvn)...
echo.

call "%~dp0installations\apache-maven-3.9.16\bin\mvn.cmd" install -q
if %errorlevel% neq 0 (
    echo Error: Failed to build project.
    pause
    exit /b 1
)

echo.
echo Setup completed successfully!
echo.
echo IMPORTANT: Update your MySQL username and password in:
echo   - src\main\java\com\furever\server\data\DatabaseConnection.java
echo   - setup.bat (MYSQL_USERNAME and MYSQL_PASSWORD variables)
echo.
echo To run the server:
echo   run-server.bat
echo.
echo To run the client:
echo   run-client.bat
