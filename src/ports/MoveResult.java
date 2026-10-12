package ports;

// Resultado de intentar mover una pieza. Cada adaptador decide cómo comunicarlo al usuario.
public enum MoveResult {
    OK,
    NO_PIECE,
    NOT_YOUR_TURN,
    ILLEGAL_MOVE
}
