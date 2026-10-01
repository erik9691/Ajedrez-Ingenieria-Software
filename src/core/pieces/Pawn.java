package core.pieces;

import java.util.ArrayList;
import java.util.List;

import core.interfaces.Board;
import core.interfaces.Movement;
import core.interfaces.Piece;
import core.model.Color;
import core.model.EnPassantMove;
import core.model.NormalMove;
import core.model.Pos;
import core.model.PromotionMove;

public class Pawn extends Piece {

    private boolean justMovedTwo;

    public Pawn(Color color) {
        super(color);
    }

    @Override
    public List<Pos> getValidMoves(Board board, Pos tile){
        List<Pos> moves = new ArrayList<>();

        int direction = (color == Color.WHITE) ? 1 : -1;
        int startRow = (color == Color.WHITE) ? 1 : board.getRows() - 2;

        int nextRow = tile.row() + direction;
        if (nextRow < 0 || nextRow >= board.getRows()) {
            return moves;
        }

        Pos oneAhead = new Pos(nextRow, tile.col());
        if (board.isEmpty(oneAhead)) {
            moves.add(oneAhead);

            Pos twoAhead = new Pos(tile.row() + 2 * direction, tile.col());
            if (tile.row() == startRow && board.isEmpty(twoAhead)) {
                moves.add(twoAhead);
            }
        }

        for (int dCol = -1; dCol <= 1; dCol += 2) {
            int col = tile.col() + dCol;
            if (col < 0 || col >= board.getColumns()) {
                continue;
            }

            Pos diagonal = new Pos(nextRow, col);
            if (board.isEmpty(diagonal)) {
                Piece adjacent = board.pieceAt(new Pos(tile.row(), col));
                if (adjacent instanceof Pawn pawn
                        && pawn.getColor() == color.opposite()
                        && pawn.justMovedTwo) {
                    moves.add(diagonal);
                }
            } else if (board.isColor(diagonal, color.opposite())) {
                moves.add(diagonal);
            }
        }

        return moves;
    };
    

    @Override
    public Movement move(Board board, Pos origin, Pos dest){
        if (origin.equals(dest)) {
            return null;
        }

        if (dest.row() < 0 || dest.row() >= board.getRows()
                || dest.col() < 0 || dest.col() >= board.getColumns()) {
            return null;
        }

        int direction = (color == Color.WHITE) ? 1 : -1;
        int startRow = (color == Color.WHITE) ? 1 : board.getRows() - 2;
        int lastRow = (color == Color.WHITE) ? board.getRows() - 1 : 0;

        int dRow = dest.row() - origin.row();
        int dCol = dest.col() - origin.col();
        int forward = dRow * direction;

        justMovedTwo = false;

        if (dCol == 0) {
            if (forward == 1) {
                if (!board.isEmpty(dest)) {
                    return null;
                }
            } else if (forward == 2 && origin.row() == startRow) {
                if (!board.isEmpty(new Pos(origin.row() + direction, origin.col()))
                        || !board.isEmpty(dest)) {
                    return null;
                }
                justMovedTwo = true;
            } else {
                return null;
            }

            board.placePiece(this, dest);
            board.placePiece(null, origin);

            if (dest.row() == lastRow) {
                Piece promotedTo = new Queen(color);
                board.placePiece(promotedTo, dest);
                return new PromotionMove(origin, dest, null, this, promotedTo);
            }

            return new NormalMove(origin, dest, null);
        }

        if (Math.abs(dCol) == 1 && forward == 1) {
            if (!board.isEmpty(dest)) {
                if (board.pieceAt(dest).getColor() == color) {
                    return null;
                }

                Piece captured = board.pieceAt(dest);
                board.placePiece(this, dest);
                board.placePiece(null, origin);

                if (dest.row() == lastRow) {
                    Piece promotedTo = new Queen(color);
                    board.placePiece(promotedTo, dest);
                    return new PromotionMove(origin, dest, captured, this, promotedTo);
                }

                return new NormalMove(origin, dest, captured);
            }

            Piece adjacent = board.pieceAt(new Pos(origin.row(), dest.col()));
            if (!(adjacent instanceof Pawn pawn)
                    || pawn.getColor() == color
                    || !pawn.justMovedTwo) {
                return null;
            }

            Pos capturedAt = new Pos(origin.row(), dest.col());
            board.placePiece(null, capturedAt);
            board.placePiece(this, dest);
            board.placePiece(null, origin);
            return new EnPassantMove(origin, dest, adjacent, capturedAt);
        }

        return null;
    };
}
