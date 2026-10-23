package core;

import core.flags.PlayResult;
import core.flags.PromotionNames;
import core.managers.GameManager;
import core.models.Position;
import ports.dto.InputResult;
import ports.in.GameInputPort;
import ports.out.DisplayPort;


//GameManager estaba teniendo muchas responsabilidades.
//GM se encargara de la logica, este service es el intermediario con el resto de los adapters.
//Tambien sirve para no repetir codigo en el IA_GameManager
public class ChessService implements GameInputPort {
    private final GameManager gameManager;
    private final DisplayPort displayPort;

    public ChessService(GameManager gameManager, DisplayPort displayPort) {
        this.gameManager = gameManager;
        this.displayPort = displayPort;
    }

    @Override
    public void startGame() {
        gameManager.startGame();
        displayPort.displayGameStart();
        displayPort.displayBoard(gameManager.getBoardView());
        displayPort.displayTurn(gameManager.isWhiteTurn());
    }

    @Override
    public InputResult makeMove(Position from, Position to) {
        PlayResult result = gameManager.movePiece(from, to);
        if (!result.isLegal()) {
            displayPort.displayMoveRejected(result);
            return InputResult.REJECTED;
        }
        displayPort.displayBoard(gameManager.getBoardView());
        // Con promoción pendiente el turno se completa recién en promotePawn
        if (result == PlayResult.PROMOTION_PENDING) {
            return InputResult.PROMOTION_PENDING;
        }
        
        displayPort.displayMessage(result);
        if(result == PlayResult.CHECKMATE) {
            endGame(!gameManager.isWhiteTurn());
            return InputResult.GAME_OVER;
        } 
        
        return InputResult.ACCEPTED;
    }

    @Override
    public InputResult promotePawn(PromotionNames choice) {
        PlayResult result = gameManager.promotePawn(choice);
        if (!result.isLegal()) {
            displayPort.displayMoveRejected(result);
            return InputResult.REJECTED;
        }
        displayPort.displayBoard(gameManager.getBoardView());
        displayPort.displayMessage(result);
        if(result == PlayResult.CHECKMATE) {
            endGame(!gameManager.isWhiteTurn());
            return InputResult.GAME_OVER;
        } 
        return InputResult.ACCEPTED;
    }
    

    public void endGame(boolean isWhiteWinner) {
        displayPort.displayGameOver(isWhiteWinner);
    }


}
