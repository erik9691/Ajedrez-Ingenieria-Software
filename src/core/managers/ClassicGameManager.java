package core.managers;

import core.interfaces.Board;
import core.interfaces.GameManager;
import core.interfaces.Movement;
import core.interfaces.Piece;
import core.models.Color;
import core.models.Position;
import core.pieces.Bishop;
import core.pieces.King;
import core.pieces.Knight;
import core.pieces.Pawn;
import core.pieces.Queen;
import core.pieces.Rook;

public class ClassicGameManager extends GameManager {

    public ClassicGameManager(Board board) {
        super(board);
    }

    @Override
    public void startGame() {
        board.clear();
        this.initializeBoard();
        currentTurn = Color.WHITE;
        history.clear();
    }

    @Override
    public List<Position> selectPiece(Position tile) {
        return this.board.possibleMoves(tile);
    };

    @Override
    public boolean makeMove(Position from, Position to) {
        if (board.isEmpty(from) || !board.isColor(from, currentTurn))
            return false;

        Movement moveRecord = this.board.move(from, to);
        if (moveRecord != null) {
            super.history.push(moveRecord);
            currentTurn = currentTurn.opposite();
            return true;
        }
        return false;
    };

    private void initializeBoard() {
        for (int col = 0; col < board.getColumns(); col++) {
            board.placePiece(new Pawn(Color.WHITE), new Position(1, col));
            board.placePiece(new Pawn(Color.BLACK), new Position(board.getRows() - 2, col));
        }

        Piece[] whiteBackRank = {
                new Rook(Color.WHITE), new Knight(Color.WHITE), new Bishop(Color.WHITE),
                new Queen(Color.WHITE), new King(Color.WHITE),
                new Bishop(Color.WHITE), new Knight(Color.WHITE), new Rook(Color.WHITE)
        };
        Piece[] blackBackRank = {
                new Rook(Color.BLACK), new Knight(Color.BLACK), new Bishop(Color.BLACK),
                new Queen(Color.BLACK), new King(Color.BLACK),
                new Bishop(Color.BLACK), new Knight(Color.BLACK), new Rook(Color.BLACK)
        };

        for (int col = 0; col < board.getColumns(); col++) {
            board.placePiece(whiteBackRank[col], new Position(0, col));
            board.placePiece(blackBackRank[col], new Position(board.getRows() - 1, col));
        }
    }
}
