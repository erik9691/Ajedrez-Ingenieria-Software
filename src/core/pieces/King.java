package core.pieces;

import java.util.List;

import core.interfaces.Board;
import core.interfaces.Movement;
import core.interfaces.Piece;
import core.model.CastleMove;
import core.model.Color;
import core.model.NormalMove;
import core.model.Pos;

public class King extends Piece {

    public King(Color color) {
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

        int dRow = Math.abs(dest.row() - origin.row());
        int dCol = Math.abs(dest.col() - origin.col());

        if (dRow == 0 && dCol == 2) {
            boolean kingside = dest.col() > origin.col();
            Pos rookOrigin = new Pos(origin.row(), kingside ? board.getColumns() - 1 : 0);
            Pos rookDestination = new Pos(origin.row(), kingside ? dest.col() - 1 : dest.col() + 1);

            Piece rook = board.pieceAt(rookOrigin);
            if (!(rook instanceof Rook) || rook.getColor() != color) {
                return null;
            }

            int step = Integer.compare(rookOrigin.col(), origin.col());
            for (int col = origin.col() + step; col != rookOrigin.col(); col += step) {
                if (!board.isEmpty(new Pos(origin.row(), col))) {
                    return null;
                }
            }

            board.placePiece(this, dest);
            board.placePiece(null, origin);
            board.placePiece(rook, rookDestination);
            board.placePiece(null, rookOrigin);

            if (board.isCheckmate(color)) {
                board.placePiece(this, origin);
                board.placePiece(null, dest);
                board.placePiece(rook, rookOrigin);
                board.placePiece(null, rookDestination);
                return null;
            }

            return new CastleMove(origin, dest, rookOrigin, rookDestination);
        }

        if (dRow > 1 || dCol > 1) {
            return null;
        }

        if (!board.isEmpty(dest) && board.pieceAt(dest).getColor() == color) {
            return null;
        }

        Piece captured = board.pieceAt(dest);
        board.placePiece(this, dest);
        board.placePiece(null, origin);

        if (board.isCheckmate(color)) {
            board.placePiece(this, origin);
            board.placePiece(captured, dest);
            return null;
        }

        return new NormalMove(origin, dest, captured);
    }
}
