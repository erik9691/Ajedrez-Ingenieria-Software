package adapters.driven.input;

import core.flags.PromotionId;
import core.models.Move;
import ports.dto.BoardView;
import ports.out.PlayerInputPort;
import adapters.driven.networking.LanConnection;

// El asiento del oponente remoto: cada máquina valida las reglas por su cuenta,
// así que el BoardView se ignora y solo se espera la jugada por la red.
public class RemoteInputAdapter implements PlayerInputPort {
    private final LanConnection connection;

    public RemoteInputAdapter(LanConnection connection) {
        this.connection = connection;
    }

    @Override
    public Move requestMove(BoardView board) {
        return connection.receiveMove();
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        PromotionId promotion = connection.receivePromotion();
        // Resign o desconexión durante la promoción: se completa con dama para
        // no romper el flujo; del otro lado la partida ya terminó.
        return promotion != null ? promotion : PromotionId.QUEEN;
    }
}
