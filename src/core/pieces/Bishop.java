package core.pieces;

import core.api.Piece;
import core.api.Board;
import core.models.Position;

public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());
        // Tiene que moverse en diagonal
        if (rowDifference != columnDifference) {
            return false;
        }
        return board.isPathClear(start, end);
    }
}