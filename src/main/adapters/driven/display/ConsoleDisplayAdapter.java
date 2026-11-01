package main.adapters.driven.display;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import main.core.flags.PlayResult;
import main.ports.dto.BoardView;
import main.ports.dto.PieceView;
import main.ports.out.DisplayPort;

// Muestra el tablero en una consola aparte: al empezar la partida lanza una
// terminal nueva que lee un archivo y redibuja la pantalla (borrando y volviendo
// a imprimir) cada vez que el archivo cambia. La consola que ejecuta run.sh
// queda solo para el input y los mensajes de la partida.
// El reloj es un placeholder: su comportamiento todavía no está definido.
public class ConsoleDisplayAdapter implements DisplayPort {

    // Terminales soportadas, en orden de preferencia. Cada entrada es el binario
    // y sus flags; al final se agregan "sh", "-c" y el loop de display.
    private static final String[][] TERMINALS = {
            { "xterm", "-title", "Tablero de ajedrez", "-e" },
            { "konsole", "-e" },
            { "xfce4-terminal", "-e" },
            { "kitty" },
            { "alacritty", "-e" },
            { "gnome-terminal", "--" },
    };

    private static final long PLACEHOLDER_MILLIS = 5 * 60_000L;

    private final Path boardFile = Path.of("/tmp", "ajedrez-board-" + ProcessHandle.current().pid() + ".txt");
    private Process boardWindow;
    private boolean windowOpen;

    public ConsoleDisplayAdapter() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeBoardWindow));
    }

    @Override
    public void displayGameStart() {
        System.out.println("¡Comienza la partida!");
        writeBoardFile("Preparando tablero...");
        openBoardWindow();
    }

    @Override
    public void displayBoard(BoardView board) {
        String content = renderBoard(board);
        if (windowOpen) {
            writeBoardFile(content);
        } else {
            System.out.println(content);
        }
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
        closeBoardWindow();
        if (isWhiteWinner == null) {
            System.out.println("¡Fin del juego! Empate.");
            return;
        }
        System.out.println("¡Fin del juego! Ganan las " + (isWhiteWinner ? "blancas" : "negras") + ".");
    }

    // ---- Ventana del tablero ----

    private void openBoardWindow() {
        if (System.getenv("DISPLAY") == null && System.getenv("WAYLAND_DISPLAY") == null) {
            System.out.println("(Sin entorno gráfico: el tablero se imprime en esta consola.)");
            return;
        }

        // El loop redibuja solo cuando el archivo cambia (comparando mtime),
        // así la ventana no parpadea entre movimientos.
        String loop = "f=\"" + boardFile + "\"; o=\"\"; while true; do "
                + "n=$(stat -c %y \"$f\" 2>/dev/null); "
                + "[ \"$n\" = \"$o\" ] || { clear; cat \"$f\"; o=\"$n\"; }; "
                + "sleep 0.2; done";

        for (String[] terminal : TERMINALS) {
            String[] command = new String[terminal.length + 3];
            System.arraycopy(terminal, 0, command, 0, terminal.length);
            command[terminal.length] = "sh";
            command[terminal.length + 1] = "-c";
            command[terminal.length + 2] = loop;
            try {
                boardWindow = new ProcessBuilder(command).start();
                windowOpen = true;
                return;
            } catch (IOException e) {
                // Probar con la siguiente terminal de la lista.
            }
        }
        System.out.println("(No se encontró una terminal para el tablero: se imprime en esta consola.)");
    }

    private void closeBoardWindow() {
        if (boardWindow != null) {
            boardWindow.destroy();
            boardWindow = null;
        }
        windowOpen = false;
    }

    // Escribe vía archivo temporal + rename para que el loop nunca lea un archivo a medio escribir.
    private void writeBoardFile(String content) {
        try {
            Path tmp = boardFile.resolveSibling(boardFile.getFileName() + ".tmp");
            Files.writeString(tmp, content);
            Files.move(tmp, boardFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("No se pudo actualizar la ventana del tablero: " + e.getMessage());
        }
    }

    private String renderBoard(BoardView board) {
        StringBuilder content = new StringBuilder();
        content.append("Blancas: ").append(formatClock(PLACEHOLDER_MILLIS))
                .append("  Negras: ").append(formatClock(PLACEHOLDER_MILLIS)).append('\n');
        content.append('\n');
        content.append("    a b c d e f g h\n");
        content.append("  +-----------------+\n");
        for (int row = 0; row < BoardView.SIZE; row++) {
            int rank = BoardView.SIZE - row; // la fila 0 es la fila 8 del tablero
            content.append(rank).append(" | ");
            for (int column = 0; column < BoardView.SIZE; column++) {
                content.append(symbolOf(board.pieceAt(row, column))).append(' ');
            }
            content.append("| ").append(rank).append('\n');
        }
        content.append("  +-----------------+\n");
        content.append("    a b c d e f g h\n");
        return content.toString();
    }

    private static String formatClock(long millis) {
        long totalSeconds = millis / 1000;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    // Mayúscula = blancas, minúscula = negras, '.' = casilla vacía
    private char symbolOf(PieceView piece) {
        if (piece == null) {
            return '.';
        }
        return piece.isWhite() ? Character.toUpperCase(piece.symbol()) : Character.toLowerCase(piece.symbol());
    }
}
