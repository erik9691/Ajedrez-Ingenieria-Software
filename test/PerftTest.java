import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.input.FenEncoder;
import main.core.game.*;
import main.core.game.pieces.*;
import main.core.models.*;
import main.ports.dto.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.*;
import java.util.stream.Stream;

class PerftTest {
    static Stream<String> positions() throws Exception {
        return Files.readAllLines(Path.of("test/resources/perft.txt")).stream();
    }

    static long count(String fen, int depth) {
        if (depth == 0) return 1;
        Board original = ReferencePositionsTest.load(fen);
        boolean white = fen.split(" ")[1].equals("w");
        long total = 0;
        for (Move move : original.legalMoves(white)) {
            Board board = ReferencePositionsTest.load(fen);
            Piece piece = board.getPiece(move.start());
            boolean promotion =
                    piece instanceof Pawn && (move.end().row() == 0 || move.end().row() == 7);
            assertTrue(board.tryMove(move.start(), move.end()).isLegal());
            Position ep =
                    piece instanceof Pawn && Math.abs(move.start().row() - move.end().row()) == 2
                            ? new Position(
                                    (move.start().row() + move.end().row()) / 2,
                                    move.start().column())
                            : null;
            for (int choice = 0; choice < (promotion ? 4 : 1); choice++) {
                if (promotion)
                    board.setPiece(
                            move.end(),
                            switch (choice) {
                                case 0 -> new Queen(white);
                                case 1 -> new Rook(white);
                                case 2 -> new Bishop(white);
                                default -> new Knight(white);
                            });
                BoardView next =
                        new BoardView(
                                board.toView().cells(),
                                !white,
                                board.castlingRights(),
                                ep,
                                0,
                                1,
                                java.util.List.of());
                total += count(FenEncoder.encode(next), depth - 1);
            }
        }
        return total;
    }

    @ParameterizedTest
    @MethodSource("positions")
    void moveTreeMatchesReferenceCounts(String fixture) {
        String[] f = fixture.split("\\|");
        assertEquals(Long.parseLong(f[2]), count(f[0], Integer.parseInt(f[1])));
    }
}
