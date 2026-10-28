package main.core.models;

import main.core.game.Piece;

public record MoveRecord(Move move, Piece movedPiece, Piece capturedPiece, int moveNum) {}
