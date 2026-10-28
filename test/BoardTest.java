package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import main.core.game.Board;
import main.core.game.MoveHistory;
import main.core.game.pieces.Knight;
import main.core.models.Position;
import main.core.flags.PlayResult;

public class BoardTest {

    @Test
    public void setPiece_onValidPosition_shouldSaveCorrectly() {
        // Arrange
        Board board = new Board(new MoveHistory());
        Position position = new Position(4, 4);
        Knight knight = new Knight(true);

        // Act
        board.setPiece(position, knight);
        var retrievedPiece = board.getPiece(position);

        // Assert
        assertNotNull(retrievedPiece, "The retrieved piece should not be null");
        assertEquals(knight, retrievedPiece, "The retrieved piece should be the exact knight placed");
    }

    @Test
    public void tryMove_outOfBounds_shouldReturnOutOfBounds() {
        // Arrange
        Board board = new Board(new MoveHistory());
        Position start = new Position(4, 4);
        Position outOfBoundsEnd = new Position(-1, 0);
        Knight knight = new Knight(true);
        board.setPiece(start, knight);

        // Act
        PlayResult result = board.tryMove(start, outOfBoundsEnd);

        // Assert
        assertEquals(PlayResult.OUT_OF_BOUNDS, result, "Moving out of bounds should return PlayResult.OUT_OF_BOUNDS");
    }
}
