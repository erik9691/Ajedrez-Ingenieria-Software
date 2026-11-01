package main.adapters.driven.input;

import java.util.Random;

import main.core.flags.PromotionId;
import main.core.models.Move;
import main.core.models.Position;
import main.core.time.PlayerClock;
import main.ports.dto.BoardView;
import main.ports.dto.InputResult;
import main.ports.dto.PieceView;
import main.ports.dto.PlayerStatus;
import main.ports.out.PlayerInputPort;

//IA que elige movimientos random
//Provisional

public class RandomMovesAI implements PlayerInputPort {
    private static final long TURN_TIME_MS = 5_000; // cada turno descuenta 5 segundos fijos

    private final Random random = new Random();
    private PlayerClock clock;

    @Override
    public void setClock(PlayerClock clock) {
        this.clock = clock;
    }

    @Override
    public InputResult requestMove(BoardView board) {
        if (clock != null) {
            if (clock.isExpired()) {
                return new InputResult(null, PlayerStatus.TIME_OVER);
            }
            clock.reduceTime(TURN_TIME_MS);
        }

        // Tira casillas al azar hasta obtener una jugada plausible; la validez
        // final la decide el GameManager, que la rechaza y el servicio vuelve a pedir.
        while (true) {
            Position start = randomSquare();

            PieceView piece = board.pieceAt(start.row(), start.column());

            if (piece == null) { //Origen debe tener pieza
                continue;
            }

            Position end = randomSquare(); 
            if (end.equals(start)) { //el destino debe ser distinto
                continue;
            }

            PieceView target = board.pieceAt(end.row(), end.column());

            if (target != null && target.isWhite() == piece.isWhite()) { //no tener una pieza del mismo color que la elegida
                continue;
            }

            return new InputResult(new Move(start, end), PlayerStatus.OK);
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
