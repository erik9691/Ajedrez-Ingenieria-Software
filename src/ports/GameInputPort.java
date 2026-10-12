package ports;

public interface GameInputPort {
    // Inicializa las piezas y el estado base del juego.
    void startGame();

    // Intenta mover una pieza. Las coordenadas son del tablero: fila 0 = parte superior (negras).
    // Devuelve true si la jugada se realizó. Si se rechaza, el core lo informa por DisplayPort.
    boolean movePiece(int fromRow, int fromColumn, int toRow, int toColumn);

    // Verifica si la partida ha finalizado (Jaque Mate)
    boolean isGameOver();
}
