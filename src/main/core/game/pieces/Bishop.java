package main.core.game.pieces;

import main.core.flags.MoveVerdict;
import main.core.game.Board;
import main.core.game.Piece;
import main.core.models.Position;

public class Bishop extends Piece {

    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
        if (!Board.isValidPosition(start) || !Board.isValidPosition(end) || start.equals(end))
            return MoveVerdict.INVALID;
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
