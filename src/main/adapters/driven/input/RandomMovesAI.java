package main.adapters.driven.input;

import main.core.flags.PromotionId;
import main.core.time.PlayerClock;
import main.ports.dto.*;
import main.ports.out.PlayerInputPort;

import java.util.Objects;
import java.util.Random;

public class RandomMovesAI implements PlayerInputPort {
    private final Random random;
    private PlayerClock clock;

    public RandomMovesAI() {
        this(new Random());
    }

    public RandomMovesAI(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public void setClock(PlayerClock clock) {
        this.clock = clock;
    }

    @Override
    public InputResult requestMove(BoardView board) {
        if (clock != null && clock.isExpired())
            return new InputResult(null, PlayerStatus.TIME_OVER);
        var moves = board.legalMoves();
        if (moves.isEmpty()) return new InputResult(null, PlayerStatus.FORFEIT);
        return new InputResult(moves.get(random.nextInt(moves.size())), PlayerStatus.OK);
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        PromotionId[] choices = PromotionId.values();
        return choices[random.nextInt(choices.length)];
    }
}
