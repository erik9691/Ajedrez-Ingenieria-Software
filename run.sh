#!/bin/bash
set -e

javac -d out $(find src -name "*.java")
java -cp out ajedrez.Main "$@"
