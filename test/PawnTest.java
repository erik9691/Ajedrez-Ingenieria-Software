import static org.junit.jupiter.api.Assertions.*;

import main.core.flags.MoveVerdict;
import main.core.flags.PlayResult;
import main.core.game.Board;
import main.core.game.MoveHistory;
import main.core.game.pieces.Pawn;
import main.core.models.Move;
import main.core.models.MoveRecord;
import main.core.models.Position;

import org.junit.jupiter.api.Test;

public class PawnTest {

    //  Captura al paso

    @Test
    public void canMove_enPassantRightAfterTwoTileAdvance_shouldReturnEnPassant() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        Pawn whitePawn = new Pawn(true);
        Pawn blackPawn = new Pawn(false);
        Position whiteStart = new Position(3, 4);
        Position blackStart = new Position(1, 3);
        Position blackEnd = new Position(3, 3);
        Position end = new Position(2, 3);
        board.setPiece(whiteStart, whitePawn);
        board.setPiece(blackEnd, blackPawn);
        // El peon negro acaba de avanzar 2 casillas
        history.registerMove(new MoveRecord(new Move(blackStart, blackEnd), blackPawn, null, 1));

        // Act
        MoveVerdict verdict = whitePawn.canMove(board, whiteStart, end);

        // Assert
        assertEquals(
                MoveVerdict.EN_PASSANT,
                verdict,
                "Un peon al lado de otro que acaba de avanzar 2 casillas deberia devolver"
                        + " MoveVerdict.EN_PASSANT");
    }

    @Test
    public void tryMove_enPassant_shouldCaptureAdjacentPawn() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        Pawn whitePawn = new Pawn(true);
        Pawn blackPawn = new Pawn(false);
        Position whiteStart = new Position(3, 4);
        Position blackStart = new Position(1, 3);
        Position blackEnd = new Position(3, 3);
        Position end = new Position(2, 3);
        board.setPiece(whiteStart, whitePawn);
        board.setPiece(blackEnd, blackPawn);
        history.registerMove(new MoveRecord(new Move(blackStart, blackEnd), blackPawn, null, 1));

        // Act
        PlayResult result = board.tryMove(whiteStart, end);

        // Assert
        assertEquals(
                PlayResult.LEGAL, result, "La captura al paso deberia devolver PlayResult.LEGAL");
        assertSame(
                whitePawn,
                board.getPiece(end),
                "El peon blanco deberia quedar en la casilla de atras del peon negro");
        assertNull(
                board.getPiece(blackEnd),
                "El peon negro capturado deberia desaparecer del tablero");
        assertNull(
                board.getPiece(whiteStart),
                "La casilla original del peon blanco deberia quedar vacia");
    }

    @Test
    public void canMove_enPassantAfterOneTileAdvance_shouldBeInvalid() {
        // Arrange
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        Pawn whitePawn = new Pawn(true);
        Pawn blackPawn = new Pawn(false);
        Position whiteStart = new Position(3, 4);
        Position blackStart = new Position(2, 3);
        Position blackEnd = new Position(3, 3);
        Position end = new Position(2, 3);
        board.setPiece(whiteStart, whitePawn);
        board.setPiece(blackEnd, blackPawn);
        // El peon negro avanzo solo 1 casilla: no se puede capturar al paso
        history.registerMove(new MoveRecord(new Move(blackStart, blackEnd), blackPawn, null, 1));

        // Act
        MoveVerdict verdict = whitePawn.canMove(board, whiteStart, end);

        // Assert
        assertEquals(
                MoveVerdict.INVALID,
                verdict,
                "La captura al paso tras un avance de 1 casilla deberia devolver"
                        + " MoveVerdict.INVALID");
    }

    // ---------- Promocion ----------

    @Test
    public void canMove_forwardToLastRow_shouldReturnPromotion() {
        // Arrange
        Board board = new Board(new MoveHistory());
        Pawn pawn = new Pawn(true);
        Position start = new Position(1, 0);
        Position end = new Position(0, 0);
        board.setPiece(start, pawn);

        // Act
        MoveVerdict verdict = pawn.canMove(board, start, end);

        // Assert
        assertEquals(
                MoveVerdict.PROMOTION,
                verdict,
                "Un peon que llega a la ultima fila deberia devolver MoveVerdict.PROMOTION");
    }

    @Test
    public void tryMove_pawnReachesLastRow_shouldReturnPromotionPending() {
        // Arrange
        Board board = new Board(new MoveHistory());
        Pawn pawn = new Pawn(true);
        Position start = new Position(1, 0);
        Position end = new Position(0, 0);
        board.setPiece(start, pawn);

        // Act
        PlayResult result = board.tryMove(start, end);

        // Assert
        assertEquals(
                PlayResult.PROMOTION_PENDING,
                result,
                "Un peon que llega a la ultima fila deberia devolver PlayResult.PROMOTION_PENDING");
        assertSame(
                pawn,
                board.getPiece(end),
                "El peon deberia quedar en la ultima fila esperando la eleccion de la pieza");
    }
}
