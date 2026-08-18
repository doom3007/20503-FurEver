@echo off
REM Make sure the server is running before starting the client
REM Run run-server.bat first
chcp 65001 >nul
echo Starting FurEver Client...
call "%~dp0installations\apache-maven-3.9.16\bin\mvn.cmd" javafx:run
pause