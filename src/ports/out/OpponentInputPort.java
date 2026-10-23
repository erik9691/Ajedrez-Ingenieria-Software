package ports.out;
import ports.dto.BoardView;
import core.models.Move;
import core.flags.PromotionId;

public interface OpponentInputPort {
    Move chooseMove(BoardView board);
    PromotionId choosePromotion(BoardView board);
}
