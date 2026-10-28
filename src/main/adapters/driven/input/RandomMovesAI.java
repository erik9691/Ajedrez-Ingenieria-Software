package main.adapters.driven.input;

import java.util.Random;

import main.core.flags.PromotionId;
import main.core.models.Move;
import main.core.models.Position;
import main.ports.dto.BoardView;
import main.ports.dto.PieceView;
import main.ports.out.PlayerInputPort;

//IA que elige movimientos random
//Provisional

public class RandomMovesAI implements PlayerInputPort {
    private final Random random = new Random();

    @Override
    public Move requestMove(BoardView board) {
        // Tira casillas al azar hasta obtener una jugada plausible; la validez
        // final la decide el GameManager, que la rechaza y el servicio vuelve a pedir.
        while (true) {
            Position start = randomSquare();
            PieceView piece = board.pieceAt(start.row(), start.column());
            if (piece == null) {
                continue;
            }

            Position end = randomSquare();
            if (end.equals(start)) {
                continue;
            }
            PieceView target = board.pieceAt(end.row(), end.column());
            if (target != null && target.isWhite() == piece.isWhite()) {
                continue;
            }

            return new Move(start, end);
        }
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        PromotionId[] options = PromotionId.values();
        return options[random.nextInt(options.length)];
    }

    private Position randomSquare() {
        return new Position(random.nextInt(BoardView.SIZE), random.nextInt(BoardView.SIZE));
    }
}
