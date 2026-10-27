package core.pieces;

import core.flags.MoveVerdict;
import core.models.Position;
import core.rules.Board;
import core.rules.Piece;


public class Rook extends Piece {

    private boolean hasMoved;

    public Rook(boolean isWhite) {
        super(isWhite);
        this.hasMoved = false;
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {
        // Tiene que moverse horizontal o vertical, no en diagonal
        if (start.row() != end.row() && start.column() != end.column()) {
            return MoveVerdict.INVALID;
        }
        // Revisar que no haya nada en el camino
        if (board.isPathClear(start, end)) {
            return MoveVerdict.VALID;
        }
        
        return MoveVerdict.INVALID;
    }

    @Override
    public char getSymbol() {
        return 'R';
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void markAsMoved() {
        this.hasMoved = true;
    }
}
