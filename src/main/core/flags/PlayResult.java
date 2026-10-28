package main.core.flags;

// Flags del juego que el main.core comunica a los adaptadores (resultados de jugada y avisos).
public enum PlayResult {
    LEGAL,
    ILLEGAL,
    NO_PIECE,
    OUT_OF_BOUNDS,
    OWN_PIECE_CAPTURE,
    PROMOTION_PENDING,
    SELF_MATE,
    CHECK,
    CHECKMATE,
    NOT_YOUR_TURN,
    INVALID_PROMOTION,
    DRAW,
    STALEMATE,
    FORFEIT,
    NONE;

    // true si la jugada fue aceptada, false si fue rechazada.
    public boolean isLegal() {
        return this == LEGAL || this == PROMOTION_PENDING || this == CHECK || this == CHECKMATE || this == DRAW || this == STALEMATE;
    }

    public boolean isFinal() {
        return this == CHECKMATE || this == STALEMATE || this == FORFEIT || this == DRAW;
    }
}
