@echo off
REM Make sure MySQL is running and credentials are set in DatabaseConnection.java
REM Default: username=root, password=root
chcp 65001 >nul
echo Starting FurEver Server...
echo API messages will appear below:
echo.
java -jar target\furever-server.jar
pause
