package adapters;

import java.util.Scanner;

import ports.GameInputPort;

public class ConsoleInputAdapter {
    private final GameInputPort gameInputPort;
    private final Scanner scanner = new Scanner(System.in);

    // Casilla en coordenadas del tablero (fila 0 = parte superior, donde están las negras)
    private record Square(int row, int column) {
    }

    public ConsoleInputAdapter(GameInputPort gameInputPort) {
        this.gameInputPort = gameInputPort;
    }

    public void run() {
        gameInputPort.startGame();

        while (!gameInputPort.isGameOver()) {
            System.out.print("Origen: ");
            if (!scanner.hasNextLine()) {
                return; // se cerró la entrada
            }
            String origin = scanner.nextLine();

            System.out.print("Destino: ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String destination = scanner.nextLine();

            Square from;
            Square to;
            try {
                from = parseSquare(origin);
                to = parseSquare(destination);
            } catch (IllegalArgumentException e) {
                // Error de sintaxis de la consola: el core nunca llega a verlo
                System.out.println("Error: " + e.getMessage());
                continue;
            }

            // Si la jugada se rechaza, el core ya lo informó por DisplayPort
            gameInputPort.movePiece(from.row(), from.column(), to.row(), to.column());
        }
    }

    // Convierte notación de ajedrez (ej. "e2") a coordenadas del tablero.
    // La fila 8 del tablero es la fila 0 de la matriz, y la fila 1 es la 7.
    private Square parseSquare(String notation) {
        String text = notation == null ? "" : notation.trim().toLowerCase();
        if (!text.matches("[a-h][1-8]")) {
            throw new IllegalArgumentException("Posición inválida: \"" + notation + "\". Usá el formato a1-h8.");
        }
        int column = text.charAt(0) - 'a';
        int row = 8 - (text.charAt(1) - '0');
        return new Square(row, column);
    }
}
