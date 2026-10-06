package adapter.driving.consolePlay;

import java.util.Scanner;

import core.api.Board;
import core.boards.ClassicBoard;
import core.managers.ClassicGameManager;
import core.models.Position;
import ports.in.GameUseCase;
import ports.in.dto.BoardSnapshot;
import ports.in.dto.GameStatus;
import ports.in.dto.MoveResult;
import ports.in.dto.PieceView;
import ports.in.service.GameService;

public class ConsolePlayAdapter {

    public static void main(String[] args) {
        Board board = new ClassicBoard();
        GameUseCase game = new GameService(new ClassicGameManager(board), board);
        game.startGame();

        Scanner scanner = new Scanner(System.in);
        BoardSnapshot snapshot = game.getSnapshot();
        printBoard(snapshot);

        while (snapshot.status() == GameStatus.IN_PROGRESS) {
            System.out.println(snapshot.whiteTurn() ? "Turno: blancas" : "Turno: negras");
            if (!scanner.hasNextLine()) {
                break;
            }
            String[] parts = scanner.nextLine().trim().split("\\s+");
            MoveResult result = null;
            if (parts.length == 2) {
                Position from = parseSquare(parts[0]);
                Position to = parseSquare(parts[1]);
                if (from != null && to != null) {
                    result = game.move(from, to);
                }
            }
            if (result == null || !result.accepted()) {
                System.out.println("Movimiento inválido");
            }
            snapshot = game.getSnapshot();
            printBoard(snapshot);
        }
        System.out.println(snapshot.status() == GameStatus.WHITE_WINS ? "Ganan las blancas" : "Ganan las negras");
        scanner.close();
    }

    private static Position parseSquare(String token) {
        if (token.length() != 2) {
            return null;
        }
        int column = token.charAt(0) - 'a';
        int row = 8 - (token.charAt(1) - '0');
        if (column < 0 || column > 7 || row < 0 || row > 7) {
            return null;
        }
        return new Position(row, column);
    }

    private static void printBoard(BoardSnapshot snapshot) {
        PieceView[][] squares = snapshot.squares();
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " ");
            for (int column = 0; column < 8; column++) {
                PieceView piece = squares[row][column];
                System.out.print(piece == null ? "□ " : symbol(piece) + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }

    private static String symbol(PieceView piece) {
        char white = switch (piece.type()) {
            case KING -> '♔';
            case QUEEN -> '♕';
            case ROOK -> '♖';
            case BISHOP -> '♗';
            case KNIGHT -> '♘';
            case PAWN -> '♙';
        };
        return String.valueOf(piece.isWhite() ? white : (char) (white + 6));
    }
}
