package core.model;

import core.interfaces.Movement;
import core.model.Pos;

public record EnPassantMove(Pos origin, Pos destination, Piece captured, Pos capturedAt) implements Movement {}
