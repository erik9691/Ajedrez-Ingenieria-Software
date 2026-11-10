package main.core.game;

import main.core.models.MoveRecord;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MoveHistory {
    private final Deque<MoveRecord> moves = new ArrayDeque<>();

    public void registerMove(MoveRecord move) {
        moves.push(move);
    }

    public void clear() {
        moves.clear();
    }

    public List<MoveRecord> getNMoves(int n) {
        if (n < 0) throw new IllegalArgumentException("Cantidad negativa");
        List<MoveRecord> last = new ArrayList<>();
        for (MoveRecord move : moves) {
            if (last.size() == n) {
                break;
            }
            last.addFirst(move);
        }
        return last;
    }
}
