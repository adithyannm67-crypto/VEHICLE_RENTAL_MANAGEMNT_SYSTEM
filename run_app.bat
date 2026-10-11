
@echo off
REM Compiles and starts the Vehicle Rental System.
REM No database credentials are stored in this file.

cd /d "%~dp0"

if not exist "lib\mysql-connector-j-26.7.0.jar" (
    echo Put the MySQL Connector/J JAR in the lib folder.
    pause
    exit /b 1
)

if not exist "lib\dotenv-java-3.2.0.jar" (
    echo Put dotenv-java-3.2.0.jar in the lib folder.
    pause
    exit /b 1
)

if not exist ".env" (
    echo Missing .env file in the project root.
    pause
    exit /b 1
)

if not exist out mkdir out

javac -cp "lib\*" -d out -sourcepath src src\Main.java

if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)

java -cp "out;lib\*" Main

pause