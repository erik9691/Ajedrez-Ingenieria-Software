package main.ports.out;

import main.core.models.Move;
import main.core.flags.PromotionId;
import main.ports.dto.BoardView;

//El input es un puerto 'out', ya que el nucleo (ChessService) decide cuando pedir input.
//El núcleo lo llama
public interface PlayerInputPort {
    Move requestMove(BoardView board);
    PromotionId requestPromotion(BoardView board);
}
