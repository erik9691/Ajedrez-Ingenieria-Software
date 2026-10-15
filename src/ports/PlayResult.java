package ports;

// Resultado de intentar mover una pieza. Cada adaptador decide cómo comunicarlo al usuario.
public enum PlayResult {
    OK,
    NO_PIECE,
    NOT_YOUR_TURN,
    ILLEGAL_MOVE,
    INVALID_PROMOTION
}
