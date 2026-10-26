package adapters.driven.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import core.flags.PromotionId;
import core.models.Move;
import core.models.Position;
import ports.dto.BoardView;
import ports.dto.PieceView;
import ports.out.PlayerInputPort;

//IA que delega en el motor Stockfish 11 por protocolo UCI (stdin/stdout).
public class StockfishElevenAI implements PlayerInputPort {
    
    private static final long MOVE_TIME_MS = 500;

    private String enginePath;
    private final boolean playsWhite;
    private final RandomMovesAI fallback = new RandomMovesAI();

    private PrintWriter engineIn;
    private BufferedReader engineOut;

    // FEN enviado en la jugada anterior: si se repite, la jugada fue rechazada
    private String lastFen = "";
    private int repeatCount = 0;
    private char pendingPromotion = '\0';

    public StockfishElevenAI(boolean playsWhite, String path) {
        this.playsWhite = playsWhite;
        this.enginePath = path;
        startEngine();
    }

    @Override
    public Move requestMove(BoardView board) {
        String canonicalFen = toFen(board, true);
        String fenToSend;

        // El GameManager rechazó la jugada anterior: se vuelve a pedir la misma posición
        if (canonicalFen.equals(lastFen)) {
            repeatCount++;
            if (repeatCount >= 2) {
                return randomFallbackMove(board);
            }
            // El enroque heurístico es lo único del FEN que puede mentir; sin
            // derechos el motor solo propone jugadas legales en la partida real
            fenToSend = toFen(board, false);
        } else {
            repeatCount = 0;
            fenToSend = canonicalFen;
        }
        lastFen = canonicalFen;

        engineIn.println("position fen " + fenToSend);
        engineIn.println("go movetime " + MOVE_TIME_MS);

        String bestmove = readBestMove();
        if (bestmove == null) {
            return randomFallbackMove(board);
        }
        // e7e8q: la pieza de promoción queda cacheada para requestPromotion
        pendingPromotion = bestmove.length() == 5 ? bestmove.charAt(4) : '\0';
        return new Move(
            squareToPosition(bestmove.substring(0, 2)),
            squareToPosition(bestmove.substring(2, 4))
        );
    }

    @Override
    public PromotionId requestPromotion(BoardView board) {
        return switch (pendingPromotion) {
            case 'r' -> PromotionId.ROOK;
            case 'b' -> PromotionId.BISHOP;
            case 'n' -> PromotionId.KNIGHT;
            default -> PromotionId.QUEEN;
        };
    }

    private Move randomFallbackMove(BoardView board) {
        pendingPromotion = '\0';
        System.out.println("FAIL-STOCKFISH");
        return fallback.requestMove(board);
    }

    private void startEngine() {
        try {
            Process process = new ProcessBuilder(enginePath)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
            engineIn = new PrintWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8),
                true
            );
            engineOut = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo iniciar el motor Stockfish en " + enginePath, e);
        }
        engineIn.println("uci");
        if (!readUntil("uciok")) {
            throw new IllegalStateException("El motor Stockfish no respondió al handshake uci");
        }
        engineIn.println("isready");
        if (!readUntil("readyok")) {
            throw new IllegalStateException("El motor Stockfish no respondió a isready");
        }
    }

    private boolean readUntil(String expected) {
        try {
            String line;
            while ((line = engineOut.readLine()) != null) {
                if (line.equals(expected)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private String readBestMove() {
        try {
            String line;
            while ((line = engineOut.readLine()) != null) {
                if (line.startsWith("bestmove ")) {
                    String move = line.substring("bestmove ".length()).trim();
                    int space = move.indexOf(' ');
                    if (space >= 0) {
                        move = move.substring(0, space);
                    }
                    return move.equals("(none)") ? null : move;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    // FEN sin historial: sin en passant el motor nunca lo captura, y sus jugadas
    // siguen siendo un subconjunto de las legales de la partida real
    private String toFen(BoardView board, boolean withCastlingRights) {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < BoardView.SIZE; row++) {
            int empty = 0;
            for (int column = 0; column < BoardView.SIZE; column++) {
                PieceView piece = board.pieceAt(row, column);
                if (piece == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    char letter = piece.symbol();
                    fen.append(piece.isWhite() ? letter : Character.toLowerCase(letter));
                }
            }
            if (empty > 0) {
                fen.append(empty);
            }
            if (row < BoardView.SIZE - 1) {
                fen.append('/');
            }
        }
        fen.append(playsWhite ? " w " : " b ");
        fen.append(withCastlingRights ? castlingRights(board) : "-");
        fen.append(" - 0 1");
        return fen.toString();
    }

    // Deduce derechos por las casillas de origen: si el rey o la torre se movieron
    // y volvieron, el enroque se rechaza y el reintento usa el FEN sin derechos
    private String castlingRights(BoardView board) {
        StringBuilder rights = new StringBuilder();
        if (isPiece(board, 7, 4, 'K', true)) {
            if (isPiece(board, 7, 7, 'R', true)) rights.append('K');
            if (isPiece(board, 7, 0, 'R', true)) rights.append('Q');
        }
        if (isPiece(board, 0, 4, 'K', false)) {
            if (isPiece(board, 0, 7, 'R', false)) rights.append('k');
            if (isPiece(board, 0, 0, 'R', false)) rights.append('q');
        }
        return rights.length() == 0 ? "-" : rights.toString();
    }

    private boolean isPiece(BoardView board, int row, int column, char symbol, boolean isWhite) {
        PieceView piece = board.pieceAt(row, column);
        return piece != null && piece.symbol() == symbol && piece.isWhite() == isWhite;
    }

    private Position squareToPosition(String square) {
        int column = square.charAt(0) - 'a';
        int row = '8' - square.charAt(1);
        return new Position(row, column);
    }
}
