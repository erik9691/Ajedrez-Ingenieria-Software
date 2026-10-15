package ports;

public interface GameInputPort {
    // Inicializa las piezas y el estado base del juego.
    void startGame();

    // Intenta mover una pieza. Las coordenadas son del tablero: fila 0 = parte superior (negras).
    // Devuelve true si la jugada se realizó. Si se rechaza, el core lo informa por DisplayPort.
    boolean movePiece(int fromRow, int fromColumn, int toRow, int toColumn);

    // Informa si hay una promoción esperando la elección de pieza.
    boolean isPromotionPending();

    // Completa la promoción pendiente con la pieza elegida (Q, R, B o N).
    // Si la elección es inválida, el core lo informa por DisplayPort.
    boolean promotePawn(char choice);

    // Verifica si la partida ha finalizado (Jaque Mate)
    boolean isGameOver();
}
