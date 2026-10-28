package main.core.game;

import main.core.flags.MoveVerdict;
import main.core.models.Position;

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
