package adapters.driven.input;

import java.io.IOException;

import core.flags.PromotionId;
import core.models.Move;
import ports.dto.BoardView;
import ports.out.PlayerInputPort;
import adapters.driven.networking.LanConnection;

// Decora el input local: cada jugada (o abandono) se reenvía por la red para
// que la máquina remota, bloqueada en RemoteInputAdapter, la reciba.
public class ForwardingInputAdapter implements PlayerInputPort {
    private final PlayerInputPort inner;
    private final LanConnection connection;

    public ForwardingInputAdapter(PlayerInputPort inner, LanConnection connection) {
        this.inner = inner;
        this.connection = connection;
    }

    @Override
    public Move requestMove(BoardView board) {
        Move move = inner.requestMove(board);
        try {
            if (move == null) {
                connection.sendResign();
            } else {
                connection.sendMove(move);
            }
        } catch (IOException e) {
            // El oponente se desconectó: se abandona localmente
            System.err.println("Se perdió la conexión con el oponente: " + e.getMessage());
            return null;
        }
        return move;
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        PromotionId promotion = inner.requestPromotion(board);
        try {
            if (promotion == null) {
                connection.sendResign();
                return PromotionId.QUEEN;
            }
            connection.sendPromotion(promotion);
        } catch (IOException e) {
            System.err.println("Se perdió la conexión con el oponente: " + e.getMessage());
        }
        return promotion;
    }
}
