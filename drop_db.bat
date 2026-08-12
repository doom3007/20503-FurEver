@echo off
REM Update MYSQL_USERNAME and MYSQL_PASSWORD if your MySQL uses different credentials
REM Default: username=root, password=root
set MYSQL_USERNAME=root
set MYSQL_PASSWORD=root
echo Dropping FurEver database...
mysql -u %MYSQL_USERNAME% -p%MYSQL_PASSWORD% -e "DROP DATABASE IF EXISTS furever;"
echo Database dropped successfully.
pause
