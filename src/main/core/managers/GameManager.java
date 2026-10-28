package main.core.managers;

import main.core.flags.PlayResult;
import main.core.flags.PromotionId;
import main.core.game.MoveHistory;
import main.core.models.Position;
import main.core.game.pieces.Bishop;
import main.core.game.pieces.King;
import main.core.game.pieces.Knight;
import main.core.game.pieces.Pawn;
import main.core.game.pieces.Queen;
import main.core.game.pieces.Rook;
import main.core.game.Board;
import main.core.game.GameOverChecker;
import main.core.game.Piece;
import main.core.models.Move;
import main.core.models.MoveRecord;
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

    public GameManager(Board board, GameOverChecker gameOverChecker, MoveHistory moveHistory) {
        this.board = board;
        this.endRules = gameOverChecker;
        this.history = moveHistory;
        this.moveCount = 0;
    }

    public void startGame() {
        initializeBoard();
        this.whiteTurn = true;
        // Partida nueva: historial y contadores de la anterior no deben quedar
        history.clear();
        this.moveCount = 0;
        endRules.resetHalfmoveClock();
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public PlayResult movePiece(Move move) {
        Position start = move.start();
        Position end = move.end();
        
        Piece piece = board.getPiece(start);
        Piece captured = board.getPiece(end);
        if (piece == null) {
            return PlayResult.NO_PIECE;
        }
        // Valida que sea el color correcto
        if (piece.getColor() != whiteTurn) {
            return PlayResult.NOT_YOUR_TURN;
        }

        PlayResult result = board.tryMove(start, end);





        // Cambia el turno y chequea jaques solo si la jugada fue aceptada
        if (result.isLegal()) {
            moveCount++;
            history.registerMove(new MoveRecord(move, piece, captured, moveCount));
        
            // Con promoción pendiente el turno se completa recién en promotePawn
            if (result == PlayResult.PROMOTION_PENDING) {
                pendingPromotion = end;
                return result;
            }

            whiteTurn = !whiteTurn;
            // El contador de movimientos sin progreso vuelve a 0 con una captura o un movimiento de peon
            if((captured != null || piece instanceof Pawn))
                endRules.resetHalfmoveClock();
            else
                endRules.incrementHalfmoveClock();


            if (board.isChecked(whiteTurn)) 
                return PlayResult.CHECK;

            if (endRules.isCheckMate(whiteTurn)) 
                return PlayResult.CHECKMATE;

            if (endRules.isDraw())
                return PlayResult.DRAW;
             
            if (endRules.isStalemate(whiteTurn))
                return PlayResult.STALEMATE;

        }

        return result;
    }

    // Informa si hay una promoción esperando la elección de pieza
    public boolean isPromotionPending() {
        return pendingPromotion != null;
    }

    // Completa la promoción pendiente y continúa el flujo del turno
    public PlayResult promotePawn(PromotionId choice) {
        if (pendingPromotion == null) {
            return PlayResult.INVALID_PROMOTION;
        }
        Piece pawn = board.getPiece(pendingPromotion);
        Piece promoted = switch (choice) {
            case QUEEN -> new Queen(pawn.getColor());
            case ROOK -> new Rook(pawn.getColor());
            case BISHOP -> new Bishop(pawn.getColor());
            case KNIGHT -> new Knight(pawn.getColor());
        };
        board.setPiece(pendingPromotion, promoted);
        pendingPromotion = null;

        // Cambia el turno
        whiteTurn = !whiteTurn;

        //Promoción siempre reinicia el contador
        endRules.resetHalfmoveClock();

        if (board.isChecked(whiteTurn)) 
            return PlayResult.CHECK;

        if (endRules.isCheckMate(whiteTurn)) 
            return PlayResult.CHECKMATE;

        if (endRules.isDraw())
            return PlayResult.DRAW;
            
        if (endRules.isStalemate(whiteTurn))
            return PlayResult.STALEMATE;

        return PlayResult.LEGAL;
    }

    // true si el jugador al que le toca mover está en jaque
    public boolean currentTurnInCheck() {
        return board.isChecked(whiteTurn);
    }

    public BoardView getBoardView() {
        return board.toView();
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
            board.setPiece(
                new Position(1, column),
                new Pawn(false)
            );
        }

        //Piezas blancas
        // Segunda fila
        for (int column = 0; column < 8; column++) {
            board.setPiece(
                new Position(6, column),
                new Pawn(true)
            );
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
