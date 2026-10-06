package core.api;

import core.models.Position;

public abstract class Board {
    protected Piece[][] squares;

    public abstract Piece getPiece(Position position);

    public abstract void setPiece(Position position, Piece piece);

    public abstract boolean tryMove(Position start, Position end);

    public abstract boolean isPathClear(Position start, Position end);

    public abstract boolean isCheckmate(boolean isWhite);
}
