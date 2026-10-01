package core.pieces;

import java.util.List;

import core.interfaces.Board;
import core.interfaces.Movement;
import core.interfaces.Piece;
import core.model.Color;
import core.model.NormalMove;
import core.model.Pos;

public class Bishop extends Piece {

    public Bishop(Color color) {
        super(color);
    }

    @Override
    public List<Pos> getValidMoves(Board board, Pos from) {
        return List.of(); // pendiente
    }

    @Override
    public Movement move(Board board, Pos origin, Pos dest){
        if (origin.equals(dest)) {
            return null;
        }

        if (dest.row() < 0 || dest.row() >= board.getRows()
                || dest.col() < 0 || dest.col() >= board.getColumns()) {
            return null;
        }

        if (Math.abs(dest.row() - origin.row()) != Math.abs(dest.col() - origin.col())) {
            return null;
        }

        int rowStep = Integer.compare(dest.row(), origin.row());
        int colStep = Integer.compare(dest.col(), origin.col());

        int row = origin.row() + rowStep;
        int col = origin.col() + colStep;
        while (row != dest.row() || col != dest.col()) {
            if (!board.isEmpty(new Pos(row, col))) {
                return null;
            }
            row += rowStep;
            col += colStep;
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
