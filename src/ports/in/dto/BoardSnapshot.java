package ports.in.dto;

public record BoardSnapshot(PieceView[][] squares, boolean whiteTurn, GameStatus status) {}

