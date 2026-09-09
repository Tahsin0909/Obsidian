#!/bin/bash

# Compiles and runs the application

set -e

./compile.sh

echo "Starting application..."

java -cp bin com.library.Main