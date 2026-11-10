import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.input.*;
import main.core.flags.*;
import main.core.game.*;
import main.core.game.pieces.*;
import main.core.managers.GameManager;
import main.core.models.*;
import main.core.time.PlayerClock;
import main.ports.dto.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.*;

class AdapterTest {
    static class Engine implements EngineConnection {
        String reply = "bestmove e2e4", command;
        boolean fail, closed;
        int calls;

        public String request(String c, String prefix, long timeout) throws IOException {
            calls++;
            command = c;
            if (fail) throw new IOException("Test failure");
            return reply;
        }

        public void close() {
            closed = true;
        }
    }

    final MoveHistory history = new MoveHistory();
    final Board board = new Board(history);
    final GameManager game = new GameManager(board, new GameOverChecker(board, history), history);

    BoardView start() {
        game.startGame();
        return game.getBoardView();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20})
    void randomAIAlwaysUsesCurrentPlayersLegalMoves(int seed) {
        BoardView view = start();
        RandomMovesAI ai = new RandomMovesAI(new Random(seed));
        for (int i = 0; i < 50; i++) {
            InputResult result = ai.requestMove(view);
            assertEquals(PlayerStatus.OK, result.status());
            assertTrue(view.legalMoves().contains(result.move()));
        }
    }

    @Test
    void randomAIHandlesEmptyBoardWithoutException() {
        assertEquals(
                PlayerStatus.FORFEIT,
                new RandomMovesAI().requestMove(new BoardView(new PieceView[8][8])).status());
    }

    @Test
    void randomAIDoesNotSubtractArbitraryFiveSeconds() {
        RandomMovesAI ai = new RandomMovesAI();
        PlayerClock clock = new PlayerClock(10000);
        ai.setClock(clock);
        ai.requestMove(start());
        assertEquals(10000, clock.remainingMillis());
    }

    @Test
    void randomAIHonorsExpiredClock() {
        RandomMovesAI ai = new RandomMovesAI();
        ai.setClock(new PlayerClock(0));
        assertEquals(PlayerStatus.TIME_OVER, ai.requestMove(start()).status());
    }

    @Test
    void randomPromotionAlwaysProducesAChoice() {
        RandomMovesAI ai = new RandomMovesAI(new Random(8));
        Set<PromotionId> choices = EnumSet.noneOf(PromotionId.class);
        for (int i = 0; i < 100; i++) choices.add(ai.requestPromotion(start()));
        assertEquals(EnumSet.allOf(PromotionId.class), choices);
    }

    @Test
    void initialFenIncludesAllRealMetadata() {
        assertEquals(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                FenEncoder.encode(start()));
    }

    @Test
    void fenIncludesEnPassantTurnAndCounters() {
        start();
        game.movePiece(ServiceTest.ok("e2e4").move());
        assertTrue(FenEncoder.encode(game.getBoardView()).endsWith(" b KQkq e3 0 1"));
        game.movePiece(ServiceTest.ok("g8f6").move());
        assertTrue(FenEncoder.encode(game.getBoardView()).endsWith(" w KQkq - 1 2"));
    }

    @Test
    void stockfishMoveIsValidatedAndPositionSentExactly() {
        Engine engine = new Engine();
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        BoardView view = start();
        assertEquals(ServiceTest.ok("e2e4").move(), ai.requestMove(view).move());
        assertEquals(
                "position fen " + FenEncoder.encode(view) + "\ngo movetime 500", engine.command);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "bestmove (none)",
                "bestmove 0000",
                "bestmove",
                "bestmove a",
                "bestmove z9z1",
                "bestmove e2e5",
                "bestmove e2e4q",
                "garbage",
                "bestmove e2e4x",
                ""
            })
    void malformedOrIllegalEngineResponseFallsBackToLegalMove(String response) {
        Engine engine = new Engine();
        engine.reply = response;
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        BoardView view = start();
        assertTrue(view.legalMoves().contains(ai.requestMove(view).move()));
    }

    @Test
    void engineReadFailureClosesEngineAndFallsBack() {
        Engine engine = new Engine();
        engine.fail = true;
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        BoardView view = start();
        assertTrue(view.legalMoves().contains(ai.requestMove(view).move()));
        assertTrue(engine.closed);
        ai.requestMove(view);
        assertEquals(1, engine.calls);
    }

    @Test
    void closeReleasesEngineAndIsIdempotent() {
        Engine engine = new Engine();
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        ai.close();
        ai.close();
        assertTrue(engine.closed);
    }

    @Test
    void expiredClockDoesNotContactEngine() {
        Engine engine = new Engine();
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        ai.setClock(new PlayerClock(0));
        assertEquals(PlayerStatus.TIME_OVER, ai.requestMove(start()).status());
        assertEquals(0, engine.calls);
    }

    @Test
    void noLegalMovesDoesNotContactEngine() {
        Engine engine = new Engine();
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        assertEquals(
                PlayerStatus.FORFEIT, ai.requestMove(new BoardView(new PieceView[8][8])).status());
        assertEquals(0, engine.calls);
    }

    @ParameterizedTest
    @ValueSource(strings = {"q", "r", "b", "n"})
    void engineUnderpromotionIsPreserved(String suffix) {
        board.setPiece(RegressionTest.p("e7"), new Pawn(true));
        board.setPiece(RegressionTest.p("e1"), new King(true));
        board.setPiece(RegressionTest.p("a6"), new King(false));
        BoardView view =
                new BoardView(
                        board.toView().cells(), true, "-", null, 0, 1, board.legalMoves(true));
        Engine engine = new Engine();
        engine.reply = "bestmove e7e8" + suffix;
        StockfishElevenAI ai = new StockfishElevenAI(true, engine);
        assertEquals(ServiceTest.ok("e7e8").move(), ai.requestMove(view).move());
        PromotionId expected =
                switch (suffix) {
                    case "r" -> PromotionId.ROOK;
                    case "b" -> PromotionId.BISHOP;
                    case "n" -> PromotionId.KNIGHT;
                    default -> PromotionId.QUEEN;
                };
        assertEquals(expected, ai.requestPromotion(view));
    }
}
