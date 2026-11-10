package main.adapters.driven.display;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

class BoardWindowScriptTest {
    static Path bash() {
        String custom = System.getenv("CHESS_BASH");
        return Path.of(
                custom != null
                        ? custom
                        : System.getProperty("os.name").startsWith("Windows")
                                ? "C:/Program Files/Git/bin/bash.exe"
                                : "/bin/bash");
    }

    void await(Path log, String text) {
        assertTimeoutPreemptively(
                Duration.ofSeconds(4),
                () -> {
                    while (!Files.readString(log).contains(text)) Thread.sleep(20);
                },
                "Waiting for: " + text);
    }

    @Test
    void boardUpdatesAndQuotedPathsWorkWithoutOpeningATerminal() throws Exception {
        assumeTrue(Files.exists(bash()), "Bash is required for the optional separate board window");
        Path folder = Files.createTempDirectory("board-script-");
        Path board = folder.resolve("tablero con ' comilla.txt"),
                log = folder.resolve("output.txt");
        Files.writeString(board, "PRIMERO");
        Files.writeString(log, "");
        Path script = folder.resolve("viewer.sh");
        Files.writeString(script, BoardWindowScript.build(board));
        ProcessBuilder command =
                new ProcessBuilder(
                        bash().toString(), "--login", script.toString().replace('\\', '/'));
        command.environment().put("TERM", "xterm");
        command.redirectOutput(log.toFile()).redirectErrorStream(true);
        Process process = command.start();
        try {
            await(log, "PRIMERO");
            Files.writeString(board, "SEGUNDO");
            await(log, "SEGUNDO");
            Files.writeString(
                    board, "#CLOCK W 60000 B 120000 A W\nBlancas: 01:00  Negras: 02:00\nTERCERO");
            await(log, "TERCERO");
            await(log, "Blancas: 00:59");
            Files.delete(board);
            assertTrue(process.waitFor(2, TimeUnit.SECONDS));
            assertEquals(0, process.exitValue());
        } catch (Throwable failure) {
            System.err.println(Files.readString(log));
            throw failure;
        } finally {
            Files.deleteIfExists(board);
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
            }
            Thread.sleep(300);
            Files.deleteIfExists(log);
            Files.deleteIfExists(script);
            Files.delete(folder);
        }
    }
}
