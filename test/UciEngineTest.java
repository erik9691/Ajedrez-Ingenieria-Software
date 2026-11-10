import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.input.UciEngine;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

class UciEngineTest {
    static List<String> fakeCommand(String mode, Path pid) throws Exception {
        String java =
                Path.of(
                                System.getProperty("java.home"),
                                "bin",
                                System.getProperty("os.name").startsWith("Windows")
                                        ? "java.exe"
                                        : "java")
                        .toString();
        String classes =
                Path.of(
                                UciEngineTest.class
                                        .getProtectionDomain()
                                        .getCodeSource()
                                        .getLocation()
                                        .toURI())
                        .toString();
        return List.of(java, "-cp", classes, "UciEngineTest$FakeEngine", mode, pid.toString());
    }

    @Test
    void handshakeAndResponseAreReadPastInfoLines() throws Exception {
        Path pid = Files.createTempFile("uci-test-", ".pid");
        try (UciEngine engine = new UciEngine(fakeCommand("normal", pid))) {
            assertEquals("bestmove e2e4", engine.request("go movetime 1", "bestmove ", 1000));
        } finally {
            Files.deleteIfExists(pid);
        }
    }

    @Test
    void closedEngineRejectsRequests() throws Exception {
        Path pid = Files.createTempFile("uci-test-", ".pid");
        UciEngine engine = new UciEngine(fakeCommand("normal", pid));
        engine.close();
        engine.close();
        assertThrows(IOException.class, () -> engine.request("go", "bestmove ", 100));
        Files.deleteIfExists(pid);
    }

    @Test
    void timeoutReleasesStalledProcess() throws Exception {
        Path pid = Files.createTempFile("uci-test-", ".pid");
        try (UciEngine engine = new UciEngine(fakeCommand("stall", pid))) {
            long processId = Long.parseLong(Files.readString(pid));
            assertTimeoutPreemptively(
                    Duration.ofSeconds(3),
                    () ->
                            assertThrows(
                                    IOException.class,
                                    () -> engine.request("go", "bestmove ", 80)));
            assertTimeoutPreemptively(
                    Duration.ofSeconds(2),
                    () -> {
                        while (ProcessHandle.of(processId)
                                .map(ProcessHandle::isAlive)
                                .orElse(false)) Thread.sleep(10);
                    });
        } finally {
            Files.deleteIfExists(pid);
        }
    }

    @Test
    void unexpectedEofFailsPromptly() throws Exception {
        Path pid = Files.createTempFile("uci-test-", ".pid");
        try (UciEngine engine = new UciEngine(fakeCommand("eof", pid))) {
            assertThrows(IOException.class, () -> engine.request("go", "bestmove ", 1000));
        } finally {
            Files.deleteIfExists(pid);
        }
    }

    public static class FakeEngine {
        public static void main(String[] args) throws Exception {
            Files.writeString(Path.of(args[1]), Long.toString(ProcessHandle.current().pid()));
            Scanner in = new Scanner(System.in);
            while (in.hasNextLine()) {
                String command = in.nextLine();
                if (command.equals("uci")) System.out.println("uciok");
                if (command.equals("isready")) System.out.println("readyok");
                if (command.startsWith("go")) {
                    if (args[0].equals("eof")) return;
                    if (!args[0].equals("stall")) {
                        System.out.println("info depth 1");
                        System.out.println("bestmove e2e4");
                    }
                }
            }
        }
    }
}
