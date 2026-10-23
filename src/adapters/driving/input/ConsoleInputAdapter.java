package adapters.driving.input;

import java.util.Scanner;

import core.flags.PromotionNames;
import core.models.Position;
import ports.dto.InputResult;
import ports.in.GameInputPort;

public class ConsoleInputAdapter {
    private final GameInputPort gameInputPort;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleInputAdapter(GameInputPort gameInputPort) {
        this.gameInputPort = gameInputPort;
    }

    public void run() {
        gameInputPort.startGame();

        InputResult result = InputResult.ACCEPTED;
        while (result != InputResult.GAME_OVER) {
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

            Position from;
            Position to;
            try {
                from = parseSquare(origin);
                to = parseSquare(destination);
            } catch (IllegalArgumentException e) {
                // Error de sintaxis de la consola: el core nunca llega a verlo
                System.out.println("Error: " + e.getMessage());
                continue;
            }

            // Si la jugada se rechaza, el core ya lo informó por DisplayPort
            result = gameInputPort.makeMove(from, to);

            // Si el movimiento dejó una promoción pendiente, se pide la pieza hasta que se acepte
            while (result == InputResult.PROMOTION_PENDING) {
                System.out.print("Promocionar a (Q/R/B/N): ");
                if (!scanner.hasNextLine()) {
                    return;
                }
                String choice = scanner.nextLine();
                if (choice.isEmpty()) {
                    continue;
                }
                PromotionNames piece = parsePromotionPiece(choice.charAt(0));
                if (piece == null) {
                    // Error de sintaxis de la consola: el core nunca llega a verlo
                    System.out.println("Error: elección inválida. Usá Q, R, B o N.");
                    continue;
                }
                result = gameInputPort.promotePawn(piece);
            }
        }
    }

    // Convierte notación de ajedrez (ej. "e2") a coordenadas del tablero.
    // La fila 8 del tablero es la fila 0 de la matriz, y la fila 1 es la 7.
    private Position parseSquare(String notation) {
        String text = notation == null ? "" : notation.trim().toLowerCase();
        if (!text.matches("[a-h][1-8]")) {
            throw new IllegalArgumentException("Posición inválida: \"" + notation + "\". Usá el formato a1-h8.");
        }
        int column = text.charAt(0) - 'a';
        int row = 8 - (text.charAt(1) - '0');
        return new Position(row, column);
    }

    // Convierte la letra ingresada (Q, R, B o N) a la pieza de promoción elegida.
    private PromotionNames parsePromotionPiece(char letter) {
        return switch (Character.toUpperCase(letter)) {
            case 'Q' -> PromotionNames.QUEEN;
            case 'R' -> PromotionNames.ROOK;
            case 'B' -> PromotionNames.BISHOP;
            case 'N' -> PromotionNames.KNIGHT;
            default -> null;
        };
    }
}
