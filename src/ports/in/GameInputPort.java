package ports.in;

import core.flags.PromotionNames;
import core.models.Position;
import ports.dto.InputResult;

public interface GameInputPort {
    // Inicializa las piezas y el estado base del juego.
    void startGame();

    // Intenta mover una pieza. Las coordenadas son del tablero: fila 0 = parte superior (negras).
    // Devuelve true si la jugada se realizó. Si se rechaza, el core lo informa por DisplayPort.
    InputResult makeMove(Position from, Position to);

    // Completa la promoción pendiente con la pieza elegida.
    InputResult promotePawn(PromotionNames choice);

    void endGame(boolean isWhiteWinner);
}
