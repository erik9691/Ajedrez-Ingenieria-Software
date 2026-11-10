package main.core.application;

import main.core.flags.*;
import main.core.managers.GameManager;
import main.core.time.PlayerClock;
import main.ports.dto.*;
import main.ports.in.MatchRunnerPort;
import main.ports.out.*;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class ChessService implements MatchRunnerPort {
    private final GameManager game;
    private final DisplayPort display;
    private final PlayerInputPort whiteInput, blackInput;
    private final PlayerClock whiteClock, blackClock;
    private final ExecutorService inputWorker =
            Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "chess-player-input");
                thread.setDaemon(true);
                return thread;
            });

    public ChessService(
            GameManager game,
            DisplayPort display,
            PlayerInputPort white,
            PlayerInputPort black,
            PlayerClock time) {
        this.game = game;
        this.display = display;
        this.whiteInput = white;
        this.blackInput = black;
        whiteClock = time == null ? null : time.copy();
        blackClock = time == null ? null : time.copy();
    }

    @Override
    public Boolean runGame() {
        try {
            game.startGame();
            whiteInput.setClock(whiteClock);
            blackInput.setClock(blackClock);
            display.displayGameStart();
            display.displayBoard(game.getBoardView(), clockView());
            PlayResult result = PlayResult.NONE;
            while (!result.isFinal()) {
                display.displayTurn(game.isWhiteTurn());
                result =
                        playTurn(
                                game.isWhiteTurn() ? whiteInput : blackInput,
                                game.isWhiteTurn() ? whiteClock : blackClock);
            }
            Boolean winner =
                    (result == PlayResult.CHECKMATE
                                    || result == PlayResult.FORFEIT
                                    || result == PlayResult.TIME_OVER)
                            ? !game.isWhiteTurn()
                            : null;
            display.displayGameOver(winner);
            return winner;
        } finally {
            inputWorker.shutdownNow();
            try {
                whiteInput.close();
            } finally {
                try {
                    if (blackInput != whiteInput) blackInput.close();
                } finally {
                    display.close();
                }
            }
        }
    }

    private PlayResult playTurn(PlayerInputPort input, PlayerClock clock) {
        try {
            InputResult next = read(() -> input.requestMove(game.getBoardView()), clock);
            if (next == null || next.status() == PlayerStatus.FORFEIT)
                return announce(PlayResult.FORFEIT);
            if (next.status() == PlayerStatus.TIME_OVER) return announce(PlayResult.TIME_OVER);
            PlayResult result = game.movePiece(next.move());
            if (!result.isLegal()) {
                display.displayMoveRejected(result);
                return result;
            }
            display.displayBoard(game.getBoardView(), clockView());
            while (result == PlayResult.PROMOTION_PENDING) {
                display.displayPromotionMenu(game.getBoardView());
                PromotionId promotion =
                        read(() -> input.requestPromotion(game.getBoardView()), clock);
                if (promotion == null) return announce(PlayResult.FORFEIT);
                result = game.promotePawn(promotion);
                if (!result.isLegal()) {
                    display.displayMoveRejected(result);
                    result = PlayResult.PROMOTION_PENDING;
                    continue;
                }
                display.displayBoard(game.getBoardView(), clockView());
            }
            return announce(result);
        } catch (TimeExpired e) {
            return announce(PlayResult.TIME_OVER);
        }
    }

    private <T> T read(Supplier<T> request, PlayerClock clock) {
        if (clock == null) return request.get();
        if (clock.isExpired()) throw new TimeExpired();
        clock.resume();
        Future<T> pending = inputWorker.submit(request::get);
        try {
            T answer = pending.get(Math.max(1, clock.remainingMillis()), TimeUnit.MILLISECONDS);
            if (clock.isExpired()) throw new TimeExpired();
            return answer;
        } catch (TimeoutException e) {
            throw new TimeExpired();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Partida interrumpida", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            if (e.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(e.getCause());
        } finally {
            pending.cancel(true);
            clock.pause();
        }
    }

    private PlayResult announce(PlayResult result) {
        display.displayMessage(result);
        return result;
    }

    private ClockView clockView() {
        return whiteClock == null
                ? null
                : new ClockView(
                        whiteClock.remainingMillis(),
                        blackClock.remainingMillis(),
                        game.isWhiteTurn());
    }

    private static class TimeExpired extends RuntimeException {}
}
