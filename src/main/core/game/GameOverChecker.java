package main.core.game;

import main.core.game.pieces.*;

import java.util.*;

public class GameOverChecker {
    private final Board board;
    private final Map<String, Integer> positions = new HashMap<>();
    private int movesWithoutProgress;

    public GameOverChecker(Board board, MoveHistory history) {
        this.board = Objects.requireNonNull(board);
    }

    public boolean isCheckMate(boolean white) {
        return board.isChecked(white) && board.legalMoves(white).isEmpty();
    }

    public boolean isStalemate(boolean white) {
        return board.findKingPosition(white) != null
                && !board.isChecked(white)
                && board.legalMoves(white).isEmpty();
    }

    public boolean isDraw() {
        return movesWithoutProgress >= 100
                || isInsufficientMaterial()
                || positions.values().stream().anyMatch(n -> n >= 3);
    }

    public void incrementHalfmoveClock() {
        movesWithoutProgress++;
    }

    public void resetHalfmoveClock() {
        movesWithoutProgress = 0;
    }

    public int halfmoveClock() {
        return movesWithoutProgress;
    }

    public void resetPositions() {
        positions.clear();
    }

    public void recordPosition(boolean white) {
        positions.merge(board.repetitionKey(white), 1, Integer::sum);
    }

    public boolean isInsufficientMaterial() {
        List<Piece> pieces =
                board.getPiecePositions().keySet().stream()
                        .filter(p -> !(p instanceof King))
                        .toList();
        if (pieces.isEmpty()) return true;
        if (pieces.size() == 1)
            return pieces.get(0) instanceof Bishop || pieces.get(0) instanceof Knight;
        if (pieces.stream().anyMatch(p -> !(p instanceof Bishop))) return false;
        return pieces.stream()
                        .map(p -> board.getPiecePositions().get(p))
                        .map(p -> (p.row() + p.column()) % 2)
                        .distinct()
                        .count()
                == 1;
    }
}
