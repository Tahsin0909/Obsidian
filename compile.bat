@echo off
REM Compiles the whole project into .\bin
set SRC_DIR=src\main\java
set OUT_DIR=bin

if not exist %OUT_DIR% mkdir %OUT_DIR%
dir /s /b %SRC_DIR%\*.java > sources.txt
javac -encoding UTF-8 -d %OUT_DIR% @sources.txt
del sources.txt
echo Build complete -^> %OUT_DIR%
