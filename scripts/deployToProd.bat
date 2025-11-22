@echo off
REM ================================
REM Build, copy, and restart Spring Boot app on Linux server
REM ================================

REM 1. Build the WAR using Maven
echo Building WAR...
CALL "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests -Dmaven.test.skip=true
IF %ERRORLEVEL% NEQ 0 (
    echo Maven build failed!
    exit /b %ERRORLEVEL%
)

REM 2. Copy WAR to remote Linux server
echo Copying WAR to remote server...
REM assuming ssh key is installed for root user
scp target\Lixiarchos.war root@lixiarchos.site:/opt/lixiarchos/Lixiarchos.war
IF %ERRORLEVEL% NEQ 0 (
    echo SCP failed!
    exit /b %ERRORLEVEL%
)

REM 3. Restart the application remotely
echo Restarting application...
REM assuming ssh key is installed for root user
ssh root@lixiarchos.site "sudo systemctl restart lixiarchos"
IF %ERRORLEVEL% NEQ 0 (
    echo Remote restart failed!
    exit /b %ERRORLEVEL%
)

echo Deployment completed successfully!