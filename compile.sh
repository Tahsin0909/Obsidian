#!/bin/bash
# Compiles the whole project into ./bin
# Usage: ./compile.sh
set -e
SRC_DIR="src/main/java"
OUT_DIR="bin"

mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > sources.txt
javac -encoding UTF-8 -d "$OUT_DIR" @sources.txt
rm -f sources.txt
echo "Build complete -> $OUT_DIR"
