package core.managers;

import core.flags.PlayResult;
import core.flags.PromotionId;
import core.models.Position;
import core.pieces.Bishop;
import core.pieces.King;
import core.pieces.Knight;
import core.pieces.Pawn;
import core.pieces.Queen;
import core.pieces.Rook;
import core.rules.Board;
import core.rules.GameOverChecker;
import core.rules.Piece;
import core.models.Move;
import ports.dto.BoardView;

// Reglas y estado de la partida. No conoce el puerto de salida:
// comunica resultados con PlayResult y ChessService decide qué mostrar.
public class GameManager {
    private boolean whiteTurn;
    private final Board board;
    private final GameOverChecker gameOverRules;
    private Position pendingPromotion;

    public GameManager(Board board, GameOverChecker gameOverChecker) {
        this.board = board;
        this.gameOverRules = gameOverChecker;
    }

    public void startGame() {
        initializeBoard();
        this.whiteTurn = true;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public PlayResult movePiece(Move move) {
        Position start = move.start();
        Position end = move.end();
        
        Piece piece = board.getPiece(start);
        if (piece == null) {
            return PlayResult.NO_PIECE;
        }
        // Valida que sea el color correcto
        if (piece.getColor() != whiteTurn) {
            return PlayResult.NOT_YOUR_TURN;
        }

        boolean isCapture = board.getPiece(end) != null;
        PlayResult result = board.tryMove(start, end);

        // Con promoción pendiente el turno se completa recién en promotePawn
        if (result == PlayResult.PROMOTION_PENDING) {
            pendingPromotion = end;
            return result;
        }

        // Cambia el turno y chequea jaques solo si la jugada fue aceptada
        if (result.isLegal()) {
            whiteTurn = !whiteTurn;
            // El contador de movimientos sin progreso vuelve a 0 con una captura o un movimiento de peon
            if((isCapture || piece instanceof Pawn))
                gameOverRules.resetHalfmoveClock();
            else
                gameOverRules.incrementHalfmoveClock();


            if (board.isChecked(whiteTurn)) 
                return PlayResult.CHECK;

            if (gameOverRules.isCheckMate(whiteTurn)) 
                return PlayResult.CHECKMATE;

            if (gameOverRules.isDraw())
                return PlayResult.DRAW;
             
            if (gameOverRules.isStalemate(whiteTurn))
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
        gameOverRules.resetHalfmoveClock();

        if (board.isChecked(whiteTurn)) 
            return PlayResult.CHECK;

        if (gameOverRules.isCheckMate(whiteTurn)) 
            return PlayResult.CHECKMATE;

        if (gameOverRules.isDraw())
            return PlayResult.DRAW;
            
        if (gameOverRules.isStalemate(whiteTurn))
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
