package core.pieces;

import core.boards.Board;
import core.models.Piece;
import core.models.Position;
import core.models.MoveVerdict;

public class King extends Piece {

    private static final int QUEENSIDE_ROOK_COLUMN = 0;
    private static final int KINGSIDE_ROOK_COLUMN = 7;

    private boolean hasMoved;

    public King(boolean isWhite) {
        super(isWhite);
        this.hasMoved = false;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    // Lo llama el Board DESPUÉS de ejecutar el movimiento
    public void markAsMoved() {
        this.hasMoved = true;
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());

        // Movimiento normal: 1 casilla en cualquier dirección
        if (rowDifference <= 1 && columnDifference <= 1) {
            return MoveVerdict.VALID;
        }

        // Enroque: el rey se mueve 2 columnas en la misma fila
        if (rowDifference == 0 && columnDifference == 2 && canCastle(board, start, end)) {
            return MoveVerdict.CASTLE;
        }

        return MoveVerdict.INVALID;
    }

    private boolean canCastle(Board board, Position start, Position end) {
        // 1. El rey no se movió nunca
        if (hasMoved) {
            return false;
        }
        
        // 2. Ubicar la torre según el lado del enroque
        int direction = end.column() > start.column() ? 1 : -1;
        int rookColumn = direction == 1 ? KINGSIDE_ROOK_COLUMN : QUEENSIDE_ROOK_COLUMN;
        Position rookPosition = new Position(start.row(), rookColumn);

        // 3. Debe haber una torre propia, que tampoco se movió
        Piece piece = board.getPiece(rookPosition);
        if (!(piece instanceof Rook rook)
                || rook.getColor() != this.getColor()
                || rook.hasMoved()) {
            return false;
        }

        // 4. Casillas entre el rey y la torre libres
        return board.isPathClear(start, rookPosition);
    }

    @Override
    public char getSymbol() {
        return 'K';
    }
}
