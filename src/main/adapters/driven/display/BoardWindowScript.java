package main.adapters.driven.display;

import java.nio.file.Path;

final class BoardWindowScript {
    private BoardWindowScript() {}

    static String build(Path boardFile) {
        String file = boardFile.toString().replace('\\', '/').replace("'", "'\\''");
        return "f='"
                + file
                + "'; "
                + """
                last=''; shown=''; started=0
                while [ -f "$f" ]; do
                    raw=$(cat "$f" 2>/dev/null)
                    if [ "$raw" != "$last" ]; then
                        last="$raw"; started=$(date +%s)
                        meta=$(printf '%s\\n' "$raw" | sed -n 's/^#CLOCK //p' | head -1)
                    fi
                    if [ -z "$meta" ]; then
                        body="$raw"
                    else
                        now=$(date +%s); set -- $meta; wrem=$2; brem=$4
                        if [ "$6" = W ]; then wrem=$(( wrem - (now - started) * 1000 )); else brem=$(( brem - (now - started) * 1000 )); fi
                        [ "$wrem" -lt 0 ] && wrem=0
                        [ "$brem" -lt 0 ] && brem=0
                        ws=$(( wrem / 1000 )); bs=$(( brem / 1000 ))
                        clockline=$(printf 'Blancas: %02d:%02d  Negras: %02d:%02d' $(( ws / 60 )) $(( ws % 60 )) $(( bs / 60 )) $(( bs % 60 )))
                        body=$(printf '%s\\n' "$raw" | sed '1d' | sed "1s/.*/$clockline/")
                    fi
                    if [ "$body" != "$shown" ]; then clear; printf '%s\\n' "$body"; shown="$body"; fi
                    sleep 0.2
                done
                """;
    }
}
