package main.adapters.driven.input;

import main.core.flags.PromotionId;
import main.core.models.Move;
import main.core.models.Position;
import main.core.time.PlayerClock;
import main.ports.dto.BoardView;
import main.ports.dto.InputResult;
import main.ports.dto.PlayerStatus;
import main.ports.out.PlayerInputPort;

import java.util.Scanner;

public class ConsoleInputAdapter implements PlayerInputPort {
    private final Scanner scanner;

    public ConsoleInputAdapter() {
        this(ConsoleScanner.getInstance());
    }

    public ConsoleInputAdapter(Scanner scanner) {
        this.scanner = java.util.Objects.requireNonNull(scanner);
    }

    private PlayerClock clock;

    @Override
    public void setClock(PlayerClock clock) {
        this.clock = clock;
    }

    // El servicio controla el reloj y limita la espera de lectura.
    @Override
    public InputResult requestMove(BoardView board) {
        while (true) {
            if (clockExpired()) {
                return new InputResult(null, PlayerStatus.TIME_OVER);
            }

            System.out.print("Origen: ");
            if (!scanner.hasNextLine()) {
                return new InputResult(null, PlayerStatus.FORFEIT); // se cerró la entrada
            }
            String origin = scanner.nextLine();
            if (isForfeit(origin)) {
                return new InputResult(null, PlayerStatus.FORFEIT);
            }
            if (clockExpired()) {
                return new InputResult(null, PlayerStatus.TIME_OVER);
            }

            System.out.print("Destino: ");
            if (!scanner.hasNextLine()) {
                return new InputResult(null, PlayerStatus.FORFEIT);
            }
            String destination = scanner.nextLine();
            if (isForfeit(destination)) {
                return new InputResult(null, PlayerStatus.FORFEIT);
            }
            if (clockExpired()) {
                return new InputResult(null, PlayerStatus.TIME_OVER);
            }

            try {
                return new InputResult(
                        new Move(parseSquare(origin), parseSquare(destination)), PlayerStatus.OK);
            } catch (IllegalArgumentException e) {
                // Error de sintaxis de la consola: el main.core nunca llega a verlo
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // El servicio mide tambien el tiempo de la eleccion de promocion.
    @Override
    public PromotionId requestPromotion(BoardView board) {
        while (true) {
            System.out.print("Elección: ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String choice = scanner.nextLine().trim();
            if (isForfeit(choice)) return null;

            if (choice.length() != 1) {
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

    private boolean clockExpired() {
        return clock != null && clock.isExpired();
    }

    // "CHAU" en origen o destino: el jugador se rinde.
    private boolean isForfeit(String input) {
        return "CHAU".equalsIgnoreCase(input.trim());
    }

    // Convierte notación de ajedrez (ej. "e2") a coordenadas del tablero.
    // La fila 8 del tablero es la fila 0 de la matriz, y la fila 1 es la 7.
    private Position parseSquare(String notation) {
        String text = notation == null ? "" : notation.trim().toLowerCase();
        if (!text.matches("[a-h][1-8]")) {
            throw new IllegalArgumentException(
                    "Posición inválida: \"" + notation + "\". Usá el formato a1-h8.");
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
