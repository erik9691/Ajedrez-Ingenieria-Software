package core.interfaces;

import java.util.List;

import core.model.Color;
import core.model.Pos;

public abstract class Piece {

    protected final Color color;

    protected Piece(Color color) {
        this.color = color;
    }

    public abstract List<Pos> getValidMoves(Board board, Pos tile);

    public abstract Movement move(Board board, Pos origin, Pos dest);

    public Color getColor() {
        return color;
    }
}
