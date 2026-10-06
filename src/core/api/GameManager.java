package core.api;

import core.models.Position;

public abstract class GameManager {
    protected boolean whiteTurn;
    protected Board board;

    public GameManager(Board board) {
        this.board = board;
    }

    public abstract void startGame();

    public abstract boolean isWhiteTurn();

    public abstract boolean makeMove(Position start, Position end);

    public abstract boolean endGame();


}
