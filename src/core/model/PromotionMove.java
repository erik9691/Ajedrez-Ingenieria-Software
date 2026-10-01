package core.model;

import core.interfaces.Movement;
import core.model.Pos;

public record PromotionMove(Pos origin, Pos destination, Piece captured, Piece pawn, Piece promotedTo) implements Movement {}
