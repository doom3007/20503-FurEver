@echo off
setlocal enabledelayedexpansion
echo Setting up FurEver Pet Adoption System...
echo.

echo 1. Setting up MySQL database...
REM Update MYSQL_PASSWORD and MYSQL_USERNAME if your MySQL uses different credentials
set MYSQL_PASSWORD=root
set MYSQL_USERNAME=root

where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: MySQL was not found in PATH!
    echo Do you have MySQL installed? If not please refer to the installations directory here.
    echo To avoid unnecessary changes it is recommanded to use the username "root" and the password of "root"
    echo Make sure you add the MySQL bin directory to your PATH environment variables.
    echo Example: "C:\Program Files\MySQL\MySQL Server 8.0\bin"
    pause
    exit /b 1
)

mysql -u%MYSQL_USERNAME% -p%MYSQL_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS furever CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Failed to connect to MySQL or create database.
    echo Please ensure MySQL is running. Press WinKey+R, services.msc, find MySQL, check if the status is "Running", and if not right click it and press run.
    echo Update MYSQL_PASSWORD and MYSQL_USERNAME in setup.bat, USERNAME and PASSWORD in DatabaseConnection.java if it's not the default advised.
    pause
    exit /b 1
)

echo Database ready. Importing schema...
mysql -u%MYSQL_USERNAME% -p%MYSQL_PASSWORD% furever < src\main\resources\schema.sql 2>nul
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
