package core.pieces;

import core.flags.MoveVerdict;
import core.models.Position;
import core.rules.Board;
import core.rules.Piece;


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
