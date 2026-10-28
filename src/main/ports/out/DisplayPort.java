package main.ports.out;
import main.core.flags.PlayResult;
import main.ports.dto.BoardView;


public interface DisplayPort {
    void displayGameStart();
    
    // Dibuja el estado actual del tablero (vista de solo lectura).
    void displayBoard(BoardView board);

    // Muestra un mensaje general al jugador (ej. "¡Jaque!", "Juego terminado").
    void displayMessage(PlayResult flag);

    // Muestra de quién es el turno actual.
    void displayTurn(boolean isWhiteTurn);

    // Informa que una jugada fue rechazada y por qué. El adaptador decide el texto.
    void displayMoveRejected(PlayResult reason);

    void displayPromotionMenu(BoardView board);

    void displayGameOver(Boolean isWhiteWinner);
}
