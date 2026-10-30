
package main.adapters.driving.menu;

import java.util.Scanner;

import main.adapters.driven.display.ConsoleDisplayAdapter;
import main.adapters.driven.input.ConsoleInputAdapter;
import main.adapters.driven.input.ConsoleScanner;
import main.adapters.driven.input.RandomMovesAI;
import main.adapters.driven.input.StockfishElevenAI;
import main.core.application.ChessService;
import main.core.game.Board;
import main.core.game.GameOverChecker;
import main.core.game.MoveHistory;
import main.core.managers.GameManager;
import main.core.models.GameOption;
import main.ports.in.MenuPort;
import main.ports.out.PlayerInputPort;

public class ConsoleMenuAdapter implements MenuPort {

    private final Scanner scanner = ConsoleScanner.getInstance();

    @Override
    public void configureAndStartGame() {
        // Obtener configuración de la partida
        GameOption option = chooseOption();

        boolean humanIsWhite = true;

        if (option != GameOption.LOCAL) {
            humanIsWhite = choosePlayerColor();
        }

        // Crear dependencias del dominio
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        GameManager game = new GameManager(board, checker, history);

        // Crear adaptador de salida
        ConsoleDisplayAdapter display = new ConsoleDisplayAdapter();

        // Seleccionar adaptadores de entrada
        PlayerInputPort whiteInput;
        PlayerInputPort blackInput;

        switch (option) {
            case LOCAL -> {
                whiteInput = new ConsoleInputAdapter();
                blackInput = new ConsoleInputAdapter();
            }

            case VS_AI -> {
                RandomMovesAI ai = new RandomMovesAI();

                whiteInput = humanIsWhite ? new ConsoleInputAdapter() : ai;

                blackInput = humanIsWhite ? ai : new ConsoleInputAdapter();
            }

            case VS_STOCKFISH -> {
                String path = enginePath();

                if (path == null) {
                    throw new IllegalStateException("Sistema operativo no compatible con Stockfish.");
                }

                StockfishElevenAI ai = new StockfishElevenAI(!humanIsWhite,path);

                whiteInput = humanIsWhite ? new ConsoleInputAdapter() : ai;

                blackInput = humanIsWhite ? ai : new ConsoleInputAdapter();
            }

            default -> throw new IllegalStateException("Modo de juego desconocido");
        }

        // Crear y ejecutar ChessService
        ChessService chessService = new ChessService(
            game,
            display,
            whiteInput,
            blackInput
        );

        chessService.runGame();
    }

    private GameOption chooseOption() {
        while (true) {
            System.out.println("Elegí el modo de juego:");
            System.out.println("1. Local");
            System.out.println("2. Contra la IA");
            System.out.println("3. Contra Stockfish");
            System.out.print("Opción: ");

            if (!scanner.hasNextLine()) {
                return GameOption.LOCAL;
            }

            switch (scanner.nextLine().trim()) {
                case "1" -> { return GameOption.LOCAL; }
                case "2" -> { return GameOption.VS_AI; }
                case "3" -> { return GameOption.VS_STOCKFISH; }
                default -> System.out.println(
                    "Opción inválida. Elegí 1, 2 o 3."
                );
            }
        }
    }

    private boolean choosePlayerColor() {
        while (true) {
            System.out.print("¿Jugás con blancas? (s/n): ");

            if (!scanner.hasNextLine()) {
                return true;
            }

            String answer = scanner.nextLine().trim().toLowerCase();

            if (answer.equals("s")) return true;
            if (answer.equals("n")) return false;

            System.out.println("Respuesta inválida. Respondé s o n.");
        }
    }

    private String enginePath() {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("win")) {
            return "engines/stockfish11/sf11-windows.exe";
        }

        if (os.contains("mac")) {
            return "engines/stockfish11/sf11-mac";
        }

        if (os.contains("linux")) {
            return "engines/stockfish11/sf11-linux";
        }

        return null;
    }
}
