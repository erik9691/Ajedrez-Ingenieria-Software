package core.interfaces;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import core.model.Color;
import core.model.Pos;

public abstract class GameManager {

    protected Board board;
    protected Color currentTurn;
    protected Deque<Movement> history;

    protected GameManager(Board board) {
        this.history = new ArrayDeque<>();
        this.board = board;
    }

    public abstract void startGame();

    public abstract List<Pos> selectPiece(Pos tile);

    public abstract boolean makeMove(Pos from, Pos to);

    public void undoMove() {
        if (history.isEmpty()) {
            return;
        }
    }

    public Board getBoard() {
        return board;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }
}
