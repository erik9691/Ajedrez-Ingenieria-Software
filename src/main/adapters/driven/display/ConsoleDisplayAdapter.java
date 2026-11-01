package main.adapters.driven.display;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import main.core.flags.PlayResult;
import main.ports.dto.BoardView;
import main.ports.dto.ClockView;
import main.ports.dto.PieceView;
import main.ports.out.DisplayPort;

// Muestra el tablero en una consola aparte: al empezar la partida lanza una
// terminal nueva que lee un archivo y redibuja la pantalla cada vez que cambia.
// La consola que ejecuta run.sh queda solo para el input y los mensajes.
// El conteo en vivo del reloj lo hace el loop de la ventana: el archivo lleva
// una línea oculta (#CLOCK) con el snapshot de los relojes y el loop descuenta
// el tiempo transcurrido usando el mtime del archivo como referencia.
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

    private final Path boardFile = Path.of("/tmp", "ajedrez-board-" + ProcessHandle.current().pid() + ".txt");
    private Process boardWindow;
    private boolean windowOpen;

    public ConsoleDisplayAdapter() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeBoardWindow));
    }

    @Override
    public void displayGameStart() {
        System.out.println("¡Comienza la partida! (Escribir CHAU para rendirse)");
        writeBoardFile("Preparando tablero...");
        openBoardWindow();
    }

    @Override
    public void displayBoard(BoardView board, ClockView clock) {
        String content = renderBoard(board, clock);
        if (windowOpen) {
            String fileContent = clock == null ? content : clockMetadata(clock) + "\n" + content;
            writeBoardFile(fileContent);
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
            case TIME_OVER -> "El jugador se quedó sin tiempo!.";
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

        String loop = buildDisplayLoop();

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

    // Loop de la ventana: redibuja cuando el archivo cambia y, si hay snapshot
    // de reloj, cuenta hacia abajo al jugador activo una vez por segundo.
    // El mtime del archivo marca el momento del snapshot.
    private String buildDisplayLoop() {
        return "f=\"" + boardFile + "\"; m=\"\"; shown=\"\"; while true; do "
                + "m2=$(stat -c %Y \"$f\" 2>/dev/null); "
                + "if [ \"$m2\" != \"$m\" ]; then m=\"$m2\"; meta=$(sed -n 's/^#CLOCK //p' \"$f\" | head -1); fi; "
                + "if [ -z \"$meta\" ]; then "
                + "if [ \"$m2\" != \"$shown\" ]; then clear; cat \"$f\"; shown=\"$m2\"; fi; "
                + "else "
                + "now=$(date +%s); set -- $meta; wrem=$2; brem=$4; "
                + "if [ \"$6\" = \"W\" ]; then wrem=$(( wrem - (now - m2) * 1000 )); else brem=$(( brem - (now - m2) * 1000 )); fi; "
                + "[ \"$wrem\" -lt 0 ] && wrem=0; [ \"$brem\" -lt 0 ] && brem=0; "
                + "ws=$(( wrem / 1000 )); bs=$(( brem / 1000 )); "
                + "clockline=$(printf \"Blancas: %02d:%02d  Negras: %02d:%02d\" $(( ws / 60 )) $(( ws % 60 )) $(( bs / 60 )) $(( bs % 60 ))); "
                + "body=$(sed -e '/^#CLOCK/d' -e \"1s/.*/$clockline/\" \"$f\"); "
                + "if [ \"$body\" != \"$shown\" ]; then clear; printf '%s\\n' \"$body\"; shown=\"$body\"; fi; "
                + "fi; sleep 0.2; done";
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

    // Línea oculta con el snapshot; el loop la usa para el countdown y la filtra al mostrar.
    private String clockMetadata(ClockView clock) {
        return "#CLOCK W " + clock.whiteMillis()
                + " B " + clock.blackMillis()
                + " A " + (clock.whiteTurn() ? "W" : "B");
    }

    private String renderBoard(BoardView board, ClockView clock) {
        StringBuilder content = new StringBuilder();
        if (clock != null) {
            content.append("Blancas: ").append(formatClock(clock.whiteMillis()))
                    .append("  Negras: ").append(formatClock(clock.blackMillis())).append('\n');
        }
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
