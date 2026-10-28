package main.core.game.pieces;

import main.core.flags.MoveVerdict;
import main.core.models.Position;
import main.core.game.Board;
import main.core.game.Piece;


public class Queen extends Piece {

    public Queen(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
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
