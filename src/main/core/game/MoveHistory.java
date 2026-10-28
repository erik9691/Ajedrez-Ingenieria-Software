package main.core.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import main.core.models.MoveRecord;

public class MoveHistory {
    private final Deque<MoveRecord> moves = new ArrayDeque<>();

    public void registerMove(MoveRecord move) {
        moves.push(move);
    }

    public void clear() {
        moves.clear();
    }

    public List<MoveRecord> getNMoves(int n) {
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
