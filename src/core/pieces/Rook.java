package core.pieces;

import core.api.Piece;
import core.api.Board;
import core.models.Position;

public class Rook extends Piece {

    public Rook(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean canMove(Board board, Position start, Position end) {
        // Tiene que moverse horizontal o vertical, no en diagonal
        if (start.row() != end.row() && start.column() != end.column()) {
            return false;
        }
        // Revisar que no haya nada en el camino
        return board.isPathClear(start, end);
    }
}