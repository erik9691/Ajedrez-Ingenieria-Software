package main.adapters.driving.menu;

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
import main.core.time.PlayerClock;
import main.ports.in.MatchRunnerPort;
import main.ports.out.PlayerInputPort;

import java.util.Scanner;

public class ConsoleMenuAdapter {

    private final Scanner scanner;
    private final java.util.function.Supplier<main.ports.out.DisplayPort> displays;
    private final java.util.function.Supplier<PlayerInputPort> randomPlayers;
    private final java.util.function.BiFunction<Boolean, String, PlayerInputPort> engines;

    public ConsoleMenuAdapter() {
        this(
                ConsoleScanner.getInstance(),
                ConsoleDisplayAdapter::new,
                RandomMovesAI::new,
                StockfishElevenAI::new);
    }

    public ConsoleMenuAdapter(
            Scanner scanner,
            java.util.function.Supplier<main.ports.out.DisplayPort> displays,
            java.util.function.Supplier<PlayerInputPort> randomPlayers,
            java.util.function.BiFunction<Boolean, String, PlayerInputPort> engines) {
        this.scanner = scanner;
        this.displays = displays;
        this.randomPlayers = randomPlayers;
        this.engines = engines;
    }

    public void configureAndStartGame() {

        // Obtener configuración de la partida
        GameOption option = chooseOption(); // Game Option -> local, AI, StockFish

        // Elegir control de tiempo
        PlayerClock timeControl = chooseTimeControl();

        boolean humanIsWhite = true;

        if (option != GameOption.LOCAL
                && option != GameOption.STOCKFISH_VS_AI
                && option != GameOption.STOCKFISH_VS_STOCKFISH) {
            humanIsWhite = choosePlayerColor();
        }

        // Crear dependencias del dominio
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker checker = new GameOverChecker(board, history);
        GameManager game = new GameManager(board, checker, history);

        // Crear adaptador de salida
        main.ports.out.DisplayPort display = null;

        // Seleccionar adaptadores de entrada
        PlayerInputPort whiteInput = null;
        PlayerInputPort blackInput = null;

        boolean started = false;
        try {
            switch (option) {
                case LOCAL -> {
                    whiteInput = new ConsoleInputAdapter(scanner);
                    blackInput = new ConsoleInputAdapter(scanner);
                }

                case VS_AI -> {
                    PlayerInputPort ai = randomPlayers.get();

                    whiteInput = humanIsWhite ? new ConsoleInputAdapter(scanner) : ai;

                    blackInput = humanIsWhite ? ai : new ConsoleInputAdapter(scanner);
                }

                case VS_STOCKFISH -> {
                    String path = enginePath();

                    if (path == null) {
                        throw new IllegalStateException(
                                "Sistema operativo no compatible con Stockfish.");
                    }

                    PlayerInputPort ai = engines.apply(!humanIsWhite, path);

                    whiteInput = humanIsWhite ? new ConsoleInputAdapter(scanner) : ai;

                    blackInput = humanIsWhite ? ai : new ConsoleInputAdapter(scanner);
                }

                case STOCKFISH_VS_AI -> {
                    String path = enginePath();

                    if (path == null) {
                        throw new IllegalStateException(
                                "Sistema operativo no compatible con Stockfish.");
                    }

                    whiteInput = engines.apply(true, path);
                    blackInput = randomPlayers.get();
                }

                case STOCKFISH_VS_STOCKFISH -> {
                    String path = enginePath();

                    if (path == null) {
                        throw new IllegalStateException(
                                "Sistema operativo no compatible con Stockfish.");
                    }

                    whiteInput = engines.apply(true, path);
                    blackInput = engines.apply(false, path);
                }

                default -> throw new IllegalStateException("Modo de juego desconocido");
            }

            display = displays.get();
            // Crear y ejecutar ChessService
            MatchRunnerPort matchRunner =
                    new ChessService(game, display, whiteInput, blackInput, timeControl);

            started = true;
            matchRunner.runGame();
        } finally {
            if (!started) {
                try {
                    if (whiteInput != null) whiteInput.close();
                } finally {
                    try {
                        if (blackInput != null && blackInput != whiteInput) blackInput.close();
                    } finally {
                        if (display != null) display.close();
                    }
                }
            }
        }
    }

    private GameOption chooseOption() {
        while (true) {
            System.out.println("Elegí el modo de juego:");
            System.out.println("1. Local");
            System.out.println("2. Contra la IA Random");
            System.out.println("3. Contra Stockfish");
            System.out.println("4. Stockfish vs IA Random");
            System.out.println("5. Stockfish vs Stockfish");
            System.out.print("Opción: ");

            if (!scanner.hasNextLine()) {
                return GameOption.LOCAL;
            }

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    return GameOption.LOCAL;
                }
                case "2" -> {
                    return GameOption.VS_AI;
                }
                case "3" -> {
                    return GameOption.VS_STOCKFISH;
                }
                case "4" -> {
                    return GameOption.STOCKFISH_VS_AI;
                }
                case "5" -> {
                    return GameOption.STOCKFISH_VS_STOCKFISH;
                }
                default -> System.out.println("Opción inválida. Elegí 1, 2, 3, 4 o 5.");
            }
        }
    }

    // Devuelve null si se juega sin reloj; ChessService ya lo contempla.
    private PlayerClock chooseTimeControl() {
        while (true) {
            System.out.println("Elegí el control de tiempo:");
            System.out.println("1. Sin tiempo");
            System.out.println("2. Clásica (90 min)");
            System.out.println("3. Rápida (30 min)");
            System.out.println("4. Blitz (10 min)");
            System.out.print("Opción: ");

            if (!scanner.hasNextLine()) {
                return null;
            }

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    return null;
                }
                case "2" -> {
                    return new PlayerClock(90 * 60_000L);
                }
                case "3" -> {
                    return new PlayerClock(30 * 60_000L);
                }
                case "4" -> {
                    return new PlayerClock(10 * 60_000L);
                }
                default -> System.out.println("Opción inválida. Elegí 1, 2, 3 o 4.");
            }
        }
    }

    private boolean choosePlayerColor() {
        while (true) {
            System.out.print("¿Jugás con blancas? (s/n): ");

            if (!scanner.hasNextLine()) {
                return true;
            }

            String answer = scanner.nextLine().trim().toLowerCase(java.util.Locale.ROOT);

            if (answer.equals("s")) return true;
            if (answer.equals("n")) return false;

            System.out.println("Respuesta inválida. Respondé s o n.");
        }
    }

    private String enginePath() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);

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
