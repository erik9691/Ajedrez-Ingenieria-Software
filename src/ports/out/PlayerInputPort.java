package ports.out;

import core.models.Move;
import core.flags.PromotionId;
import ports.dto.BoardView;

//El input es un puerto 'out', ya que el nucleo (ChessService) decide cuando pedir input.
//El núcleo lo llama
public interface PlayerInputPort {
    Move requestMove(BoardView board);
    PromotionId requestPromotion(BoardView board);
}
