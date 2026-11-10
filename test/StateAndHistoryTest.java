import static org.junit.jupiter.api.Assertions.*;

import main.core.flags.*;
import main.core.game.*;
import main.core.game.pieces.*;
import main.core.managers.GameManager;
import main.core.models.*;
import main.ports.dto.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;

class StateAndHistoryTest {
    final MoveHistory history = new MoveHistory();
    final Board board = new Board(history);
    final GameManager game = new GameManager(board, new GameOverChecker(board, history), history);

    static Position p(String s) {
        return RegressionTest.p(s);
    }

    PlayResult play(String move) {
        return game.movePiece(ServiceTest.ok(move).move());
    }

    void clear() {
        board.clear();
    }

    void foolsMateEndsTheMatch() {
        game.startGame();
        play("f2f3");
        play("e7e5");
        play("g2g4");
        assertEquals(PlayResult.CHECKMATE, play("d8h4"));
    }

    @Test
    void historyReturnsChronologicalTailAndIndependentList() {
        game.startGame();
        play("e2e4");
        play("e7e5");
        play("g1f3");
        var moves = history.getNMoves(2);
        assertEquals(2, moves.get(0).moveNum());
        assertEquals(3, moves.get(1).moveNum());
        moves.clear();
        assertEquals(3, history.getNMoves(100).size());
        assertTrue(history.getNMoves(0).isEmpty());
    }

    @Test
    void historyRejectsNegativeCount() {
        assertThrows(IllegalArgumentException.class, () -> history.getNMoves(-1));
    }

    @Test
    void enPassantHistoryRecordsCapturedPawn() {
        game.startGame();
        play("e2e4");
        play("a7a6");
        play("e4e5");
        play("d7d5");
        Piece captured = board.getPiece(p("d5"));
        assertEquals(PlayResult.LEGAL, play("e5d6"));
        assertSame(captured, history.getNMoves(1).get(0).capturedPiece());
        assertNull(board.getPiece(p("d5")));
    }

    @Test
    void enPassantExpiresAfterOneReply() {
        game.startGame();
        play("e2e4");
        play("a7a6");
        play("e4e5");
        play("d7d5");
        play("g1f3");
        play("a6a5");
        assertEquals(PlayResult.ILLEGAL, play("e5d6"));
    }

    @Test
    void lostCastlingRightsPreventFalseRepetition() {
        game.startGame();
        clear();
        board.setPiece(p("e1"), new King(true));
        board.setPiece(p("e8"), new King(false));
        board.setPiece(p("a1"), new Rook(true));
        board.setPiece(p("a8"), new Rook(false));
        String initial = board.repetitionKey(true);
        for (int i = 0; i < 2; i++) {
            assertEquals(PlayResult.LEGAL, play("a1a2"));
            assertEquals(PlayResult.LEGAL, play("a8a7"));
            assertEquals(PlayResult.LEGAL, play("a2a1"));
            assertEquals(PlayResult.LEGAL, play("a7a8"));
        }
        assertNotEquals(initial, board.repetitionKey(true));
        assertEquals(PlayResult.LEGAL, play("a1a2"));
        assertEquals(PlayResult.DRAW, play("a8a7"));
    }

    @Test
    void irrelevantEnPassantTargetDoesNotChangeRepetitionKey() {
        game.startGame();
        play("e2e4");
        String afterPawn = board.repetitionKey(false);
        play("g8f6");
        play("g1f3");
        play("f6g8");
        play("f3g1");
        assertEquals(afterPawn, board.repetitionKey(false));
    }

    @Test
    void legalEnPassantTargetDoesChangeRepetitionKey() {
        game.startGame();
        play("e2e4");
        play("a7a6");
        play("e4e5");
        play("d7d5");
        String ep = board.repetitionKey(true);
        play("g1f3");
        play("g8f6");
        play("f3g1");
        play("f6g8");
        assertNotEquals(ep, board.repetitionKey(true));
    }

    @Test
    void finalMatchRejectsFurtherMovesWithoutChangingBoard() {
        foolsMateEndsTheMatch();
        BoardView before = game.getBoardView();
        assertEquals(PlayResult.CHECKMATE, play("a2a3"));
        assertArrayEquals(before.cells(), game.getBoardView().cells());
    }

    @Test
    void restartClearsPendingPromotion() {
        game.startGame();
        clear();
        board.setPiece(p("e1"), new King(true));
        board.setPiece(p("a6"), new King(false));
        board.setPiece(p("e7"), new Pawn(true));
        play("e7e8");
        game.startGame();
        assertFalse(game.isPromotionPending());
        assertEquals(PlayResult.LEGAL, play("e2e4"));
    }

    @ParameterizedTest
    @EnumSource(PromotionId.class)
    void allPromotionsWorkAndKeepColor(PromotionId promotion) {
        game.startGame();
        clear();
        board.setPiece(p("e1"), new King(true));
        board.setPiece(p("a6"), new King(false));
        board.setPiece(p("e7"), new Pawn(true));
        assertEquals(PlayResult.PROMOTION_PENDING, play("e7e8"));
        assertTrue(game.promotePawn(promotion).isLegal());
        assertFalse(game.isPromotionPending());
        assertTrue(board.getPiece(p("e8")).getColor());
        assertFalse(game.isWhiteTurn());
    }

    @Test
    void viewLegalMovesAreImmutable() {
        game.startGame();
        assertThrows(
                UnsupportedOperationException.class,
                () -> game.getBoardView().legalMoves().clear());
    }

    @Test
    void oldViewDoesNotChangeAfterMove() {
        game.startGame();
        BoardView old = game.getBoardView();
        play("e2e4");
        assertNotNull(old.pieceAt(6, 4));
        assertNull(old.pieceAt(4, 4));
    }

    @Test
    void nullBoardPositionsAreHandled() {
        assertNull(board.getPiece(null));
        assertEquals(PlayResult.OUT_OF_BOUNDS, board.tryMove(null, p("a1")));
        assertThrows(IllegalArgumentException.class, () -> board.setPiece(null, new Pawn(true)));
    }
}
