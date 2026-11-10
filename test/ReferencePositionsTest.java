import static org.junit.jupiter.api.Assertions.*;

import main.core.game.*;
import main.core.game.pieces.*;
import main.core.models.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

class ReferencePositionsTest {
    static Stream<String> positions() throws Exception {
        return Files.readAllLines(Path.of("test/resources/legal-positions.txt")).stream()
                .filter(s -> !s.startsWith("#") && !s.isBlank());
    }

    static Position square(String s) {
        return new Position('8' - s.charAt(1), s.charAt(0) - 'a');
    }

    static String square(Position p) {
        return "" + (char) ('a' + p.column()) + (char) ('8' - p.row());
    }

    static Board load(String fen) {
        MoveHistory history = new MoveHistory();
        Board board = new Board(history);
        String[] fields = fen.split(" ");
        String[] rows = fields[0].split("/");
        for (int r = 0; r < 8; r++) {
            int c = 0;
            for (char symbol : rows[r].toCharArray()) {
                if (Character.isDigit(symbol)) {
                    c += symbol - '0';
                    continue;
                }
                boolean white = Character.isUpperCase(symbol);
                Piece piece =
                        switch (Character.toUpperCase(symbol)) {
                            case 'K' -> new King(white);
                            case 'Q' -> new Queen(white);
                            case 'R' -> new Rook(white);
                            case 'B' -> new Bishop(white);
                            case 'N' -> new Knight(white);
                            case 'P' -> new Pawn(white);
                            default -> throw new IllegalArgumentException();
                        };
                Position p = new Position(r, c++);
                board.setPiece(p, piece);
                String rights = fields[2];
                if (piece instanceof King king
                        && !(rights.indexOf(white ? 'K' : 'k') >= 0
                                || rights.indexOf(white ? 'Q' : 'q') >= 0)) king.markAsMoved();
                if (piece instanceof Rook rook
                        && (r != (white ? 7 : 0)
                                || (p.column() != 0 && p.column() != 7)
                                || rights.indexOf(
                                                white
                                                        ? (p.column() == 0 ? 'Q' : 'K')
                                                        : (p.column() == 0 ? 'q' : 'k'))
                                        < 0)) rook.markAsMoved();
            }
        }
        if (!fields[3].equals("-")) {
            Position ep = square(fields[3]);
            boolean movedWhite = fields[1].equals("b");
            Position end = new Position(ep.row() + (movedWhite ? -1 : 1), ep.column());
            Position start = new Position(ep.row() + (movedWhite ? 1 : -1), ep.column());
            history.registerMove(
                    new MoveRecord(new Move(start, end), board.getPiece(end), null, 1));
        }
        return board;
    }

    static Set<String> moves(Board board, boolean white) {
        Set<String> moves = new TreeSet<>();
        for (Move move : board.legalMoves(white)) {
            String uci = square(move.start()) + square(move.end());
            if (board.getPiece(move.start()) instanceof Pawn
                    && (move.end().row() == 0 || move.end().row() == 7))
                for (char promotion : "qrbn".toCharArray()) moves.add(uci + promotion);
            else moves.add(uci);
        }
        return moves;
    }

    @ParameterizedTest(name = "Reference position {index}")
    @MethodSource("positions")
    void legalMovesAndEndingsMatchIndependentReference(String fixture) {
        String[] f = fixture.split("\\|", -1);
        Board board = load(f[0]);
        boolean white = f[0].split(" ")[1].equals("w");
        GameOverChecker checker = new GameOverChecker(board, new MoveHistory());
        Set<String> expected =
                f[1].isEmpty() ? Set.of() : new TreeSet<>(Arrays.asList(f[1].split(",")));
        String before = board.repetitionKey(white);
        Map<Piece, Position> index = board.getPiecePositions();
        assertEquals(expected, moves(board, white), f[0]);
        assertEquals(Boolean.parseBoolean(f[2]), board.isChecked(white), f[0]);
        assertEquals(Boolean.parseBoolean(f[3]), checker.isCheckMate(white), f[0]);
        assertEquals(Boolean.parseBoolean(f[4]), checker.isStalemate(white), f[0]);
        assertEquals(Boolean.parseBoolean(f[5]), checker.isInsufficientMaterial(), f[0]);
        assertEquals(before, board.repetitionKey(white), "Queries must preserve state: " + f[0]);
        assertEquals(index, board.getPiecePositions());
    }

    static Stream<Arguments> geometry() {
        return IntStream.range(0, 768)
                .mapToObj(i -> Arguments.of(i / 128, i / 64 % 2 == 0, i / 8 % 8, i % 8));
    }

    @ParameterizedTest(name = "Piece {0}, white {1}, destination {2},{3}")
    @MethodSource("geometry")
    void emptyBoardGeometry(int type, boolean white, int row, int col) {
        Board board = new Board(new MoveHistory());
        Position start = new Position(4, 3), end = new Position(row, col);
        Piece piece =
                switch (type) {
                    case 0 -> new King(white);
                    case 1 -> new Queen(white);
                    case 2 -> new Rook(white);
                    case 3 -> new Bishop(white);
                    case 4 -> new Knight(white);
                    default -> new Pawn(white);
                };
        board.setPiece(start, piece);
        int dr = Math.abs(row - 4), dc = Math.abs(col - 3);
        boolean expected =
                (dr + dc) > 0
                        && switch (type) {
                            case 0 -> dr <= 1 && dc <= 1;
                            case 1 -> dr == dc || dr == 0 || dc == 0;
                            case 2 -> dr == 0 || dc == 0;
                            case 3 -> dr == dc;
                            case 4 -> dr * dc == 2;
                            default -> dc == 0 && row - 4 == (white ? -1 : 1);
                        };
        assertEquals(
                expected, piece.canMove(board, start, end) != main.core.flags.MoveVerdict.INVALID);
    }
}
