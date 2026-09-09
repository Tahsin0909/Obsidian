#!/bin/bash

# Compiles the whole project into ./bin

set -e

SRC_DIR="src/main/java"
RES_DIR="src/main/resources"
OUT_DIR="bin"

# Create output directory
mkdir -p "$OUT_DIR"

# Find all Java files
find "$SRC_DIR" -name "*.java" > sources.txt

# Compile Java files
javac -encoding UTF-8 -d "$OUT_DIR" @sources.txt

# Remove temporary file
rm sources.txt

# Copy resources
if [ -d "$RES_DIR" ]; then
    cp -r "$RES_DIR"/. "$OUT_DIR"/
fi

echo "Build complete -> $OUT_DIR"