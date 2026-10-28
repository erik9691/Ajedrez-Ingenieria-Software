package main.core.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.core.flags.MoveVerdict;
import main.core.flags.PlayResult;
import main.core.game.MoveHistory;
import main.core.models.MoveRecord;
import main.core.models.Position;
import main.core.game.pieces.Bishop;
import main.core.game.pieces.King;
import main.core.game.pieces.Pawn;
import main.core.game.pieces.Queen;
import main.core.game.pieces.Rook;

//Se encarga de los chequeos de fin de juego
//Board estaba teniendo muchas responsabilidades
//Board se queda con isChecked() ya que lo usa para revisar movimientos legales

public class GameOverChecker {
    private final Board board;
    private final MoveHistory history;
    private static final int FIFTY_MOVE_LIMIT = 100;
    // Medias jugadas sin captura ni movimiento de peon (regla de 50 movimientos)
    private int movesWithoutProgress;

    public GameOverChecker(Board board, MoveHistory moveHistory){
        this.board = board;
        this.history = moveHistory;
        this.movesWithoutProgress = 0;
    }

    public boolean isCheckMate(boolean isWhite) {
        // Si no esta en jaque, no es jaque mate
        if (!board.isChecked(isWhite)) {
            return false;
        }
        // Si puede mover el rey, no es jaque mate
        if (tryKingMoves(isWhite)) {
            return false;
        }
        // Si puede capturar la pieza que da jaque, no es jaque mate
        if (tryCaptureChecker(isWhite)) {
            return false;
        }
        // Si puede interponerse entre la pieza que da jaque y el rey, no es jaque mate
        if (tryBlockMoves(isWhite)) {
            return false;
        }
        return true;
    }

    // Ahogado: no esta en jaque pero no tiene ninguna jugada legal
    public boolean isStalemate(boolean isWhite) {
        if (board.isChecked(isWhite)) {
            return false;
        }
        return !hasAnyLegalMove(isWhite);
    }

    public boolean isDraw(){
        return passedFiftyMoveLimit() || isInsufficientMaterial() || hadThreeMoveRepetition();
    }

    public void incrementHalfmoveClock(){
        this.movesWithoutProgress += 1;
    }

    public void resetHalfmoveClock(){
        this.movesWithoutProgress = 0;
    }










    private boolean tryKingMoves(boolean isWhite) {
        Piece kingPiece = board.getKing(isWhite);
        if (kingPiece == null)
            return false;

        Position kingPosition = board.getPiecePositions().get(kingPiece);
        // Prueba las 8 casillas adyacentes al rey
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                Position to = new Position(kingPosition.row() + rowOffset, kingPosition.column() + columnOffset);
                // Simula el movimiento para validar si un movimiento lo saca del jaque
                Piece captured = board.getPiece(to);
                if (board.tryMove(kingPosition, to) == PlayResult.LEGAL) {
                    // Revertir la simulacion
                    board.setPiece(to, captured);
                    board.setPiece(kingPosition, kingPiece);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryCaptureChecker(boolean isWhite) {
        Piece kingPiece = board.getKing(isWhite);
        if (kingPiece == null)
            return false;

        Position kingPosition = board.getPiecePositions().get(kingPiece);
        // Busca las piezas enemigas que estan dando jaque
        Position checkerPosition = null;
        int checkerCount = 0;
        for (Map.Entry<Piece, Position> entry : board.getPiecePositions().entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(board, entry.getValue(), kingPosition) == MoveVerdict.VALID) {
                checkerPosition = entry.getValue();
                checkerCount++;
            }
        }
        // Con doble jaque no alcanza con capturar una sola pieza
        if (checkerCount != 1) {
            return false;
        }
        Piece checkerPiece = board.getPiece(checkerPosition);
        // Prueba si alguna pieza propia puede capturar a la que da jaque
        for (Map.Entry<Piece, Position> entry : new ArrayList<>(board.getPiecePositions().entrySet())) {
            Piece piece = entry.getKey();
            Position from = entry.getValue();
            if (piece.getColor() != isWhite || piece instanceof King) {
                continue;
            }
            if (board.tryMove(from, checkerPosition) == PlayResult.LEGAL) {
                // Revertir la simulacion
                board.setPiece(checkerPosition, checkerPiece);
                board.setPiece(from, piece);
                return true;
            }
        }
        return false;
    }

    private boolean tryBlockMoves(boolean isWhite) {
        Piece kingPiece = board.getKing(isWhite);
        if (kingPiece == null)
            return false;

        Position kingPosition = board.getPiecePositions().get(kingPiece);
        // Busca las piezas enemigas que estan dando jaque
        Position checkerPosition = null;
        int checkerCount = 0;
        for (Map.Entry<Piece, Position> entry : board.getPiecePositions().entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(board, entry.getValue(), kingPosition) == MoveVerdict.VALID) {
                checkerPosition = entry.getValue();
                checkerCount++;
            }
        }
        // Solo se puede bloquear un unico jaque de una pieza deslizante
        if (checkerCount != 1) {
            return false;
        }
        Piece checkerPiece = board.getPiece(checkerPosition);
        if (!(checkerPiece instanceof Rook || checkerPiece instanceof Bishop || checkerPiece instanceof Queen)) {
            return false;
        }
        // Calcula las casillas entre la checker y el rey
        int rowDirection = Integer.compare(kingPosition.row(), checkerPosition.row());
        int columnDirection = Integer.compare(kingPosition.column(), checkerPosition.column());
        int row = checkerPosition.row() + rowDirection;
        int column = checkerPosition.column() + columnDirection;
        while (row != kingPosition.row() || column != kingPosition.column()) {
            Position between = new Position(row, column);
            // Prueba si alguna pieza propia puede interponerse
            for (Map.Entry<Piece, Position> entry : new ArrayList<>(board.getPiecePositions().entrySet())) {
                Piece piece = entry.getKey();
                Position from = entry.getValue();
                if (piece.getColor() != isWhite || piece instanceof King) {
                    continue;
                }
                if (board.tryMove(from, between) == PlayResult.LEGAL) {
                    // Revertir la simulacion
                    board.setPiece(between, null);
                    board.setPiece(from, piece);
                    return true;
                }
            }
            row += rowDirection;
            column += columnDirection;
        }
        return false;
    }



    // Material insuficiente: con lo que queda nadie puede dar jaque mate
    public boolean isInsufficientMaterial() {
        // Guarda los alfiles y caballos que quedan (los reyes no cuentan)
        ArrayList<Piece> minorPieces = new ArrayList<>();
        for (Piece piece : board.getPiecePositions().keySet()) {
            if (piece instanceof King) {
                continue;
            }
            // Con un peon, torre o dama todavia se puede dar mate
            if (piece instanceof Pawn || piece instanceof Rook || piece instanceof Queen) {
                return false;
            }
            minorPieces.add(piece);
        }
        // Rey contra rey, o rey y un alfil/caballo contra rey
        if (minorPieces.size() <= 1) {
            return true;
        }
        // Rey y alfil contra rey y alfil: empate si los dos alfiles van por casillas del mismo color
        if (minorPieces.size() == 2) {
            Piece first = minorPieces.get(0);
            Piece second = minorPieces.get(1);
            return first instanceof Bishop && second instanceof Bishop
                    && first.getColor() != second.getColor()
                    && isLightSquare(board.getPiecePositions().get(first)) == isLightSquare(board.getPiecePositions().get(second));
        }
        return false;
    }

    private boolean isLightSquare(Position position) {
        return (position.row() + position.column()) % 2 == 0;
    }

    // Prueba todas las jugadas de las piezas propias hasta encontrar una que no deje al rey en jaque
    private boolean hasAnyLegalMove(boolean isWhite) {
        // Se copia la lista porque la simulacion modifica piecePositions
        for (Map.Entry<Piece, Position> entry : new ArrayList<>(board.getPiecePositions().entrySet())) {
            Piece piece = entry.getKey();
            Position from = entry.getValue();
            if (piece.getColor() != isWhite) {
                continue;
            }
            for (int row = 0; row < 8; row++) {
                for (int column = 0; column < 8; column++) {
                    Position to = new Position(row, column);
                    Piece target = board.getPiece(to);
                    // No puede quedarse en el lugar ni comer una pieza propia
                    if (to.equals(from) || (target != null && target.getColor() == isWhite)) {
                        continue;
                    }
                    MoveVerdict verdict = piece.canMove(board, from, to);
                    // Si el enroque es posible, el rey tambien puede moverse una casilla
                    if (verdict == MoveVerdict.INVALID || verdict == MoveVerdict.CASTLE) {
                        continue;
                    }
                    if (leavesKingSafe(from, to, verdict)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Simula la jugada sin usar tryMove (no marca al rey ni a la torre como movidos)
    private boolean leavesKingSafe(Position from, Position to, MoveVerdict verdict) {
        Piece piece = board.getPiece(from);
        // En el al paso el peon capturado no esta en la casilla destino
        Position capturedPosition = verdict == MoveVerdict.EN_PASSANT ? new Position(from.row(), to.column()) : to;
        Piece captured = board.getPiece(capturedPosition);

        board.setPiece(from, null);
        board.setPiece(capturedPosition, null);
        board.setPiece(to, piece);
        boolean kingSafe = !board.isChecked(piece.getColor());

        // Revertir la simulacion
        board.setPiece(to, null);
        board.setPiece(capturedPosition, captured);
        board.setPiece(from, piece);
        return kingSafe;
    }

    private boolean passedFiftyMoveLimit(){
        return (movesWithoutProgress >= FIFTY_MOVE_LIMIT);
    }


    // Repeticion triple: la misma posicion (mismas piezas y mismo turno) tres veces.
    // Sin reconstruir tableros: dentro de la ventana sin capturas ni peones, la
    // posicion de k medias jugadas atras coincide con la actual si cada pieza que
    // se movio en esas k jugadas volvio a la casilla donde estaba.
    private boolean hadThreeMoveRepetition() {
        List<MoveRecord> recentMoves = history.getNMoves(movesWithoutProgress);
        // Ir y volver con una pieza de cada bando toma 4 medias jugadas;
        // para llegar a tres repeticiones se necesitan al menos 8.
        if (recentMoves.size() < 8) {
            return false;
        }

        int repetitions = 1; // la posicion actual
        // Solo las k pares conservan el turno actual
        for (int k = 2; k <= recentMoves.size(); k += 2) {
            int from = recentMoves.size() - k;
            // El enroque mueve tambien la torre (no registrada) y es irreversible:
            // ninguna posicion anterior a el puede repetirse
            if (containsCastle(recentMoves, from)) {
                break;
            }
            if (returnsToOrigin(recentMoves, from)) {
                repetitions++;
                if (repetitions >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    // true si todas las piezas movidas en [from, fin) terminaron donde empezaron
    private boolean returnsToOrigin(List<MoveRecord> moves, int from) {
        Map<Piece, Position> firstStart = new HashMap<>();
        Map<Piece, Position> lastEnd = new HashMap<>();
        for (int i = from; i < moves.size(); i++) {
            MoveRecord record = moves.get(i);
            firstStart.putIfAbsent(record.movedPiece(), record.move().start());
            lastEnd.put(record.movedPiece(), record.move().end());
        }
        for (Piece piece : lastEnd.keySet()) {
            if (!lastEnd.get(piece).equals(firstStart.get(piece))) {
                return false;
            }
        }
        return true;
    }

    private boolean containsCastle(List<MoveRecord> moves, int from) {
        for (int i = from; i < moves.size(); i++) {
            MoveRecord record = moves.get(i);
            if (record.movedPiece() instanceof King
                    && Math.abs(record.move().end().column() - record.move().start().column()) == 2) {
                return true;
            }
        }
        return false;
    }




}
