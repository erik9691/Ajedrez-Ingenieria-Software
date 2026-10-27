package core.pieces;

import core.flags.MoveVerdict;
import core.models.Position;
import core.rules.Board;
import core.rules.Piece;

public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());
        
        // Tiene que moverse en diagonal
        if (rowDifference != columnDifference) {
            return MoveVerdict.INVALID;
        }
        return (board.isPathClear(start, end)) ? MoveVerdict.VALID : MoveVerdict.INVALID;
    }

    @Override
    public char getSymbol() {
        return 'B';
    }
}
