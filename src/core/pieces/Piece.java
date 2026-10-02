package core.pieces;

import core.boards.Board;
import core.models.Position;

public abstract class Piece {
    boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract boolean canMove(Board board, Position posStart, Position posEnd);

    public boolean getColor() {
        return isWhite;
    }
}
