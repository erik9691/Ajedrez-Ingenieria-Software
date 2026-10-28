package main.adapters.driven.input;

import java.util.Scanner;

import main.core.flags.PromotionId;
import main.core.models.Move;
import main.core.models.Position;
import main.ports.out.PlayerInputPort;
import main.ports.dto.BoardView;

public class ConsoleInputAdapter implements PlayerInputPort {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public Move requestMove(BoardView board) {
        while (true) {
            System.out.print("Origen: ");
            if (!scanner.hasNextLine()) {
                return null; // se cerró la entrada: el main.core lo trata como abandono
            }
            String origin = scanner.nextLine();

            System.out.print("Destino: ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String destination = scanner.nextLine();

            try {
                return new Move(parseSquare(origin), parseSquare(destination));
            } catch (IllegalArgumentException e) {
                // Error de sintaxis de la consola: el main.core nunca llega a verlo
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        while (true) {
            System.out.print("Elección: ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String choice = scanner.nextLine();
            if (choice.isEmpty()) {
                continue;
            }
            PromotionId piece = parsePromotionPiece(choice.charAt(0));
            if (piece == null) {
                System.out.println("Error: elección inválida. Usá Q, R, B o N.");
                continue;
            }
            return piece;
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
    private PromotionId parsePromotionPiece(char letter) {
        return switch (Character.toUpperCase(letter)) {
            case 'Q' -> PromotionId.QUEEN;
            case 'R' -> PromotionId.ROOK;
            case 'B' -> PromotionId.BISHOP;
            case 'N' -> PromotionId.KNIGHT;
            default -> null;
        };
    }
}
