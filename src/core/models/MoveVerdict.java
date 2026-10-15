package core.models;

// Flags de retorno Piece → Board (ya no basta con un booleano)
public enum MoveVerdict {
    VALID,
    INVALID,
    CASTLE,
    PROMOTION,
    EN_PASSANT
}
