package core.models;

import core.boards.Board;
import core.flags.MoveVerdict;

public abstract class Piece {
    protected boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract MoveVerdict canMove(Board board, Position posStart, Position posEnd);

    // Letra que identifica el tipo de pieza (K, Q, R, B, N, P)
    public abstract char getSymbol();

    public boolean getColor() {
        return isWhite;
    }
}
