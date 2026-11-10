package main.ports.out;

import main.core.flags.PromotionId;
import main.core.time.PlayerClock;
import main.ports.dto.BoardView;
import main.ports.dto.InputResult;

// El input es un puerto 'out', ya que el nucleo (ChessService) decide cuando pedir input.
// El núcleo lo llama
public interface PlayerInputPort extends AutoCloseable {
    @Override
    default void close() {}

    InputResult requestMove(BoardView board);

    PromotionId requestPromotion(BoardView board);

    void setClock(PlayerClock clock);
}
