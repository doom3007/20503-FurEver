@echo off
echo Packaging FurEver for production...
echo.

SET "PROJECT_DIR=%~dp0"
SET "ZIP_FILE=%PROJECT_DIR%furever-production.zip"
SET "TEMP_DIR=%PROJECT_DIR%temp_package\"

echo Cleaning up previous package...
if exist "%ZIP_FILE%" del "%ZIP_FILE%"
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"

echo Creating temporary directory...
mkdir "%TEMP_DIR%"

echo Copying source files...
xcopy "%PROJECT_DIR%src" "%TEMP_DIR%src\" /E /I /Y >nul

echo Copying Maven configuration...
copy "%PROJECT_DIR%pom.xml" "%TEMP_DIR%\" >nul

echo Copying batch files...
copy "%PROJECT_DIR%run-server.bat" "%TEMP_DIR%\" >nul
copy "%PROJECT_DIR%run-client.bat" "%TEMP_DIR%\" >nul
copy "%PROJECT_DIR%setup.bat" "%TEMP_DIR%\" >nul
copy "%PROJECT_DIR%drop_db.bat" "%TEMP_DIR%\" >nul

echo Copying database schema...
xcopy "%PROJECT_DIR%src\main\resources" "%TEMP_DIR%src\main\resources\" /E /I /Y >nul

echo Copying README...
copy "%PROJECT_DIR%README.md" "%TEMP_DIR%\" >nul

echo Copying compiled JAR files...
if exist "%PROJECT_DIR%target\furever-server.jar" (
    copy "%PROJECT_DIR%target\furever-server.jar" "%TEMP_DIR%\" >nul
    echo Server JAR copied successfully
) else (
    echo WARNING: Server JAR not found. You may need to build the project first.
)

echo Copying Maven installation...
xcopy "%PROJECT_DIR%installations" "%TEMP_DIR%installations\" /E /I /Y >nul

echo Creating ZIP file...
powershell -Command "Compress-Archive -Path '%TEMP_DIR%\*' -DestinationPath '%ZIP_FILE%' -Force"

echo Cleaning up temporary directory...
rmdir /s /q "%TEMP_DIR%"

echo.
echo ============================================
echo Production package created successfully!
echo Location: %ZIP_FILE%
echo ============================================
echo.
echo Package includes:
echo - Source code (src/)
echo - Maven configuration (pom.xml)
echo - Batch files for running the application
echo - Database schema
echo - README documentation
echo - Maven installation
echo - Compiled server JAR (if available)
echo.
echo Note: You may need to update database credentials in:
echo - src\main\java\com\furever\server\data\DatabaseConnection.java
echo - setup.bat
echo.

pause
