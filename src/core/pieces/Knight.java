package core.pieces;

import java.util.List;

import core.interfaces.Board;
import core.interfaces.Movement;
import core.interfaces.Piece;
import core.model.Color;
import core.model.NormalMove;
import core.model.Pos;

public class Knight extends Piece {

    public Knight(Color color) {
        super(color);
    }

    @Override
    public List<Pos> getValidMoves(Board board, Pos from) {
        return List.of(); // pendiente
    }

    @Override
    public Movement move(Board board, Pos origin, Pos dest){
        if (dest.row() < 0 || dest.row() >= board.getRows()
                || dest.col() < 0 || dest.col() >= board.getColumns()) {
            return null;
        }

        int dRow = Math.abs(dest.row() - origin.row());
        int dCol = Math.abs(dest.col() - origin.col());
        if (!((dRow == 2 && dCol == 1) || (dRow == 1 && dCol == 2))) {
            return null;
        }

        if (!board.isEmpty(dest) && board.pieceAt(dest).getColor() == color) {
            return null;
        }

        Piece captured = board.pieceAt(dest);
        board.placePiece(this, dest);
        board.placePiece(null, origin);
        return new NormalMove(origin, dest, captured);
    }
}
