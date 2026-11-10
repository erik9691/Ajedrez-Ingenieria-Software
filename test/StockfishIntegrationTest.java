import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.input.*;
import main.core.game.*;
import main.core.managers.GameManager;
import main.ports.dto.*;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class StockfishIntegrationTest {
    static String path() {
        String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);
        return Path.of(
                        "engines/stockfish11",
                        os.contains("win")
                                ? "sf11-windows.exe"
                                : os.contains("mac") ? "sf11-mac" : "sf11-linux")
                .toString();
    }

    @Test
    void realEngineConfirmsInitialPositionPerft() throws Exception {
        try (UciEngine engine = new UciEngine(path())) {
            assertEquals(
                    "Nodes searched: 8902",
                    engine.request("position startpos\ngo perft 3", "Nodes searched:", 5000)
                            .trim());
        }
    }

    @Test
    void realEnginePlaysTwelveLegalPliesAndCloses() throws Exception {
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        try (StockfishElevenAI white = new StockfishElevenAI(true, new UciEngine(path()));
                StockfishElevenAI black = new StockfishElevenAI(false, new UciEngine(path()))) {
            for (int i = 0; i < 12; i++) {
                BoardView view = game.getBoardView();
                InputResult move = (game.isWhiteTurn() ? white : black).requestMove(view);
                assertEquals(PlayerStatus.OK, move.status());
                assertTrue(view.legalMoves().contains(move.move()));
                assertTrue(game.movePiece(move.move()).isLegal());
            }
        }
    }

    @Test
    void missingEngineFallsBackWithoutCrashing() {
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        try (StockfishElevenAI ai = new StockfishElevenAI(true, "does-not-exist/stockfish")) {
            BoardView view = game.getBoardView();
            assertTrue(view.legalMoves().contains(ai.requestMove(view).move()));
        }
    }
}
