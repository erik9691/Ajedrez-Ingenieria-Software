package core.services;

import core.flags.PlayResult;
import core.flags.PromotionId;
import core.managers.GameManager;
import core.models.Move;
import ports.in.ChessMatchPort;
import ports.out.DisplayPort;
import ports.out.PlayerInputPort;


//Anteriormente, el loop (while !isGameOver) lo manejaban los adaptadores de Input de consola
//Eso está mal, ya que un adaptador de inputs se encarga de mantener corriendo el juego
//Todo el juego pasa por este service, y este service llama a peticiones de los Input, no al revés

public class ChessService implements ChessMatchPort {
    protected final GameManager gameManager;
    protected final DisplayPort display;
    protected final PlayerInputPort playerInput;

    public ChessService(GameManager gameManager, DisplayPort displayPort, PlayerInputPort playerInputPort) {
        this.gameManager = gameManager;
        this.display = displayPort;
        this.playerInput = playerInputPort;
    }

    @Override 
    public Boolean runGame(){
        initializeGame();
        PlayResult result = PlayResult.NONE;

        while(!result.isFinal()){
            result = playTurn();
        }

        Boolean winner = decideWinner(result);
        display.displayGameOver(winner);
        return winner;
    }


    protected void initializeGame() {
        gameManager.startGame();
        display.displayGameStart();
        display.displayBoard(gameManager.getBoardView());
        display.displayTurn(gameManager.isWhiteTurn());
    }


    protected PlayResult playTurn(){
        PlayResult currentResult = PlayResult.NONE;

        Move playerMove = playerInput.requestMove();
        if (playerMove == null) {
            display.displayMessage(PlayResult.FORFEIT);
            return PlayResult.FORFEIT;
        }

        currentResult = gameManager.movePiece(playerMove);

        if (!currentResult.isLegal()) {
            display.displayMoveRejected(currentResult);
            return currentResult;
        }

        display.displayBoard(gameManager.getBoardView());
        display.displayMessage(currentResult);

        while(currentResult == PlayResult.PROMOTION_PENDING ){
            display.displayPromotionMenu(gameManager.getBoardView());
            PromotionId promotedPiece = playerInput.requestPromotion();
            PlayResult promotedResult = gameManager.promotePawn(promotedPiece);

            if (!promotedResult.isLegal()) {
                display.displayMoveRejected(promotedResult);
                continue;
            }

            display.displayBoard(gameManager.getBoardView());
            display.displayMessage(promotedResult);
            currentResult = promotedResult;
        }

        return currentResult;
    }


    protected Boolean decideWinner(PlayResult lastResult) {
        if(lastResult == PlayResult.CHECKMATE || lastResult == PlayResult.FORFEIT)
            return !gameManager.isWhiteTurn();

        return null;
    }


}
