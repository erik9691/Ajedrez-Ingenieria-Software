package main.ports.dto;

import main.core.models.Move;
import main.core.models.Position;

import java.util.List;

// Snapshot inmutable: las IAs reciben el estado real y las jugadas autorizadas por el nucleo.
public record BoardView(
        PieceView[][] cells,
        boolean whiteTurn,
        String castlingRights,
        Position enPassantTarget,
        int halfmoveClock,
        int fullmoveNumber,
        List<Move> legalMoves) {
    public static final int SIZE = 8;

    public BoardView(PieceView[][] cells) {
        this(cells, true, "-", null, 0, 1, List.of());
    }

    public BoardView {
        cells = copy(cells);
        legalMoves = List.copyOf(legalMoves);
    }

    private static PieceView[][] copy(PieceView[][] cells) {
        if (cells == null || cells.length != SIZE)
            throw new IllegalArgumentException("El tablero debe ser 8x8");
        PieceView[][] result = new PieceView[SIZE][];
        for (int i = 0; i < SIZE; i++) {
            if (cells[i] == null || cells[i].length != SIZE)
                throw new IllegalArgumentException("El tablero debe ser 8x8");
            result[i] = cells[i].clone();
        }
        return result;
    }

    @Override
    public PieceView[][] cells() {
        return copy(cells);
    }

    public PieceView pieceAt(int row, int column) {
        return cells[row][column];
    }
}
