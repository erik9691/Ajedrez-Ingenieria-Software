package main.core.application;

import main.ports.in.MatchRunnerPort;
import main.core.flags.PlayResult;
import main.core.flags.PromotionId;
import main.core.managers.GameManager;
import main.core.models.Move;
import main.core.time.PlayerClock;
import main.ports.dto.ClockView;
import main.ports.dto.InputResult;
import main.ports.dto.PlayerStatus;
import main.ports.out.DisplayPort;
import main.ports.out.PlayerInputPort;


//Anteriormente, el loop (while !isGameOver) lo manejaban los adaptadores de Input de consola
//Eso está mal, ya que un adaptador de inputs se encarga de mantener corriendo el juego
//Todo el juego pasa por este service, y este service llama a peticiones de los Input, no al revés

public class ChessService implements MatchRunnerPort{
    private final GameManager game;
    private final DisplayPort display;
    private final PlayerInputPort whiteInput;
    private final PlayerInputPort blackInput;
    private final PlayerClock whiteClock;
    private final PlayerClock blackClock;

    public ChessService(GameManager gameManager,
                        DisplayPort displayPort,
                        PlayerInputPort whitePlayerInputPort, PlayerInputPort blackPlayerInputPort,
                        PlayerClock time) {
        this.game = gameManager;
        this.display = displayPort;
        this.whiteInput = whitePlayerInputPort;
        this.blackInput = blackPlayerInputPort;
        
        //Copiado para asegurar que ambos jugadores tengan el mismo reloj
        if(time != null){
            this.whiteClock = time.copy();
            this.blackClock = time.copy();
        } else{
            this.whiteClock = null;
            this.blackClock = null;
        }
    }

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


    private void initializeGame() {
        game.startGame();
        display.displayGameStart();
        display.displayBoard(game.getBoardView(), createClockView());
        display.displayTurn(game.isWhiteTurn());

        //los relojes pueden venir vacios (sin limite de tiempo)
        if(whiteClock != null && blackClock != null){
            whiteInput.setClock(whiteClock);
            blackInput.setClock(blackClock);
        }
    }


    private PlayResult playTurn(PlayerInputPort playerInput){
        PlayResult currentResult = PlayResult.NONE;

        InputResult playerMove = playerInput.requestMove(game.getBoardView());

        if (playerMove.status() == PlayerStatus.FORFEIT) {
            display.displayMessage(PlayResult.FORFEIT); //el jugador abandono basicamente
            return PlayResult.FORFEIT;
        }

        if (playerMove.status() == PlayerStatus.TIME_OVER) {
            display.displayMessage(PlayResult.TIME_OVER); //el jugador se quedo sin tiempo
            return PlayResult.FORFEIT;
        }

        currentResult = game.movePiece(playerMove.move());

        if (!currentResult.isLegal()) {
            display.displayMoveRejected(currentResult);
            return currentResult;
        }

        display.displayBoard(game.getBoardView(), createClockView());

        while(currentResult == PlayResult.PROMOTION_PENDING ){
            display.displayPromotionMenu(game.getBoardView());
            PromotionId promotedPiece = playerInput.requestPromotion(game.getBoardView());
            PlayResult promotedResult = game.promotePawn(promotedPiece);

            if (!promotedResult.isLegal()) {
                display.displayMoveRejected(promotedResult);
                continue;
            }

            display.displayBoard(game.getBoardView(), createClockView());
            currentResult = promotedResult;
        }

        display.displayMessage(currentResult);

        return currentResult;
    }


    private Boolean decideWinner(PlayResult lastResult) {
        if(lastResult == PlayResult.CHECKMATE || lastResult == PlayResult.FORFEIT || lastResult == PlayResult.TIME_OVER)
            return !game.isWhiteTurn();

        return null;
    }


    // Snapshot de los relojes para el display; null si la partida no tiene reloj.
    private ClockView createClockView() {
        if (whiteClock == null) {
            return null;
        }
        return new ClockView(whiteClock.remainingMillis(), blackClock.remainingMillis(), game.isWhiteTurn());
    }


}
