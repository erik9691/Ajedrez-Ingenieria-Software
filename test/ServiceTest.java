import static org.junit.jupiter.api.Assertions.*;

import main.core.application.ChessService;
import main.core.flags.*;
import main.core.game.*;
import main.core.game.pieces.*;
import main.core.managers.GameManager;
import main.core.models.*;
import main.core.time.PlayerClock;
import main.ports.dto.*;
import main.ports.out.*;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

class ServiceTest {
    static class Display implements DisplayPort {
        List<Boolean> turns = new ArrayList<>();
        List<PlayResult> messages = new ArrayList<>(), rejected = new ArrayList<>();
        int endings, closes;
        Boolean winner;

        public void displayGameStart() {}

        public void displayBoard(BoardView b, ClockView c) {}

        public void displayTurn(boolean white) {
            turns.add(white);
        }

        public void displayMessage(PlayResult r) {
            messages.add(r);
        }

        public void displayMoveRejected(PlayResult r) {
            rejected.add(r);
        }

        public void displayPromotionMenu(BoardView b) {}

        public void displayGameOver(Boolean white) {
            endings++;
            winner = white;
        }

        public void close() {
            closes++;
        }
    }

    static class Input implements PlayerInputPort {
        Deque<Function<BoardView, InputResult>> actions = new ArrayDeque<>();
        PlayerClock clock;
        int closes, calls;
        Function<BoardView, PromotionId> promotion = b -> PromotionId.QUEEN;

        Input add(String uci) {
            actions.add(b -> ok(uci));
            return this;
        }

        Input then(Function<BoardView, InputResult> action) {
            actions.add(action);
            return this;
        }

        public InputResult requestMove(BoardView b) {
            calls++;
            assertTrue(calls < 20, "Unexpected input loop");
            return actions.isEmpty()
                    ? new InputResult(null, PlayerStatus.FORFEIT)
                    : actions.remove().apply(b);
        }

        public PromotionId requestPromotion(BoardView b) {
            return promotion.apply(b);
        }

        public void setClock(PlayerClock c) {
            clock = c;
        }

        public void close() {
            closes++;
        }
    }

    static InputResult ok(String move) {
        return new InputResult(
                new Move(
                        RegressionTest.p(move.substring(0, 2)),
                        RegressionTest.p(move.substring(2, 4))),
                PlayerStatus.OK);
    }

    final MoveHistory history = new MoveHistory();
    final Board board = new Board(history);
    final GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
    final Display display = new Display();
    final Input white = new Input(), black = new Input();

    Boolean run(PlayerClock clock) {
        return new ChessService(game, display, white, black, clock).runGame();
    }

    @Test
    void whiteForfeitAwardsBlack() {
        assertFalse(run(null));
        assertEquals(1, display.endings);
    }

    @Test
    void blackForfeitAwardsWhite() {
        white.add("e2e4");
        assertTrue(run(null));
    }

    @Test
    void invalidMoveKeepsSamePlayer() {
        white.add("e2e5").add("e2e4");
        assertTrue(run(null));
        assertEquals(List.of(true, true, false), display.turns);
        assertEquals(List.of(PlayResult.ILLEGAL), display.rejected);
    }

    @Test
    void foolsMateEndsWithBlackWinner() {
        white.add("f2f3").add("g2g4");
        black.add("e7e5").add("d8h4");
        assertFalse(run(null));
        assertTrue(display.messages.contains(PlayResult.CHECKMATE));
        assertEquals(4, display.turns.size());
    }

    @Test
    void timeoutStatusEndsImmediately() {
        white.then(b -> new InputResult(null, PlayerStatus.TIME_OVER));
        assertFalse(run(null));
        assertEquals(1, white.calls);
    }

    @Test
    void expiredClockDoesNotRequestMove() {
        assertFalse(run(new PlayerClock(0)));
        assertEquals(0, white.calls);
    }

    @Test
    void clocksAreIndependentCopies() {
        run(new PlayerClock(1000));
        assertNotSame(white.clock, black.clock);
        assertEquals(1000, black.clock.remainingMillis());
    }

    @Test
    void timeoutDetectedAfterAnswerWithoutApplyingMove() {
        AtomicLong now = new AtomicLong();
        white.then(b -> {
            now.set(2_000_000_000L);
            return ok("e2e4");
        });
        assertFalse(run(new PlayerClock(1000, now::get)));
        assertNotNull(board.getPiece(RegressionTest.p("e2")));
    }

    @Test
    void blockedPlayerCannotFreezeTimedMatch() {
        white.then(b -> {
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return ok("e2e4");
        });
        assertTimeoutPreemptively(
                Duration.ofSeconds(2), () -> assertFalse(run(new PlayerClock(70))));
        assertTrue(display.messages.contains(PlayResult.TIME_OVER));
    }

    @Test
    void resourcesClosedOnSuccess() {
        run(null);
        assertEquals(1, white.closes);
        assertEquals(1, black.closes);
        assertEquals(1, display.closes);
    }

    @Test
    void resourcesClosedOnInputFailure() {
        white.then(b -> {
            throw new IllegalStateException("Input failed");
        });
        assertThrows(IllegalStateException.class, () -> run(null));
        assertEquals(1, white.closes);
        assertEquals(1, black.closes);
        assertEquals(1, display.closes);
    }

    GameManager promotionGame() {
        return new GameManager(board, new GameOverChecker(board, history), history) {
            public void startGame() {
                super.startGame();
                board.clear();
                board.setPiece(RegressionTest.p("e1"), new King(true));
                board.setPiece(RegressionTest.p("a6"), new King(false));
                board.setPiece(RegressionTest.p("e7"), new Pawn(true));
            }
        };
    }

    @Test
    void closedPromotionInputForfeitsWithoutCrash() {
        white.add("e7e8");
        white.promotion = b -> null;
        assertFalse(new ChessService(promotionGame(), display, white, black, null).runGame());
    }

    @Test
    void promotionConsumesRemainingThinkingTime() {
        AtomicLong now = new AtomicLong();
        white.then(b -> {
            now.set(400_000_000);
            return ok("e7e8");
        });
        white.promotion =
                b -> {
                    now.set(1_100_000_000);
                    return PromotionId.QUEEN;
                };
        assertFalse(
                new ChessService(
                                promotionGame(),
                                display,
                                white,
                                black,
                                new PlayerClock(1000, now::get))
                        .runGame());
        assertTrue(display.messages.contains(PlayResult.TIME_OVER));
        assertInstanceOf(Pawn.class, board.getPiece(RegressionTest.p("e8")));
    }

    @Test
    void promotionToKnightCanEndAsDraw() {
        white.add("e7e8");
        white.promotion = b -> PromotionId.KNIGHT;
        assertNull(new ChessService(promotionGame(), display, white, black, null).runGame());
        assertTrue(display.messages.contains(PlayResult.DRAW));
    }
}
