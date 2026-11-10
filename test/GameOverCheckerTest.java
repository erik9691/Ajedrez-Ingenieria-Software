import static org.junit.jupiter.api.Assertions.*;

import main.core.game.Board;
import main.core.game.GameOverChecker;
import main.core.game.MoveHistory;
import main.core.game.pieces.Bishop;
import main.core.game.pieces.King;
import main.core.game.pieces.Knight;
import main.core.game.pieces.Pawn;
import main.core.game.pieces.Queen;
import main.core.game.pieces.Rook;
import main.core.models.Position;

import org.junit.jupiter.api.Test;

public class GameOverCheckerTest {

    // Jaque mate

    @Test
    public void isCheckMate_backRankMate_shouldBeTrue() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(0, 7), new King(false));
        board.setPiece(new Position(1, 6), new Pawn(false));
        board.setPiece(new Position(1, 7), new Pawn(false));
        board.setPiece(new Position(0, 0), new Rook(true)); // Jaque por la fila 0
        board.setPiece(new Position(7, 4), new King(true));

        // Act
        boolean checkMate = checker.isCheckMate(false);

        // Assert
        assertTrue(
                checkMate,
                "Un rey en jaque sin escape, captura ni bloqueo deberia estar en jaque mate");
    }

    @Test
    public void isCheckMate_kingCanEscape_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(0, 7), new King(false));
        board.setPiece(
                new Position(1, 6), new Pawn(false)); // Sin peon en (1,7): el rey puede escapar
        board.setPiece(new Position(0, 0), new Rook(true));
        board.setPiece(new Position(7, 4), new King(true));

        // Act
        boolean checkMate = checker.isCheckMate(false);

        // Assert
        assertFalse(checkMate, "Un rey en jaque que puede escapar no deberia estar en jaque mate");
    }

    @Test
    public void isCheckMate_kingNotInCheck_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(0, 7), new King(false));
        board.setPiece(new Position(7, 4), new King(true));

        // Act
        boolean checkMate = checker.isCheckMate(false);

        // Assert
        assertFalse(checkMate, "Un rey que no esta en jaque no puede estar en jaque mate");
    }

    // Ahogado

    @Test
    public void isStalemate_noLegalMovesAndNotInCheck_shouldBeTrue() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(0, 0), new King(false));
        board.setPiece(new Position(2, 1), new King(true));
        board.setPiece(
                new Position(1, 2),
                new Queen(true)); // No da jaque pero cubre todas las casillas del rey negro

        // Act
        boolean stalemate = checker.isStalemate(false);

        // Assert
        assertTrue(stalemate, "Sin jugadas legales y sin estar en jaque deberia ser ahogado");
        assertFalse(board.isChecked(false), "El rey negro no deberia estar en jaque en un ahogado");
    }

    @Test
    public void isStalemate_kingInCheck_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(0, 7), new King(false));
        board.setPiece(new Position(1, 6), new Pawn(false));
        board.setPiece(new Position(1, 7), new Pawn(false));
        board.setPiece(new Position(0, 0), new Rook(true)); // Jaque mate, no ahogado
        board.setPiece(new Position(7, 4), new King(true));

        // Act
        boolean stalemate = checker.isStalemate(false);

        // Assert
        assertFalse(stalemate, "Un rey en jaque no puede estar ahogado");
    }

    @Test
    public void isStalemate_hasLegalMove_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(0, 0), new King(false));
        board.setPiece(new Position(2, 1), new King(true));
        board.setPiece(
                new Position(7, 7), new Queen(true)); // Lejos: el rey negro tiene casillas libres

        // Act
        boolean stalemate = checker.isStalemate(false);

        // Assert
        assertFalse(stalemate, "Un jugador con jugadas legales no esta ahogado");
    }

    // Tablas: material insuficiente

    @Test
    public void isDraw_kingVsKing_shouldBeTrue() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));

        // Act
        boolean draw = checker.isDraw();

        // Assert
        assertTrue(draw, "Rey contra rey deberia ser tablas por material insuficiente");
    }

    @Test
    public void isInsufficientMaterial_kingAndKnightVsKing_shouldBeTrue() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(7, 1), new Knight(true));
        board.setPiece(new Position(0, 4), new King(false));

        // Act
        boolean insufficient = checker.isInsufficientMaterial();

        // Assert
        assertTrue(insufficient, "Rey y caballo contra rey deberia ser material insuficiente");
    }

    @Test
    public void isInsufficientMaterial_kingAndRookVsKing_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(7, 0), new Rook(true));
        board.setPiece(new Position(0, 4), new King(false));

        // Act
        boolean insufficient = checker.isInsufficientMaterial();

        // Assert
        assertFalse(
                insufficient, "Rey y torre contra rey es material suficiente para dar jaque mate");
    }

    @Test
    public void isInsufficientMaterial_oppositeBishopsSameSquareColor_shouldBeTrue() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(4, 4), new Bishop(true)); // (4+4) par -> casilla clara
        board.setPiece(new Position(1, 1), new Bishop(false)); // (1+1) par -> casilla clara

        // Act
        boolean insufficient = checker.isInsufficientMaterial();

        // Assert
        assertTrue(
                insufficient,
                "Alfiles de bandos opuestos en casillas del mismo color no pueden forzar jaque"
                        + " mate");
    }

    //  Tablas: regla de los 50 movimientos

    @Test
    public void isDraw_fiftyMovesWithoutProgress_shouldBeTrue() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        // Con una torre en juego descartamos tablas por material insuficiente
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(7, 0), new Rook(true));

        // Act: 100 medias jugadas = 50 movimientos de cada bando
        for (int i = 0; i < 100; i++) {
            checker.incrementHalfmoveClock();
        }

        // Assert
        assertTrue(
                checker.isDraw(),
                "100 medias jugadas sin captura ni movimiento de peon deberian ser tablas");
    }

    @Test
    public void isDraw_lessThanFiftyMoves_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(7, 0), new Rook(true));

        // Act
        for (int i = 0; i < 99; i++) {
            checker.incrementHalfmoveClock();
        }

        // Assert
        assertFalse(
                checker.isDraw(), "99 medias jugadas sin progreso todavia no deberian ser tablas");
    }

    @Test
    public void isDraw_clockResetAfterProgress_shouldBeFalse() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(7, 0), new Rook(true));
        for (int i = 0; i < 99; i++) {
            checker.incrementHalfmoveClock();
        }

        // Act: una captura o un movimiento de peon reinicia el contador
        checker.resetHalfmoveClock();
        checker.incrementHalfmoveClock();

        // Assert
        assertFalse(
                checker.isDraw(),
                "Reiniciar el contador deberia reiniciar la cuenta de los 50 movimientos");
    }
}
