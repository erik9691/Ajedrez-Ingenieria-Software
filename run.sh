#!/bin/bash
set -e

case "$(uname -s)" in
    Linux*)                 OS=linux ;;
    Darwin*)                OS=mac ;;
    MINGW*|MSYS*|CYGWIN*)   OS=windows ;;
    *)                      OS=unknown ;;
esac

javac -d out $(find src/main -name "*.java")
java -cp out main.Main --os "$OS" "$@"
