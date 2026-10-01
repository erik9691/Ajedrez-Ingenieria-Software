package core.boards;

import core.interfaces.Board;
import core.model.Pos;
import core.interfaces.Movement;
import core.interfaces.Piece;
import core.model.Color;

public class ClassicBoard extends Board {
    protected ClassicBoard(int rows, int columns) {
        super(rows, columns);
    }

    public Piece pieceAt(Pos tile){
        return tiles[tile.row()][tile.col()];
    };

    public boolean isEmpty(Pos tile){
        return (tiles[tile.row()][tile.col()] == null);
    };

    public boolean isColor(Pos tile, Color color){
        return (tiles[tile.row()][tile.col()].getColor() == color);
    }

    public List<Pos> possibleMoves(Pos tile){
        Piece selectedPiece = tiles[tile.row()][tile.col()];
        return selectedPiece.getValidMoves(this, tile);
    };

    public Movement movePiece(Pos origin, Pos dest){
        Piece selectedPiece = tiles[tile.row()][tile.col()];
        return selectedPiece.move(this, origin, dest);
    }

    public void placePiece(Piece piece, Pos tile){
        tiles[tile.row()][tile.col()] = piece;
    }

    public boolean isCheckmate(Color loser){
        return false;
    };

}