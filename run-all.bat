@echo off
REM Make sure MySQL is running with credentials set in DatabaseConnection.java
REM Default: username=root, password=root
echo FurEver - All-In-One Startup
echo ==============================
echo.

echo 1. Setting up database and building project...
call setup.bat
if %errorlevel% neq 0 (
    echo Setup failed. Please fix errors and try again.
    pause
    exit /b 1
)

echo.
echo 2. Starting server...
start "FurEver Server" run-server.bat

echo Waiting for server to start...
timeout /t 3 /nobreak >nul

echo.
echo 3. Starting client...
start "FurEver Client" run-client.bat

echo.
echo Both server and client are now running!
echo Close the client window when you're done.
echo The server will continue running in the background.