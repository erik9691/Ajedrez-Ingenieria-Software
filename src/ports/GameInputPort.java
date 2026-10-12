package ports;

import core.models.Position;

public interface GameInputPort {
    // Inicializa las piezas y el estado base del juego.
    void startGame();
    
    // Intenta mover una pieza.
    boolean movePiece(Position from, Position to);
    
    // Verifica si la partida ha finalizado (Jaque Mate)
    boolean isGameOver();
}