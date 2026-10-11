#!/bin/bash
# Compiles and starts the Vehicle Rental System. Contains no credentials.
cd "$(dirname "$0")"
JAR=$(ls lib/*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then echo "Put mysql-connector-j.jar into the lib folder first."; exit 1; fi
mkdir -p out
javac -d out -sourcepath src src/Main.java || exit 1
java -cp "out:$JAR" Main
