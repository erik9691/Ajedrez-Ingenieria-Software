package ports.out;

import core.models.Move;
import core.flags.PromotionId;
import ports.dto.BoardView;

public interface PlayerInputPort {
    Move requestMove(BoardView board);
    PromotionId requestPromotion(BoardView board);
}
