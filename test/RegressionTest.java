import static org.junit.jupiter.api.Assertions.*;

import main.core.flags.*;
import main.core.game.*;
import main.core.game.pieces.*;
import main.core.managers.GameManager;
import main.core.models.*;
import main.ports.dto.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegressionTest {
    final MoveHistory history = new MoveHistory();
    final Board board = new Board(history);
    final GameOverChecker checker = new GameOverChecker(board, history);
    final GameManager game = new GameManager(board, checker, history);

    static Position p(String square) {
        return new Position('8' - square.charAt(1), square.charAt(0) - 'a');
    }

    PlayResult play(String move) {
        return game.movePiece(new Move(p(move.substring(0, 2)), p(move.substring(2, 4))));
    }

    @Test
    void foolsMateEndsTheMatch() {
        game.startGame();
        play("f2f3");
        play("e7e5");
        play("g2g4");
        assertEquals(PlayResult.CHECKMATE, play("d8h4"));
    }

    @Test
    void restartClearsMiddleRanksAndPromotion() {
        game.startGame();
        play("e2e4");
        game.startGame();
        assertNull(board.getPiece(p("e4")));
        assertEquals(32, board.getPiecePositions().size());
        assertTrue(history.getNMoves(100).isEmpty());
        assertTrue(game.isWhiteTurn());
    }

    @Test
    void promotionMustFinishBeforeAnotherMove() {
        board.setPiece(p("e1"), new King(true));
        board.setPiece(p("a6"), new King(false));
        // Inicializa turno y luego prepara una posicion reducida.
        game.startGame();
        clear();
        board.setPiece(p("e1"), new King(true));
        board.setPiece(p("a6"), new King(false));
        board.setPiece(p("e7"), new Pawn(true));
        assertEquals(PlayResult.PROMOTION_PENDING, play("e7e8"));
        assertEquals(PlayResult.PROMOTION_PENDING, play("e1d1"));
        assertNotNull(board.getPiece(p("e1")));
    }

    @Test
    void nullPromotionIsRejectedWithoutCrashing() {
        game.startGame();
        clear();
        board.setPiece(p("e1"), new King(true));
        board.setPiece(p("a6"), new King(false));
        board.setPiece(p("e7"), new Pawn(true));
        play("e7e8");
        assertEquals(PlayResult.INVALID_PROMOTION, game.promotePawn(null));
        assertTrue(game.isPromotionPending());
    }

    @Test
    void checkmateSearchDoesNotMarkKingAsMoved() {
        King king = new King(true);
        board.setPiece(p("e1"), king);
        board.setPiece(p("h1"), new Rook(true));
        board.setPiece(p("a8"), new King(false));
        board.setPiece(p("e8"), new Rook(false));
        assertFalse(checker.isCheckMate(true));
        assertFalse(king.hasMoved());
        assertEquals(p("e1"), board.findKingPosition(true));
    }

    @Test
    void castleMarksBothPiecesAsMoved() {
        King king = new King(true);
        Rook rook = new Rook(true);
        board.setPiece(p("e1"), king);
        board.setPiece(p("h1"), rook);
        assertEquals(PlayResult.LEGAL, board.tryMove(p("e1"), p("g1")));
        assertTrue(king.hasMoved());
        assertTrue(rook.hasMoved());
    }

    @Test
    void castleCannotStartFromAnotherFile() {
        board.setPiece(p("d1"), new King(true));
        board.setPiece(p("h1"), new Rook(true));
        assertEquals(PlayResult.ILLEGAL, board.tryMove(p("d1"), p("f1")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void pawnChecksOnPromotionRank(boolean white) {
        board.setPiece(p(white ? "e7" : "e2"), new Pawn(white));
        board.setPiece(p(white ? "f8" : "f1"), new King(!white));
        assertTrue(board.isChecked(!white));
    }

    @Test
    void enemyKingCannotBeCaptured() {
        board.setPiece(p("a1"), new King(true));
        board.setPiece(p("h8"), new King(false));
        board.setPiece(p("h1"), new Rook(true));
        assertEquals(PlayResult.ILLEGAL, board.tryMove(p("h1"), p("h8")));
        assertInstanceOf(King.class, board.getPiece(p("h8")));
    }

    @Test
    void invalidPathDoesNotAccessOutsideArray() {
        assertFalse(board.isPathClear(p("a1"), p("c2")));
    }

    @Test
    void boardViewCopiesConstructorArray() {
        PieceView[][] cells = new PieceView[8][8];
        cells[0][0] = new PieceView('R', true);
        BoardView view = new BoardView(cells);
        cells[0][0] = null;
        assertNotNull(view.pieceAt(0, 0));
    }

    @Test
    void boardViewCopiesReturnedArray() {
        board.setPiece(p("a1"), new Rook(true));
        BoardView view = board.toView();
        view.cells()[7][0] = null;
        assertNotNull(view.pieceAt(7, 0));
    }

    @Test
    void pieceMapCannotBeMutatedFromOutside() {
        board.setPiece(p("a1"), new Rook(true));
        assertThrows(UnsupportedOperationException.class, () -> board.getPiecePositions().clear());
    }

    @Test
    void setPieceMovesAnExistingReferenceWithoutDuplicatingIt() {
        Rook r = new Rook(true);
        board.setPiece(p("a1"), r);
        board.setPiece(p("a2"), r);
        assertNull(board.getPiece(p("a1")));
        assertSame(r, board.getPiece(p("a2")));
        assertEquals(1, board.getPiecePositions().size());
    }

    @Test
    void nullMoveIsRejected() {
        game.startGame();
        assertEquals(PlayResult.ILLEGAL, game.movePiece(null));
    }

    @Test
    void enPassantCannotExposeRookCheck() {
        board.setPiece(p("a5"), new King(true));
        board.setPiece(p("e5"), new Pawn(true));
        Pawn black = new Pawn(false);
        board.setPiece(p("d5"), black);
        board.setPiece(p("h5"), new Rook(false));
        history.registerMove(new MoveRecord(new Move(p("d7"), p("d5")), black, null, 1));
        assertEquals(PlayResult.SELF_MATE, board.tryMove(p("e5"), p("d6")));
        assertSame(black, board.getPiece(p("d5")));
        assertInstanceOf(Pawn.class, board.getPiece(p("e5")));
    }

    void clear() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) board.setPiece(new Position(r, c), null);
    }
}
