package ports.in.service;

import core.api.Board;
import core.api.GameManager;
import core.api.Piece;
import core.models.Position;
import core.pieces.*;
import ports.in.GameUseCase;
import ports.in.dto.BoardSnapshot;
import ports.in.dto.GameStatus;
import ports.in.dto.MoveResult;
import ports.in.dto.PieceView;
import ports.in.dto.PieceType;



public class GameService implements GameUseCase {
    private final GameManager manager;
    private final Board board;

    public GameService(GameManager manager, Board board) {
        this.manager = manager;
        this.board = board;
    }

    @Override
    public void startGame() {
        manager.startGame();
    }

    @Override
    public MoveResult move(Position from, Position to) {
        boolean ok = manager.makeMove(from, to);
        return new MoveResult(ok, getSnapshot());
    }

    @Override
    public BoardSnapshot getSnapshot() {
        PieceView[][] squares = new PieceView[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                squares[r][c] = (p == null) ? null : toView(p);
            }
        }
        return new BoardSnapshot(squares, manager.isWhiteTurn(), computeStatus());
    }

    private GameStatus computeStatus() {
        if (!manager.endGame()) {
            return GameStatus.IN_PROGRESS;
        }
        return manager.isWhiteTurn() ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS;
    }

    private PieceView toView(Piece p) {
        PieceType type = switch (p) {
            case Pawn x -> PieceType.PAWN;
            case Rook x -> PieceType.ROOK;
            case Knight x -> PieceType.KNIGHT;
            case Bishop x -> PieceType.BISHOP;
            case Queen x -> PieceType.QUEEN;
            case King x -> PieceType.KING;
            default -> throw new IllegalStateException("Unknown piece");
        };
        return new PieceView(type, p.getColor());
    }
}