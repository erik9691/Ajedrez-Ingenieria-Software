package ports.out;
import ports.dto.BoardView;
import core.models.Position;
import core.flags.PromotionNames;

public interface ArtificialOpponentPort {
    Position chooseMove(BoardView board, boolean isWhiteTurn);
    PromotionNames choosePromotion(BoardView board, boolean isWhiteTurn);
}
