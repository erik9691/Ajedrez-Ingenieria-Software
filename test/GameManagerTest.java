import static org.junit.jupiter.api.Assertions.*;

import main.core.flags.PlayResult;
import main.core.flags.PromotionId;
import main.core.game.Board;
import main.core.game.GameOverChecker;
import main.core.game.MoveHistory;
import main.core.game.pieces.King;
import main.core.game.pieces.Knight;
import main.core.game.pieces.Pawn;
import main.core.game.pieces.Queen;
import main.core.managers.GameManager;
import main.core.models.Move;
import main.core.models.Position;

import org.junit.jupiter.api.Test;

public class GameManagerTest {

    // startGame() arma el tablero inicial; lo vaciamos para armar posiciones de prueba
    private void clearBoard(Board board) {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                board.setPiece(new Position(row, column), null);
            }
        }
    }

    // Test para la promotion
    @Test
    public void movePiece_pawnReachesLastRow_shouldReturnPromotionPendingAndKeepTurn() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        clearBoard(board);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(2, 0), new King(false));
        board.setPiece(new Position(1, 4), new Pawn(true));

        // Act
        PlayResult result = game.movePiece(new Move(new Position(1, 4), new Position(0, 4)));

        // Assert
        assertEquals(
                PlayResult.PROMOTION_PENDING,
                result,
                "Un peon que llega a la ultima fila deberia devolver PlayResult.PROMOTION_PENDING");
        assertTrue(
                game.isPromotionPending(),
                "El juego deberia estar esperando la eleccion de la pieza de promocion");
        assertTrue(game.isWhiteTurn(), "El turno no deberia cambiar hasta completar la promocion");
    }

    @Test
    public void promotePawn_toQueen_shouldReplacePawnAndChangeTurn() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        clearBoard(board);
        Position promotionSquare = new Position(0, 4);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(2, 0), new King(false));
        board.setPiece(new Position(1, 4), new Pawn(true));
        game.movePiece(new Move(new Position(1, 4), promotionSquare));

        // Act
        PlayResult result = game.promotePawn(PromotionId.QUEEN);

        // Assert
        assertEquals(
                PlayResult.LEGAL,
                result,
                "Completar la promocion deberia devolver PlayResult.LEGAL");
        assertInstanceOf(
                Queen.class,
                board.getPiece(promotionSquare),
                "El peon deberia ser reemplazado por una dama");
        assertTrue(
                board.getPiece(promotionSquare).getColor(),
                "La dama promocionada deberia conservar el color del peon");
        assertFalse(game.isPromotionPending(), "Ya no deberia haber una promocion pendiente");
        assertFalse(
                game.isWhiteTurn(), "El turno deberia pasar a las negras despues de la promocion");
    }

    @Test
    public void promotePawn_withoutPendingPromotion_shouldReturnInvalidPromotion() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();

        // Act
        PlayResult result = game.promotePawn(PromotionId.QUEEN);

        // Assert
        assertEquals(
                PlayResult.INVALID_PROMOTION,
                result,
                "Promocionar sin una promocion pendiente deberia devolver"
                        + " PlayResult.INVALID_PROMOTION");
    }

    // Tablas: triple repeticion

    @Test
    public void movePiece_samePositionThreeTimes_shouldReturnDraw() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        clearBoard(board);
        // Con dos caballos sigue habiendo material para dar mate; sus movimientos conservan el
        // enroque.
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(7, 1), new Knight(true));
        board.setPiece(new Position(0, 1), new Knight(false));

        Move whiteOut = new Move(new Position(7, 1), new Position(5, 0));
        Move whiteBack = new Move(new Position(5, 0), new Position(7, 1));
        Move blackOut = new Move(new Position(0, 1), new Position(2, 0));
        Move blackBack = new Move(new Position(2, 0), new Position(0, 1));

        // Act: dos ciclos completos (8 medias jugadas) hacen que la posicion inicial aparezca 3
        // veces
        PlayResult[] results = new PlayResult[8];
        Move[] sequence = {
            whiteOut, blackOut, whiteBack, blackBack, whiteOut, blackOut, whiteBack, blackBack
        };
        for (int i = 0; i < sequence.length; i++) {
            results[i] = game.movePiece(sequence[i]);
        }

        // Assert
        for (int i = 0; i < 7; i++) {
            assertEquals(
                    PlayResult.LEGAL,
                    results[i],
                    "La media jugada " + (i + 1) + " no deberia terminar la partida");
        }
        assertEquals(
                PlayResult.DRAW,
                results[7],
                "La tercera repeticion de la misma posicion deberia devolver PlayResult.DRAW");
    }

    @Test
    public void movePiece_positionRepeatedOnlyTwice_shouldNotBeDraw() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameManager game = new GameManager(board, new GameOverChecker(board, history), history);
        game.startGame();
        clearBoard(board);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(7, 1), new Knight(true));
        board.setPiece(new Position(0, 1), new Knight(false));

        Move whiteOut = new Move(new Position(7, 1), new Position(5, 0));
        Move whiteBack = new Move(new Position(5, 0), new Position(7, 1));
        Move blackOut = new Move(new Position(0, 1), new Position(2, 0));
        Move blackBack = new Move(new Position(2, 0), new Position(0, 1));

        // Act: un solo ciclo (4 medias jugadas) = la posicion inicial aparece 2 veces
        game.movePiece(whiteOut);
        game.movePiece(blackOut);
        game.movePiece(whiteBack);
        PlayResult result = game.movePiece(blackBack);

        // Assert
        assertEquals(
                PlayResult.LEGAL,
                result,
                "Dos apariciones de la misma posicion no deberian ser tablas");
    }
}
