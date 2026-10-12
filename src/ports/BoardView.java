package ports;

// Vista de solo lectura del tablero. Es lo único que el dominio le entrega a los adaptadores de salida.
// La fila 0 es la parte superior del tablero (donde están las negras).
public record BoardView(PieceView[][] cells) {
    public static final int SIZE = 8;

    // Devuelve la pieza de la casilla o null si está vacía.
    public PieceView pieceAt(int row, int column) {
        return cells[row][column];
    }
}
