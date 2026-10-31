package main;

import main.adapters.driven.display.ConsoleDisplayAdapter;
import main.adapters.driven.input.ConsoleInputAdapter;
import main.adapters.driven.input.RandomMovesAI;
import main.adapters.driven.input.StockfishElevenAI;
import main.adapters.driving.menu.ConsoleMenuAdapter;
import main.adapters.driving.menu.GameOption;
import main.core.application.ChessService;
import main.core.game.MoveHistory;
import main.core.managers.GameManager;
import main.core.game.Board;
import main.core.game.GameOverChecker;
import main.ports.in.ChessMatchPort;
import main.ports.out.DisplayPort;
import main.ports.out.PlayerInputPort;

// Composition root: el único lugar que conoce todas las piezas y las conecta.

public class Main {
    public static void main(String[] args) {
        String enginePath = enginePath(osFromArgs(args));

        ConsoleMenuAdapter menu = new ConsoleMenuAdapter();
        GameOption option = menu.chooseOption();

        DisplayPort display = new ConsoleDisplayAdapter();
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        GameOverChecker gameOverChecker = new GameOverChecker(board, history);
        GameManager gameManager = new GameManager(board, gameOverChecker, history);

        PlayerInputPort whiteInput;
        PlayerInputPort blackInput;

        if (option == GameOption.LOCAL) {
            // Misma consola para ambos: una sola instancia evita dos Scanners compitiendo por System.in
            PlayerInputPort console = new ConsoleInputAdapter();
            whiteInput = console;
            blackInput = console;
        } else {
            boolean playerIsWhite = menu.choosePlayerColor();
            PlayerInputPort human = new ConsoleInputAdapter();
            PlayerInputPort ai;
            if (option == GameOption.VS_STOCKFISH && enginePath != null) {
                ai = new StockfishElevenAI(!playerIsWhite, enginePath);
            } else {
                ai = new RandomMovesAI();
            }
            whiteInput = playerIsWhite ? human : ai;
            blackInput = playerIsWhite ? ai : human;
        }

        ChessMatchPort game = new ChessService(gameManager, display, whiteInput, blackInput);
        game.runGame();
    }

    private static String osFromArgs(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--os")) {
                return args[i + 1];
            }
        }
        // Sin --os (Main ejecutado directo) se deduce de la JVM
        String name = System.getProperty("os.name", "").toLowerCase();
        if (name.contains("win")) return "windows";
        if (name.contains("mac")) return "mac";
        return "linux";
    }

    private static String enginePath(String os) {
        return switch (os) {
            case "linux" -> "engines/stockfish11/sf11-linux";
            case "mac" -> "engines/stockfish11/sf11-mac";
            case "windows" -> "engines/stockfish11/sf11-windows.exe";
            default -> null;
        };
    }
}
