import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.display.ConsoleDisplayAdapter;
import main.adapters.driving.menu.ConsoleMenuAdapter;
import main.core.flags.PlayResult;
import main.ports.dto.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class MenuAndDisplayTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "1\n1\nCHAU",
                "2\n1\ns\nCHAU",
                "2\n1\nn",
                "3\n1\ns\nCHAU",
                "3\n1\nn",
                "4\n1",
                "5\n1",
                "oops\n1\n9\n1\nCHAU",
                "3\n1\n?\ns\nCHAU",
                ""
            })
    void everyModeAndInvalidMenuInputCanComplete(String text) {
        ServiceTest.Display display = new ServiceTest.Display();
        List<ServiceTest.Input> created = new ArrayList<>();
        new ConsoleMenuAdapter(
                        new Scanner(text),
                        () -> display,
                        () -> {
                            var input = new ServiceTest.Input();
                            created.add(input);
                            return input;
                        },
                        (white, path) -> {
                            assertTrue(path.contains("stockfish11"));
                            var input = new ServiceTest.Input();
                            created.add(input);
                            return input;
                        })
                .configureAndStartGame();
        assertEquals(1, display.endings);
        assertEquals(1, display.closes);
        for (var input : created) assertEquals(1, input.closes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3", "4"})
    void allTimeControlsReachThePlayers(String control) {
        ServiceTest.Input input = new ServiceTest.Input();
        new ConsoleMenuAdapter(
                        new Scanner("3\n" + control + "\nn"),
                        ServiceTest.Display::new,
                        ServiceTest.Input::new,
                        (white, path) -> input)
                .configureAndStartGame();
        if (control.equals("1")) assertNull(input.clock);
        else {
            long expected =
                    switch (control) {
                        case "2" -> 90 * 60_000L;
                        case "3" -> 30 * 60_000L;
                        default -> 10 * 60_000L;
                    };
            assertTrue(input.clock.remainingMillis() <= expected);
            assertTrue(input.clock.remainingMillis() > expected - 2000);
        }
    }

    @Test
    void failedSecondEngineConstructionClosesFirst() {
        AtomicInteger count = new AtomicInteger();
        ServiceTest.Input first = new ServiceTest.Input();
        ConsoleMenuAdapter menu =
                new ConsoleMenuAdapter(
                        new Scanner("5\n1"),
                        ServiceTest.Display::new,
                        ServiceTest.Input::new,
                        (white, path) -> {
                            if (count.incrementAndGet() == 2)
                                throw new IllegalStateException("Startup failed");
                            return first;
                        });
        assertThrows(IllegalStateException.class, menu::configureAndStartGame);
        assertEquals(1, first.closes);
    }

    String capture(Runnable action) {
        PrintStream previous = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(output);
            action.run();
        } finally {
            System.setOut(previous);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @ParameterizedTest
    @EnumSource(PlayResult.class)
    void everyResultCanBeDisplayed(PlayResult result) {
        try (ConsoleDisplayAdapter display = new ConsoleDisplayAdapter(false)) {
            String text = capture(() -> display.displayMessage(result));
            if (result != PlayResult.LEGAL && result != PlayResult.NONE)
                assertFalse(text.isBlank());
        }
    }

    @Test
    void boardRendersBothColorsAndClocks() {
        PieceView[][] cells = new PieceView[8][8];
        cells[0][0] = new PieceView('R', false);
        cells[7][0] = new PieceView('R', true);
        try (ConsoleDisplayAdapter display = new ConsoleDisplayAdapter(false)) {
            String text =
                    capture(() -> {
                        display.displayGameStart();
                        display.displayBoard(
                                new BoardView(cells), new ClockView(60000, 120000, true));
                        display.displayTurn(false);
                        display.displayPromotionMenu(new BoardView(cells));
                        display.displayGameOver(null);
                    });
            assertTrue(text.contains("01:00"));
            assertTrue(text.contains("02:00"));
            assertTrue(text.contains("8 | r"));
            assertTrue(text.contains("1 | R"));
            assertTrue(text.contains("Empate"));
            assertTrue(text.contains("negras"));
        }
    }

    @Test
    void rejectionAndWinnerAreDisplayed() {
        try (ConsoleDisplayAdapter display = new ConsoleDisplayAdapter(false)) {
            String text =
                    capture(() -> {
                        display.displayMoveRejected(PlayResult.ILLEGAL);
                        display.displayGameOver(true);
                        display.displayGameOver(false);
                    });
            assertTrue(text.contains("Error:"));
            assertTrue(text.contains("Ganan las blancas"));
            assertTrue(text.contains("Ganan las negras"));
        }
    }
}
