package core.managers;

import core.boards.Board;
import core.models.Piece;
import core.models.Position;
import core.pieces.Bishop;
import core.pieces.King;
import core.pieces.Knight;
import core.pieces.Pawn;
import core.pieces.Queen;
import core.pieces.Rook;
import core.models.MoveResult;
import ports.BoardView;
import ports.DisplayPort;
import ports.GameInputPort;
import ports.PlayResult;
import ports.PieceView;

public class GameManager implements GameInputPort{
    private boolean whiteTurn;
    private Board board;
    private DisplayPort displayPort;
    private Position pendingPromotion;

    public GameManager(Board board, DisplayPort displayPort) {
        this.board = board;
        this.displayPort = displayPort;
    }
    
    @Override 
    public void startGame() {
        initializeBoard();
        this.whiteTurn = true;
        
        // Notificaciones iniciales
        displayPort.displayMessage("¡Comienza la partida!");
        displayPort.displayBoard(toView());
        displayPort.displayTurn(whiteTurn);
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    @Override
    public boolean movePiece(int fromRow, int fromColumn, int toRow, int toColumn) {
        Position start = new Position(fromRow, fromColumn);
        Position end = new Position(toRow, toColumn);
        Piece piece = board.getPiece(start);
        
        // Valida que sea el color correcto
        if (piece.getColor() != whiteTurn) {
            return reject(PlayResult.NOT_YOUR_TURN);
        }

        MoveResult result = board.tryMove(start, end);
        // Intenta el movimiento
        // Solo se contemplan movimientos legales y promociones. Otros resultados quedan pendientes.
        if (result != MoveResult.LEGAL && result != MoveResult.PROMOTION_PENDING) {
            return reject(PlayResult.ILLEGAL_MOVE);
        }

        // Con promoción pendiente el turno se completa recién en promotePawn
        if (result == MoveResult.PROMOTION_PENDING) {
            pendingPromotion = end;
            displayPort.displayBoard(toView());
            return true;
        }

        // Cambia el turno
        whiteTurn = !whiteTurn;

        // Actualiza el tablero en pantalla tras un movimiento exitoso
        displayPort.displayBoard(toView());

        // Verifica si el movimiento causó el fin del juego
        if (isGameOver()) {
            displayPort.displayMessage("¡Fin del juego! Jaque mate.");
        } else {
            // Si el juego continúa, muestra el nuevo turno
            displayPort.displayTurn(whiteTurn);
            // Advertencia si el jugador que sigue está en jaque
            if (board.isChecked(whiteTurn)) {
                displayPort.displayMessage("¡Jaque!");
            }
        }

        return true;
    }

    // Informa si hay una promoción esperando la elección de pieza
    @Override
    public boolean isPromotionPending() {
        return pendingPromotion != null;
    }

    // Completa la promoción pendiente y continúa el flujo del turno
    @Override
    public boolean promotePawn(char choice) {
        if (pendingPromotion == null) {
            return false;
        }
        Piece pawn = board.getPiece(pendingPromotion);
        Piece promoted;
        switch (Character.toUpperCase(choice)) {
            case 'Q':
                promoted = new Queen(pawn.getColor());
                break;
            case 'R':
                promoted = new Rook(pawn.getColor());
                break;
            case 'B':
                promoted = new Bishop(pawn.getColor());
                break;
            case 'N':
                promoted = new Knight(pawn.getColor());
                break;
            default:
                // Elección inválida: la promoción sigue pendiente para reintentar
                return reject(PlayResult.INVALID_PROMOTION);
        }
        board.setPiece(pendingPromotion, promoted);
        pendingPromotion = null;

        // Cambia el turno
        whiteTurn = !whiteTurn;

        // Actualiza el tablero en pantalla tras un movimiento exitoso
        displayPort.displayBoard(toView());

        // Verifica si el movimiento causó el fin del juego
        if (isGameOver()) {
            displayPort.displayMessage("¡Fin del juego! Jaque mate.");
        } else {
            // Si el juego continúa, muestra el nuevo turno
            displayPort.displayTurn(whiteTurn);
            // Advertencia si el jugador que sigue está en jaque
            if (board.isChecked(whiteTurn)) {
                displayPort.displayMessage("¡Jaque!");
            }
        }

        return true;
    }

    // Informa el rechazo por el puerto de salida y devuelve false
    private boolean reject(PlayResult reason) {
        displayPort.displayMoveRejected(reason);
        return false;
    }

    @Override
    public boolean isGameOver() {
        return board.isCheckmated(whiteTurn);
    }

    // Convierte el tablero del dominio en una vista de solo lectura para los adaptadores.
    private BoardView toView() {
        PieceView[][] cells = new PieceView[BoardView.SIZE][BoardView.SIZE];
        for (int row = 0; row < BoardView.SIZE; row++) {
            for (int column = 0; column < BoardView.SIZE; column++) {
                Piece piece = board.getPiece(new Position(row, column));
                if (piece != null) {
                    cells[row][column] = new PieceView(piece.getSymbol(), piece.getColor());
                }
            }
        }
        return new BoardView(cells);
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