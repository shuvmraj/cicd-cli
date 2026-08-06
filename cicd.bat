@echo off
SET SCRIPT_DIR=%~dp0
SET JAR_PATH=%SCRIPT_DIR%cicd-cli\target\cicd-cli-1.0-SNAPSHOT.jar

IF NOT EXIST "%JAR_PATH%" (
    SET JAR_PATH=%SCRIPT_DIR%target\cicd-cli-1.0-SNAPSHOT.jar
)

IF NOT EXIST "%JAR_PATH%" (
    echo Error: Executable JAR not found at %JAR_PATH%.
    echo Please build the project first by running: mvn clean package
    exit /b 1
)

java -jar "%JAR_PATH%" %*
