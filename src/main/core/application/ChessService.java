package main.core.application;

import main.core.flags.PlayResult;
import main.core.flags.PromotionId;
import main.core.managers.GameManager;
import main.core.models.Move;
import main.ports.in.ChessMatchPort;
import main.ports.out.DisplayPort;
import main.ports.out.PlayerInputPort;


//Anteriormente, el loop (while !isGameOver) lo manejaban los adaptadores de Input de consola
//Eso está mal, ya que un adaptador de inputs se encarga de mantener corriendo el juego
//Todo el juego pasa por este service, y este service llama a peticiones de los Input, no al revés

public class ChessService implements ChessMatchPort {
    protected final GameManager game;
    protected final DisplayPort display;
    protected final PlayerInputPort whiteInput;
    protected final PlayerInputPort blackInput;

    public ChessService(GameManager gameManager, DisplayPort displayPort, PlayerInputPort whitePlayerInputPort, PlayerInputPort blackPlayerInputPort) {
        this.game = gameManager;
        this.display = displayPort;
        this.whiteInput = whitePlayerInputPort;
        this.blackInput = blackPlayerInputPort;
    }

    @Override 
    public Boolean runGame(){
        initializeGame();
        PlayResult result = PlayResult.NONE;

        while(!result.isFinal()){
            if(game.isWhiteTurn())
                result = playTurn(whiteInput);
            else
                result = playTurn(blackInput);

        }

        Boolean winner = decideWinner(result);
        display.displayGameOver(winner);
        return winner;
    }


    protected void initializeGame() {
        game.startGame();
        display.displayGameStart();
        display.displayBoard(game.getBoardView());
        display.displayTurn(game.isWhiteTurn());
    }


    protected PlayResult playTurn(PlayerInputPort playerInput){
        PlayResult currentResult = PlayResult.NONE;

        Move playerMove = playerInput.requestMove(game.getBoardView());
        if (playerMove == null) {
            display.displayMessage(PlayResult.FORFEIT);
            return PlayResult.FORFEIT;
        }

        currentResult = game.movePiece(playerMove);

        if (!currentResult.isLegal()) {
            display.displayMoveRejected(currentResult);
            return currentResult;
        }

        display.displayBoard(game.getBoardView());

        while(currentResult == PlayResult.PROMOTION_PENDING ){
            display.displayPromotionMenu(game.getBoardView());
            PromotionId promotedPiece = playerInput.requestPromotion(game.getBoardView());
            PlayResult promotedResult = game.promotePawn(promotedPiece);

            if (!promotedResult.isLegal()) {
                display.displayMoveRejected(promotedResult);
                continue;
            }

            display.displayBoard(game.getBoardView());
            currentResult = promotedResult;
        }

        display.displayMessage(currentResult);

        return currentResult;
    }


    protected Boolean decideWinner(PlayResult lastResult) {
        if(lastResult == PlayResult.CHECKMATE || lastResult == PlayResult.FORFEIT)
            return !game.isWhiteTurn();

        return null;
    }


}
