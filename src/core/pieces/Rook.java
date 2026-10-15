package core.pieces;

import core.boards.Board;
import core.models.Piece;
import core.models.Position;
import core.models.MoveVerdict;


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
            this.hasMoved = true; // Marcar la torre como movida si el movimiento es válido
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
