import static org.junit.jupiter.api.Assertions.*;

import main.core.flags.PlayResult;
import main.core.game.Board;
import main.core.game.MoveHistory;
import main.core.game.pieces.King;
import main.core.game.pieces.Knight;
import main.core.game.pieces.Rook;
import main.core.models.Position;

import org.junit.jupiter.api.Test;

public class KingTest {

    @Test
    public void tryMove_shortCastle_shouldMoveKingAndRook() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Rook rook = new Rook(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 6);
        Position rookStart = new Position(7, 7);
        Position rookEnd = new Position(7, 5);
        board.setPiece(kingStart, king);
        board.setPiece(rookStart, rook);

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(
                PlayResult.LEGAL, result, "El enroque corto deberia devolver PlayResult.LEGAL");
        assertSame(king, board.getPiece(kingEnd), "El rey deberia terminar en la columna 6");
        assertSame(rook, board.getPiece(rookEnd), "La torre deberia terminar en la columna 5");
        assertNull(board.getPiece(kingStart), "La casilla original del rey deberia quedar vacia");
        assertNull(
                board.getPiece(rookStart), "La casilla original de la torre deberia quedar vacia");
    }

    @Test
    public void tryMove_longCastle_shouldMoveKingAndRook() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Rook rook = new Rook(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 2);
        Position rookStart = new Position(7, 0);
        Position rookEnd = new Position(7, 3);
        board.setPiece(kingStart, king);
        board.setPiece(rookStart, rook);

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(
                PlayResult.LEGAL, result, "El enroque largo deberia devolver PlayResult.LEGAL");
        assertSame(king, board.getPiece(kingEnd), "El rey deberia terminar en la columna 2");
        assertSame(rook, board.getPiece(rookEnd), "La torre deberia terminar en la columna 3");
        assertNull(board.getPiece(kingStart), "La casilla original del rey deberia quedar vacia");
        assertNull(
                board.getPiece(rookStart), "La casilla original de la torre deberia quedar vacia");
    }

    @Test
    public void tryMove_castleAfterKingMoved_shouldBeIllegal() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Rook rook = new Rook(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 6);
        board.setPiece(kingStart, king);
        board.setPiece(new Position(7, 7), rook);
        king.markAsMoved();

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(
                PlayResult.ILLEGAL,
                result,
                "Enrocar con un rey que ya se movio deberia ser ILLEGAL");
    }

    @Test
    public void tryMove_castleAfterRookMoved_shouldBeIllegal() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Rook rook = new Rook(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 6);
        board.setPiece(kingStart, king);
        board.setPiece(new Position(7, 7), rook);
        rook.markAsMoved();

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(
                PlayResult.ILLEGAL,
                result,
                "Enrocar con una torre que ya se movio deberia ser ILLEGAL");
    }

    @Test
    public void tryMove_castleWithPieceInBetween_shouldBeIllegal() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 6);
        board.setPiece(kingStart, king);
        board.setPiece(new Position(7, 7), new Rook(true));
        board.setPiece(new Position(7, 5), new Knight(true)); // Entre el rey y la torre

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(
                PlayResult.ILLEGAL,
                result,
                "Enrocar con una pieza entre el rey y la torre deberia ser ILLEGAL");
    }

    @Test
    public void tryMove_castleWhileInCheck_shouldBeIllegal() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 6);
        board.setPiece(kingStart, king);
        board.setPiece(new Position(7, 7), new Rook(true));
        board.setPiece(new Position(0, 4), new Rook(false)); // Da jaque al rey blanco

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(PlayResult.ILLEGAL, result, "Enrocar estando en jaque deberia ser ILLEGAL");
    }

    @Test
    public void tryMove_castleThroughAttackedSquare_shouldBeIllegal() {
        // Arrange
        Board board = new Board(new MoveHistory());
        King king = new King(true);
        Position kingStart = new Position(7, 4);
        Position kingEnd = new Position(7, 6);
        board.setPiece(kingStart, king);
        board.setPiece(new Position(7, 7), new Rook(true));
        board.setPiece(
                new Position(0, 5),
                new Rook(false)); // Ataca la casilla (7,5) por la que pasa el rey

        // Act
        PlayResult result = board.tryMove(kingStart, kingEnd);

        // Assert
        assertEquals(
                PlayResult.ILLEGAL,
                result,
                "Enrocar pasando por una casilla atacada deberia ser ILLEGAL");
        assertSame(
                king, board.getPiece(kingStart), "El rey deberia quedarse en su casilla original");
    }
}
