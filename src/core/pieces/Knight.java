package core.pieces;

import core.boards.Board;
import core.models.Position;

public class Knight extends Piece {

    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());
        // la posicion final debe estar a 2 posiciones horizontales y 1 vertical o 2 vertical y 1 horizontal
        return (rowDifference == 2 && columnDifference == 1) || (rowDifference == 1 && columnDifference == 2);
    }
}