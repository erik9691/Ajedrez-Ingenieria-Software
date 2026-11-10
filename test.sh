#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
version=1.11.4
jar="lib/junit-platform-console-standalone-$version.jar"
expected=b016ef6b1c3454d6d7c2c88ce081dabf289699686af6622d6e4e2e1b54b4a2fc
mkdir -p lib .build/main .build/test
if [[ ! -f "$jar" ]]; then
    curl --fail --location "https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/$version/junit-platform-console-standalone-$version.jar" --output "$jar"
fi
if command -v sha256sum >/dev/null; then actual=$(sha256sum "$jar"); else actual=$(shasum -a 256 "$jar"); fi
[[ ${actual%% *} == "$expected" ]] || { echo "El archivo de JUnit no coincide con la version esperada." >&2; exit 1; }
find .build/main .build/test -name '*.class' -delete
find src/main -name '*.java' > .build/main-sources.txt
find test -name '*.java' > .build/test-sources.txt
classpath_separator=:
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) classpath_separator=';' ;; esac
case "$(uname -s)" in Linux*) chmod +x engines/stockfish11/sf11-linux ;; Darwin*) chmod +x engines/stockfish11/sf11-mac ;; esac
javac --release 21 -encoding UTF-8 -d .build/main @.build/main-sources.txt
javac --release 21 -encoding UTF-8 -cp ".build/main${classpath_separator}$jar" -d .build/test @.build/test-sources.txt
java -jar "$jar" execute --class-path ".build/main${classpath_separator}.build/test" --scan-class-path --fail-if-no-tests --details summary --reports-dir .build/reports --disable-banner
