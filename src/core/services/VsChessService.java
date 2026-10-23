package core.services;

import core.flags.PlayResult;
import core.flags.PromotionId;
import core.managers.GameManager;
import core.models.Move;
import ports.out.DisplayPort;
import ports.out.OpponentInputPort;
import ports.out.PlayerInputPort;

//Hijo de ChessService, para jugar contra una IA

public class VsChessService extends ChessService{
    private final boolean isPlayerWhite;
    private final OpponentInputPort opponentInput;

    public VsChessService(GameManager gameManager, DisplayPort displayPort, PlayerInputPort playerInputPort, OpponentInputPort opponentInputPort, boolean isPlayerWhite) {
        super(gameManager, displayPort, playerInputPort);
        this.isPlayerWhite = isPlayerWhite;
        this.opponentInput = opponentInputPort;
    }

    @Override 
    public Boolean runGame(){
        super.initializeGame();
        PlayResult result = PlayResult.NONE;

        while(!result.isFinal()){
            if(gameManager.isWhiteTurn() == isPlayerWhite)
                result = super.playTurn();
            else
                result = playOpponentTurn();
        }

        Boolean winner = super.decideWinner(result);
        display.displayGameOver(winner);
        return winner;
    }

    private PlayResult playOpponentTurn(){
        PlayResult currentResult = PlayResult.NONE;

        Move playerMove = opponentInput.chooseMove(gameManager.getBoardView());
        if (playerMove == null) {
            display.displayMessage(PlayResult.FORFEIT);
            return PlayResult.FORFEIT;
        }

        currentResult = gameManager.movePiece(playerMove);

        if (!currentResult.isLegal()) {
            return currentResult;
        }

        display.displayBoard(gameManager.getBoardView());
        display.displayMessage(currentResult);

        while(currentResult == PlayResult.PROMOTION_PENDING ){
            display.displayPromotionMenu(gameManager.getBoardView());
            PromotionId promotedPiece = opponentInput.choosePromotion(gameManager.getBoardView());
            PlayResult promotedResult = gameManager.promotePawn(promotedPiece);

            if (!promotedResult.isLegal()) {
                continue;
            }

            display.displayBoard(gameManager.getBoardView());
            display.displayMessage(promotedResult);
            currentResult = promotedResult;
        }

        return currentResult;
    }
}
