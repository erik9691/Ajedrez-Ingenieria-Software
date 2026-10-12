package adapters;

import java.util.Scanner;
import core.models.Position;
import ports.GameInputPort;

public class ConsoleInputAdapter {
    private final GameInputPort gameInputPort;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleInputAdapter(GameInputPort gameInputPort) {
        this.gameInputPort = gameInputPort;
    }

    public void run() {
        gameInputPort.startGame();

        while (!gameInputPort.isGameOver()) {
            System.out.print("Origen: ");
            String origin = scanner.nextLine();

            System.out.print("Destino: ");
            String destination = scanner.nextLine();

            Position from = Position.fromNotation(origin);
            Position to = Position.fromNotation(destination);

            boolean success = gameInputPort.movePiece(from, to);

            if (!success) {
                System.out.println("Movimiento inválido.");
            }
        }
    }
}