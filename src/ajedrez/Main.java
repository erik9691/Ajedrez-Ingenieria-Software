package ajedrez;

import adapters.ConsoleDisplayAdapter;
import adapters.ConsoleInputAdapter;
import core.boards.Board;
import core.managers.GameManager;
import ports.DisplayPort;
import ports.GameInputPort;

// Composition root: el único lugar que conoce todas las piezas y las conecta.
public class Main {
    public static void main(String[] args) {
        // Adaptador de salida (implementa el puerto que el core necesita)
        DisplayPort display = new ConsoleDisplayAdapter();

        // Core del juego, recibe sus dependencias desde afuera
        GameInputPort game = new GameManager(new Board(), display);

        // Adaptador de entrada: usa el puerto de entrada del core
        new ConsoleInputAdapter(game).run();
    }
}
