package ports.in;

import core.models.Position;
import ports.in.dto.BoardSnapshot;
import ports.in.dto.MoveResult;

public interface GameUseCase {
    void startGame();
    MoveResult move(Position from, Position to);
    BoardSnapshot getSnapshot();
}
