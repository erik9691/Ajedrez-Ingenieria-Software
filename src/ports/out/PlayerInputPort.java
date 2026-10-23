package ports.out;

import core.models.Move;
import core.flags.PromotionId;

public interface PlayerInputPort {
    Move requestMove();
    PromotionId requestPromotion();
}
