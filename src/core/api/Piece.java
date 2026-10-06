package core.api;

import core.models.Position;

public abstract class Piece {
    protected boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract boolean canMove(Board board, Position posStart, Position posEnd);

    public boolean getColor() {
        return isWhite;
    }
}
