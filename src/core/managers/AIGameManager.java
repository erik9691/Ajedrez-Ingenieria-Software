package core.managers;

import core.boards.Board;
import core.flags.PlayResult;
import core.flags.PromotionNames;
import core.models.Position;
import ports.out.ArtificialOpponentPort;

// GameManager que en el turno de la IA le pide la jugada al oponente artificial.
// El resto de las reglas se heredan de GameManager sin cambios.

// Falta implementar algun adapter de IA, asi que todavia no se usa.
public class AIGameManager extends GameManager {
    private final boolean humanIsWhite;
    private final ArtificialOpponentPort opponent;

    public AIGameManager(Board board, boolean humanIsWhite, ArtificialOpponentPort opponent) {
        super(board);
        this.humanIsWhite = humanIsWhite;
        this.opponent = opponent;
    }

    @Override
    public PlayResult movePiece(Position start, Position end) {
        // Turno del humano: se comporta como el GameManager base
        if (isWhiteTurn() == humanIsWhite) {
            return super.movePiece(start, end);
        }

        // Turno de la IA: el oponente elige la casilla destino y se mueve
        // la primera pieza propia que pueda llegar legalmente a ella
        Position target = opponent.chooseMove(getBoardView(), isWhiteTurn());
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                PlayResult result = super.movePiece(new Position(row, column), target);
                if (result.isLegal()) {
                    return result;
                }
            }
        }
        return PlayResult.ILLEGAL;
    }

    @Override
    public PlayResult promotePawn(PromotionNames choice) {
        // En la promoción el turno todavía no cambió: isWhiteTurn() es quien promociona
        if (isWhiteTurn() == humanIsWhite) {
            return super.promotePawn(choice);
        }
        return super.promotePawn(opponent.choosePromotion(getBoardView(), isWhiteTurn()));
    }
}
