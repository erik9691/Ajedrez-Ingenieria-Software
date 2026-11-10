package main.core.game.pieces;

import main.core.flags.MoveVerdict;
import main.core.game.Board;
import main.core.game.Piece;
import main.core.models.Position;

public class Queen extends Piece {

    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
        if (!Board.isValidPosition(start) || !Board.isValidPosition(end) || start.equals(end))
            return MoveVerdict.INVALID;
        int rowDifference = Math.abs(end.row() - start.row());
        int columnDifference = Math.abs(end.column() - start.column());

        boolean straight = start.row() == end.row() || start.column() == end.column();

        boolean diagonal = rowDifference == columnDifference;

        // Combina logica de Rook y Bishop
        if (!straight && !diagonal) {
            return MoveVerdict.INVALID;
        }

        return (board.isPathClear(start, end)) ? MoveVerdict.VALID : MoveVerdict.INVALID;
    }

    @Override
    public char getSymbol() {
        return 'Q';
    }
}
