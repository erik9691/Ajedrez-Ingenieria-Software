package core.managers;

import core.api.Piece;
import core.api.Board;
import core.api.GameManager;
import core.models.Position;
import core.pieces.Bishop;
import core.pieces.King;
import core.pieces.Knight;
import core.pieces.Pawn;
import core.pieces.Queen;
import core.pieces.Rook;

public class ClassicGameManager extends GameManager {
    private boolean whiteTurn;
    private Board board;

    public ClassicGameManager(Board board) {
        super(board);
    }
    
    public void startGame() {
        initializeBoard();
        this.whiteTurn = true;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public boolean makeMove(Position start, Position end) {
        Piece piece = board.getPiece(start);
        // Valida que haya elegido una pieza
        if (piece == null) {
            return false;
        }
        // Valida que sea el color correcto
        if (piece.getColor() != whiteTurn) {
            return false;
        }
        // Intenta el movimiento
        if (!board.tryMove(start, end)) {
            return false;
        }
        // Cambia el turno
        whiteTurn = !whiteTurn;

        return true;
    }

    //Logica provisoria 
    public boolean endGame() {
        return board.isCheckmate(whiteTurn);
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
