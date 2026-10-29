import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import main.core.game.Board;
import main.core.game.MoveHistory;
import main.core.game.pieces.Knight;
import main.core.models.Position;
import main.core.flags.MoveVerdict;

public class KnightTest {

    @Test
    public void canMove_lShape_shouldBeValid() {
        // Arrange
        Board board = new Board(new MoveHistory());
        Knight knight = new Knight(true);
        Position start = new Position(4, 4);
        Position end = new Position(6, 5);
        board.setPiece(start, knight);

        // Act
        MoveVerdict verdict = knight.canMove(board, start, end);

        // Assert
        assertEquals(MoveVerdict.VALID, verdict, "Knight moving in L-shape should return MoveVerdict.VALID");
    }

    @Test
    public void canMove_straightLine_shouldBeInvalid() {
        // Arrange
        Board board = new Board(new MoveHistory());
        Knight knight = new Knight(true);
        Position start = new Position(4, 4);
        Position end = new Position(4, 6);
        board.setPiece(start, knight);

        // Act
        MoveVerdict verdict = knight.canMove(board, start, end);

        // Assert
        assertEquals(MoveVerdict.INVALID, verdict, "Knight moving in straight line should return MoveVerdict.INVALID");
    }
}
