package ports;

import core.boards.Board;

public interface DisplayPort {
    // Dibuja el estado actual del tablero.
    void displayBoard(Board board);
    
    // Muestra un mensaje general al jugador (ej. "¡Jaque!", "Juego terminado").
    void displayMessage(String message);
    
    // Muestra de quién es el turno actual (Blancas o Negras).
    void displayTurn(boolean isWhiteTurn);
    
    // Muestra un error específico cuando una acción no es válida.
    void displayError(String error);
}