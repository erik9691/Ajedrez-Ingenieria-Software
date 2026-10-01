package core.model;

import core.interfaces.Movement;
import core.model.Pos;

public record CastleMove(Pos kingOrigin, Pos kingDestination, Pos rookOrigin, Pos rookDestination) implements Movement {}
