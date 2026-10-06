package core.pieces;

import core.api.Piece;
import core.api.Board;
import core.models.Position;

public class Queen extends Piece {

    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());

        boolean straight = start.row() == end.row() || start.column() == end.column();

        boolean diagonal = rowDifference == columnDifference;
        // Combina logica de Rook y Bishop
        if (!straight && !diagonal) {
            return false;
        }

        return board.isPathClear(start, end);
    }
}