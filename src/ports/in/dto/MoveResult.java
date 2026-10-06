package ports.in.dto;

public record MoveResult(boolean accepted, BoardSnapshot snapshot) {}
