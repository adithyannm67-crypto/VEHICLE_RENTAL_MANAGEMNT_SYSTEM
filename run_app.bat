@echo off
REM Compiles and starts the Vehicle Rental System. Contains no credentials.
cd /d "%~dp0"
if not exist lib\*.jar (
  echo Put mysql-connector-j.jar into the lib folder first.
  pause
  exit /b 1
)
if not exist out mkdir out
javac -d out -sourcepath src src\Main.java
if errorlevel 1 (
  pause
  exit /b 1
)
for %%J in (lib\*.jar) do set JAR=%%J
java -cp "out;%JAR%" Main
