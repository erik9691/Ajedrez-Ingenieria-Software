package core.models;

// Flags de retorno Board → GameManager (ya no basta con un booleano)
public enum MoveResult {
    LEGAL,
    ILLEGAL,
    NO_PIECE,
    OUT_OF_BOUNDS,
    OWN_PIECE_CAPTURE,
    PROMOTION_PENDING,
    SELF_MATE
}
