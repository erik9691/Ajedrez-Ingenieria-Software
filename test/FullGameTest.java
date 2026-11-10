import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.input.FenEncoder;
import main.core.flags.*;
import main.core.game.*;
import main.core.managers.GameManager;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.*;
import java.util.stream.Stream;

class FullGameTest {
    static Stream<String> games() throws Exception {
        return Files.readAllLines(Path.of("test/resources/games.txt")).stream();
    }

    @ParameterizedTest(name = "Game {index}: replay and compare every resulting FEN")
    @MethodSource("games")
    void completeGamesMatchIndependentReference(String moves) {
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        for (String step : moves.split("\\|")) {
            String[] data = step.split("~");
            String uci = data[0];
            var result = game.movePiece(ServiceTest.ok(uci).move());
            assertTrue(result.isLegal(), uci + ": " + result);
            if (uci.length() == 5) {
                PromotionId choice =
                        switch (uci.charAt(4)) {
                            case 'r' -> PromotionId.ROOK;
                            case 'b' -> PromotionId.BISHOP;
                            case 'n' -> PromotionId.KNIGHT;
                            default -> PromotionId.QUEEN;
                        };
                assertTrue(game.promotePawn(choice).isLegal());
            }
            assertEquals(data[1], FenEncoder.encode(game.getBoardView()), "After " + uci);
        }
    }
}
