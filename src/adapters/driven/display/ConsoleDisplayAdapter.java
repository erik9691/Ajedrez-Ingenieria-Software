package adapters.driven.display;

import core.flags.PlayResult;
import ports.dto.BoardView;
import ports.out.DisplayPort;
import ports.dto.PieceView;

public class ConsoleDisplayAdapter implements DisplayPort {

    @Override
    public void displayGameStart() {
        System.out.println("¡Comienza la partida!");
    }

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
    public void displayMessage(PlayResult flag) {
        System.out.println(switch (flag) {
            case LEGAL -> "";
            case ILLEGAL -> "La pieza no puede moverse a esa casilla.";
            case NO_PIECE -> "No hay ninguna pieza en la casilla de origen.";
            case OUT_OF_BOUNDS -> "Posición fuera del tablero.";
            case OWN_PIECE_CAPTURE -> "No podés capturar tu propia pieza.";
            case PROMOTION_PENDING -> "Promoción pendiente: elegí la pieza (Q/R/B/N).";
            case SELF_MATE -> "Ese movimiento te dejaría en jaque.";
            case CHECK -> "¡Jaque!";
            case CHECKMATE -> "¡Fin del juego! Jaque mate.";
            case NOT_YOUR_TURN -> "No es el turno de esa pieza.";
            case INVALID_PROMOTION -> "Elección de promoción inválida. Usá Q, R, B o N.";
            case DRAW -> "Empate.";
            case STALEMATE -> "Tablas por ahogado.";
            case FORFEIT -> "El jugador abandonó la partida.";
            case NONE -> "";
        });
    }

    @Override
    public void displayTurn(boolean isWhiteTurn) {
        System.out.println("Turno de las " + (isWhiteTurn ? "blancas" : "negras") + ".");
    }

    @Override
    public void displayMoveRejected(PlayResult reason) {
        System.out.print("Error: ");
        displayMessage(reason);
    }

    @Override
    public void displayPromotionMenu(BoardView board) {
        System.out.println("Promocionar a: Q = Dama, R = Torre, B = Alfil, N = Caballo.");
    }

    @Override
    public void displayGameOver(Boolean isWhiteWinner) {
        if (isWhiteWinner == null) {
            System.out.println("¡Fin del juego! Empate.");
            return;
        }
        System.out.println("¡Fin del juego! Ganan las " + (isWhiteWinner ? "blancas" : "negras") + ".");
    }

    // Mayúscula = blancas, minúscula = negras, '.' = casilla vacía
    private char symbolOf(PieceView piece) {
        if (piece == null) {
            return '.';
        }
        return piece.isWhite() ? Character.toUpperCase(piece.symbol()) : Character.toLowerCase(piece.symbol());
    }
}
