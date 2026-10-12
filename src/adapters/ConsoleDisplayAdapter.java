package adapters;

import ports.BoardView;
import ports.DisplayPort;
import ports.MoveResult;
import ports.PieceView;

public class ConsoleDisplayAdapter implements DisplayPort {

    @Override
    public void displayBoard(BoardView board) {
        System.out.println();
        System.out.println("    a b c d e f g h");
        System.out.println("  +-----------------+");
        for (int row = 0; row < BoardView.SIZE; row++) {
            int rank = BoardView.SIZE - row; // la fila 0 es la fila 8 del tablero
            StringBuilder line = new StringBuilder();
            line.append(rank).append(" | ");
            for (int column = 0; column < BoardView.SIZE; column++) {
                line.append(symbolOf(board.pieceAt(row, column))).append(' ');
            }
            line.append("| ").append(rank);
            System.out.println(line);
        }
        System.out.println("  +-----------------+");
        System.out.println("    a b c d e f g h");
        System.out.println();
    }

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void displayTurn(boolean isWhiteTurn) {
        System.out.println("Turno de las " + (isWhiteTurn ? "blancas" : "negras") + ".");
    }

    @Override
    public void displayMoveRejected(MoveResult reason) {
        String text = switch (reason) {
            case NO_PIECE -> "No hay ninguna pieza en la casilla de origen.";
            case NOT_YOUR_TURN -> "No es el turno de esa pieza.";
            case ILLEGAL_MOVE -> "La pieza no puede moverse a esa casilla.";
            case OK -> "";
        };
        if (!text.isEmpty()) {
            System.out.println("Error: " + text);
        }
    }

    // Mayúscula = blancas, minúscula = negras, '.' = casilla vacía
    private char symbolOf(PieceView piece) {
        if (piece == null) {
            return '.';
        }
        return piece.isWhite() ? Character.toUpperCase(piece.symbol()) : Character.toLowerCase(piece.symbol());
    }
}
