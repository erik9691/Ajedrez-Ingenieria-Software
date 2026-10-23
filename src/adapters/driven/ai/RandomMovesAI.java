package adapters.driven.ai;

import java.util.Random;

import core.flags.PromotionId;
import core.models.Move;
import core.models.Position;
import ports.dto.BoardView;
import ports.dto.PieceView;
import ports.out.OpponentInputPort;

//IA que elige movimientos random
//Provisional

public class RandomMovesAI implements OpponentInputPort {
    private final Random random = new Random();

    @Override
    public Move chooseMove(BoardView board) {
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
    public PromotionId choosePromotion(BoardView board) {
        PromotionId[] options = PromotionId.values();
        return options[random.nextInt(options.length)];
    }

    private Position randomSquare() {
        return new Position(random.nextInt(BoardView.SIZE), random.nextInt(BoardView.SIZE));
    }
}
