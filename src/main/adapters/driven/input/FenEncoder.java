package main.adapters.driven.input;

import main.ports.dto.*;

public final class FenEncoder {
    private FenEncoder() {}

    public static String encode(BoardView board) {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                PieceView piece = board.pieceAt(row, col);
                if (piece == null) {
                    empty++;
                    continue;
                }
                if (empty > 0) {
                    fen.append(empty);
                    empty = 0;
                }
                fen.append(
                        piece.isWhite() ? piece.symbol() : Character.toLowerCase(piece.symbol()));
            }
            if (empty > 0) fen.append(empty);
            if (row < 7) fen.append('/');
        }
        fen.append(board.whiteTurn() ? " w " : " b ").append(board.castlingRights()).append(' ');
        var ep = board.enPassantTarget();
        if (ep == null) fen.append('-');
        else fen.append((char) ('a' + ep.column())).append((char) ('8' - ep.row()));
        return fen.append(' ')
                .append(board.halfmoveClock())
                .append(' ')
                .append(board.fullmoveNumber())
                .toString();
    }
}
