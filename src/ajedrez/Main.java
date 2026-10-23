package ajedrez;

import adapters.driven.ai.RandomMovesAI;
import adapters.driven.display.ConsoleDisplayAdapter;
import adapters.driven.input.ConsoleInputAdapter;
import adapters.driving.menu.ConsoleMenuAdapter;
import adapters.driving.menu.GameOption;
import core.boards.Board;
import core.managers.GameManager;
import core.services.ChessService;
import core.services.VsChessService;
import ports.in.ChessMatchPort;
import ports.out.DisplayPort;
import ports.out.OpponentInputPort;
import ports.out.PlayerInputPort;

// Composition root: el único lugar que conoce todas las piezas y las conecta.
public class Main {
    public static void main(String[] args) {
        ConsoleMenuAdapter menu = new ConsoleMenuAdapter();
        GameOption option = menu.chooseOption();

        DisplayPort display = new ConsoleDisplayAdapter();
        GameManager gameManager = new GameManager(new Board());

        ChessMatchPort game;
        if (option == GameOption.LOCAL) {
            PlayerInputPort input = new ConsoleInputAdapter();
            game = new ChessService(gameManager, display, input);
        } else {
            PlayerInputPort input = new ConsoleInputAdapter();
            OpponentInputPort opponent = new RandomMovesAI();
            boolean playerIsWhite = menu.choosePlayerColor();
            game = new VsChessService(gameManager, display, input, opponent, playerIsWhite);
        }

        game.runGame();
    }
}
