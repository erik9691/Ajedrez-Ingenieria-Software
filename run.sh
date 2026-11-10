#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
case "$(uname -s)" in
    Linux*) chmod +x engines/stockfish11/sf11-linux ;;
    Darwin*) chmod +x engines/stockfish11/sf11-mac ;;
esac
javac --release 21 -encoding UTF-8 -d out -sourcepath src src/main/Main.java
java -cp out main.Main "$@"
