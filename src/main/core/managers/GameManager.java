package main.core.managers;

import main.core.flags.PlayResult;
import main.core.flags.PromotionId;
import main.core.game.Board;
import main.core.game.GameOverChecker;
import main.core.game.MoveHistory;
import main.core.game.Piece;
import main.core.game.pieces.Bishop;
import main.core.game.pieces.King;
import main.core.game.pieces.Knight;
import main.core.game.pieces.Pawn;
import main.core.game.pieces.Queen;
import main.core.game.pieces.Rook;
import main.core.models.Move;
import main.core.models.MoveRecord;
import main.core.models.Position;
import main.ports.dto.BoardView;

// Reglas y estado de la partida. No conoce el puerto de salida:
// comunica resultados con PlayResult y ChessService decide qué mostrar.
public class GameManager {
    private boolean whiteTurn;
    private final Board board;
    private final GameOverChecker endRules;
    private Position pendingPromotion;
    private final MoveHistory history;
    private int moveCount;
    private PlayResult lastResult = PlayResult.NONE;

    public GameManager(Board board, GameOverChecker gameOverChecker, MoveHistory moveHistory) {
        this.board = board;
        this.endRules = gameOverChecker;
        this.history = moveHistory;
        this.moveCount = 0;
    }

    public void startGame() {
        board.clear();
        pendingPromotion = null;
        lastResult = PlayResult.NONE;
        initializeBoard();
        this.whiteTurn = true;

        // Partida nueva: historial y contadores de la anterior se reinicia
        history.clear();
        this.moveCount = 0;
        endRules.resetHalfmoveClock();
        endRules.resetPositions();
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public PlayResult movePiece(Move move) {
        if (lastResult.isFinal()) return lastResult;
        if (pendingPromotion != null) return PlayResult.PROMOTION_PENDING;
        if (move == null) return PlayResult.ILLEGAL;
        if (!Board.isValidPosition(move.start()) || !Board.isValidPosition(move.end()))
            return PlayResult.OUT_OF_BOUNDS;
        Piece piece = board.getPiece(move.start());
        if (piece == null) return PlayResult.NO_PIECE;
        if (piece.getColor() != whiteTurn) return PlayResult.NOT_YOUR_TURN;
        Piece captured = board.getPiece(move.end());
        if (piece instanceof Pawn
                && captured == null
                && move.start().column() != move.end().column())
            captured = board.getPiece(new Position(move.start().row(), move.end().column()));
        if (moveCount == 0) {
            endRules.resetPositions();
            endRules.recordPosition(whiteTurn);
        }
        PlayResult result = board.tryMove(move.start(), move.end());
        if (!result.isLegal()) return result;
        history.registerMove(new MoveRecord(move, piece, captured, ++moveCount));
        if (piece instanceof Pawn || captured != null) endRules.resetHalfmoveClock();
        else endRules.incrementHalfmoveClock();
        if (result == PlayResult.PROMOTION_PENDING) {
            pendingPromotion = move.end();
            return result;
        }
        return finishTurn();
    }

    private PlayResult finishTurn() {
        whiteTurn = !whiteTurn;
        endRules.recordPosition(whiteTurn);
        if (endRules.isCheckMate(whiteTurn)) lastResult = PlayResult.CHECKMATE;
        else if (endRules.isStalemate(whiteTurn)) lastResult = PlayResult.STALEMATE;
        else if (endRules.isDraw()) lastResult = PlayResult.DRAW;
        else lastResult = board.isChecked(whiteTurn) ? PlayResult.CHECK : PlayResult.LEGAL;
        return lastResult;
    }

    // Informa si hay una promoción esperando la elección de pieza
    public boolean isPromotionPending() {
        return pendingPromotion != null;
    }

    // Completa la promoción pendiente y continúa el flujo del turno
    public PlayResult promotePawn(PromotionId choice) {

        if (pendingPromotion == null || choice == null) {
            return PlayResult.INVALID_PROMOTION;
        }

        Piece pawn = board.getPiece(pendingPromotion);

        // Crea la pieza elegida por el jugador
        Piece promoted =
                switch (choice) {
                    case QUEEN -> new Queen(pawn.getColor());
                    case ROOK -> new Rook(pawn.getColor());
                    case BISHOP -> new Bishop(pawn.getColor());
                    case KNIGHT -> new Knight(pawn.getColor());
                };

        board.setPiece(pendingPromotion, promoted);
        pendingPromotion = null;

        return finishTurn();
    }

    // true si el jugador al que le toca mover está en jaque
    public boolean currentTurnInCheck() {
        return board.isChecked(whiteTurn);
    }

    public BoardView getBoardView() {
        return new BoardView(
                board.toView().cells(),
                whiteTurn,
                board.castlingRights(),
                board.enPassantTarget(),
                endRules.halfmoveClock(),
                moveCount / 2 + 1,
                pendingPromotion == null && !lastResult.isFinal()
                        ? board.legalMoves(whiteTurn)
                        : java.util.List.of());
    }

    private void initializeBoard() {
        // Piezas negras
        // Primera fila
        board.setPiece(new Position(0, 0), new Rook(false));
        board.setPiece(new Position(0, 1), new Knight(false));
        board.setPiece(new Position(0, 2), new Bishop(false));
        board.setPiece(new Position(0, 3), new Queen(false));
        board.setPiece(new Position(0, 4), new King(false));
        board.setPiece(new Position(0, 5), new Bishop(false));
        board.setPiece(new Position(0, 6), new Knight(false));
        board.setPiece(new Position(0, 7), new Rook(false));
        // Segunda fila
        for (int column = 0; column < 8; column++) {
            board.setPiece(new Position(1, column), new Pawn(false));
        }

        // Piezas blancas
        // Segunda fila
        for (int column = 0; column < 8; column++) {
            board.setPiece(new Position(6, column), new Pawn(true));
        }
        // Primera fila
        board.setPiece(new Position(7, 0), new Rook(true));
        board.setPiece(new Position(7, 1), new Knight(true));
        board.setPiece(new Position(7, 2), new Bishop(true));
        board.setPiece(new Position(7, 3), new Queen(true));
        board.setPiece(new Position(7, 4), new King(true));
        board.setPiece(new Position(7, 5), new Bishop(true));
        board.setPiece(new Position(7, 6), new Knight(true));
        board.setPiece(new Position(7, 7), new Rook(true));
    }
}
