package main.ports.out;

import main.core.flags.PlayResult;
import main.ports.dto.BoardView;
import main.ports.dto.ClockView;

public interface DisplayPort extends AutoCloseable {
    @Override
    default void close() {}

    void displayGameStart();

    // Dibuja el estado actual del tablero y los relojes (vista de solo lectura).
    // clock es null si la partida no tiene control de tiempo.
    void displayBoard(BoardView board, ClockView clock);

    // Muestra un mensaje general al jugador (ej. "¡Jaque!", "Juego terminado").
    void displayMessage(PlayResult flag);

    // Muestra de quién es el turno actual.
    void displayTurn(boolean isWhiteTurn);

    // Informa que una jugada fue rechazada y por qué. El adaptador decide el texto.
    void displayMoveRejected(PlayResult reason);

    void displayPromotionMenu(BoardView board);

    void displayGameOver(Boolean isWhiteWinner);
}
