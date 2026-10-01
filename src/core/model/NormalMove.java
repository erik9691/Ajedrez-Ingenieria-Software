package core.model;

import core.interfaces.Movement;
import core.model.Pos;

public record NormalMove(Pos origin, Pos destination, Piece captured) implements Movement {}
