package ajedrez;

import adapters.driven.display.ConsoleDisplayAdapter;
import adapters.driving.input.ConsoleInputAdapter;
import core.ChessService;
import core.boards.Board;
import core.managers.GameManager;
import ports.out.DisplayPort;
import ports.in.GameInputPort;

// Composition root: el único lugar que conoce todas las piezas y las conecta.
public class Main {
    public static void main(String[] args) {
        // Adaptador de salida (implementa el puerto que el core necesita)
        DisplayPort display = new ConsoleDisplayAdapter();

        // Core del juego, recibe sus dependencias desde afuera
        GameManager gameManager = new GameManager(new Board());
        GameInputPort game = new ChessService(gameManager, display);

        // Adaptador de entrada: usa el puerto de entrada del core
        new ConsoleInputAdapter(game).run();
    }
}
