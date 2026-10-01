package core.interfaces;

import java.util.Arrays;

public abstract class Board {

    protected final int rows;
    protected final int columns;
    protected final Piece[][] tiles;

    protected Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.tiles = new Piece[rows][columns];
    }

    public abstract Piece pieceAt(Pos tile);

    public abstract boolean isEmpty(Pos tile);

    public abstract boolean isColor(Pos tile);

    public abstract void placePiece(Piece piece, Pos tile);

    public abstract List<Pos> possibleMoves(Pos tile);

    public abstract Movement movePiece(Pos origin, Pos dest);

    public abstract boolean isCheckmate(Color loser);

    public void clear() {
        for (Piece[] row : tiles) {
            Arrays.fill(row, null);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
