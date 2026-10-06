package core.pieces;

import core.api.Piece;
import core.api.Board;
import core.models.Position;

public class King extends Piece {

    public King(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());
        // El destino tiene que estar a 1 de horizontal y/o 1 de vertical
        return rowDifference <= 1 && columnDifference <= 1;
    }
}