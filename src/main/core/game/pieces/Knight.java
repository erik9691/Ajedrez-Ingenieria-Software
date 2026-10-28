package main.core.game.pieces;

import main.core.flags.MoveVerdict;
import main.core.models.Position;
import main.core.game.Board;
import main.core.game.Piece;


public class Knight extends Piece {

    public Knight(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());
        // la posicion final debe estar a 2 posiciones horizontales y 1 vertical o 2 vertical y 1 horizontal
        if ((rowDifference == 2 && columnDifference == 1) || (rowDifference == 1 && columnDifference == 2)) {
            return MoveVerdict.VALID;
        }
        return MoveVerdict.INVALID;
    }

    @Override
    public char getSymbol() {
        return 'N';
    }
}
