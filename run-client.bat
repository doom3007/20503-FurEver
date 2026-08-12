@echo off
REM Make sure the server is running before starting the client
REM Run run-server.bat first
chcp 65001 >nul
echo Starting FurEver Client...
call "%~dp0installations\maven-mvnd-1.0.6-windows-amd64\bin\mvnd.cmd" javafx:run
pause