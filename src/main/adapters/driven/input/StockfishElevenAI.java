package main.adapters.driven.input;

import main.core.flags.PromotionId;
import main.core.models.*;
import main.core.time.PlayerClock;
import main.ports.dto.*;
import main.ports.out.PlayerInputPort;

import java.io.IOException;

public class StockfishElevenAI implements PlayerInputPort {
    private EngineConnection engine;
    private final RandomMovesAI fallback = new RandomMovesAI();
    private PlayerClock clock;
    private PromotionId promotion = PromotionId.QUEEN;

    public StockfishElevenAI(boolean playsWhite, String path) {
        try {
            engine = new UciEngine(path);
        } catch (IOException e) {
            System.err.println(
                    "Stockfish no disponible; se usara la IA aleatoria: " + e.getMessage());
        }
    }

    public StockfishElevenAI(boolean playsWhite, EngineConnection engine) {
        this.engine = engine;
    }

    @Override
    public void setClock(PlayerClock clock) {
        this.clock = clock;
        fallback.setClock(clock);
    }

    @Override
    public InputResult requestMove(BoardView board) {
        promotion = PromotionId.QUEEN;
        if (clock != null && clock.isExpired())
            return new InputResult(null, PlayerStatus.TIME_OVER);
        if (board.legalMoves().isEmpty()) return new InputResult(null, PlayerStatus.FORFEIT);
        if (engine == null) return fallback.requestMove(board);
        try {
            String response =
                    engine.request(
                            "position fen " + FenEncoder.encode(board) + "\ngo movetime 500",
                            "bestmove ",
                            3000);
            if (response == null || !response.startsWith("bestmove "))
                return fallback.requestMove(board);
            String text = response.substring(9).trim().split("\\s+")[0];
            if (!text.matches("[a-h][1-8][a-h][1-8][qrbn]?")) return fallback.requestMove(board);
            Move move = new Move(square(text.substring(0, 2)), square(text.substring(2, 4)));
            if (!board.legalMoves().contains(move)) return fallback.requestMove(board);
            boolean promotes =
                    board.pieceAt(move.start().row(), move.start().column()).symbol() == 'P'
                            && (move.end().row() == 0 || move.end().row() == 7);
            if (promotes != (text.length() == 5)) return fallback.requestMove(board);
            if (promotes)
                promotion =
                        switch (text.charAt(4)) {
                            case 'r' -> PromotionId.ROOK;
                            case 'b' -> PromotionId.BISHOP;
                            case 'n' -> PromotionId.KNIGHT;
                            default -> PromotionId.QUEEN;
                        };
            return new InputResult(move, PlayerStatus.OK);
        } catch (IOException e) {
            engine.close();
            engine = null;
            return fallback.requestMove(board);
        }
    }

    private static Position square(String s) {
        return new Position('8' - s.charAt(1), s.charAt(0) - 'a');
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        return promotion;
    }

    @Override
    public void close() {
        if (engine != null) {
            engine.close();
            engine = null;
        }
    }
}
