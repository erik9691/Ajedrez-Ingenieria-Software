package core.boards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import core.models.Piece;
import core.models.Position;
import core.models.MoveVerdict;
import core.models.MoveResult;

import core.pieces.Bishop;
import core.pieces.King;
import core.pieces.Queen;
import core.pieces.Rook;
import core.pieces.Pawn;


public class Board {
    protected Piece[][] squares;
    private final Map<Piece, Position> piecePositions = new HashMap<>();

    public Board() {
        this.squares = new Piece[8][8];
    }

    public Piece getPiece(Position position) {
        // Validar si las posicion elegida esta adentro del tablero
        if (!isValidPosition(position)) {
            return null;
        }        
        return squares[position.row()][position.column()];
    }

    public void setPiece(Position position, Piece piece) {
        Piece previousPiece = squares[position.row()][position.column()];
        // Si se reemplaza o se saca una pieza, se la quita del diccionario
        if (previousPiece != null) {
            piecePositions.remove(previousPiece);
        }
        if (piece != null) {
            piecePositions.put(piece, position);
        }
        squares[position.row()][position.column()] = piece;
    }

    public MoveResult tryMove(Position start, Position end) {
        // Evita salirse del tablero (ej. el rey en un borde probando casillas adyacentes)
        if (!isValidPosition(start) || !isValidPosition(end)) {
            return MoveResult.OUT_OF_BOUNDS;
        }
        Piece startPiece = getPiece(start);
        Piece endPiece = getPiece(end);
        // Validar que se haya elegido una pieza para mover
        if (startPiece == null) {
            return MoveResult.NO_PIECE;
        }
        // Validar que la pieza a comer es de otro color
        if (endPiece != null && endPiece.getColor() == startPiece.getColor()) {
            return MoveResult.OWN_PIECE_CAPTURE;
        }
        // Validacion especifica de cada pieza
        MoveVerdict verdict = startPiece.canMove(this, start, end);
        if (verdict == MoveVerdict.INVALID) {
            return MoveResult.ILLEGAL;
        }

        if(verdict == MoveVerdict.VALID || verdict == MoveVerdict.PROMOTION) {
            // Mueve la pieza
            setPiece(start, null);
            setPiece(end, startPiece);
            // Si al final del movimiento está en jaque no se permite el movimiento
            if (isChecked(startPiece.getColor())){
                setPiece(end, endPiece);
                setPiece(start, startPiece);
                return MoveResult.SELF_MATE;
            }
            // El return cambia si el movimiento dejo la pieza en promocion
            return verdict == MoveVerdict.PROMOTION ? MoveResult.PROMOTION_PENDING : MoveResult.LEGAL;
        }
        // Enroque
        if(verdict == MoveVerdict.CASTLE) {
            return doCastle(start, end);
        }
        // Al paso
        if(verdict == MoveVerdict.EN_PASSANT) {
            return doEnPassant(start, end);
        }
        

        return MoveResult.ILLEGAL; 
    }

    public boolean isPathClear(Position start, Position end) {
        int rowDirection = Integer.compare(end.row(), start.row());
        int columnDirection = Integer.compare(end.column(), start.column());

        int row = start.row() + rowDirection;
        int column = start.column() + columnDirection;

        // Pasa por cada cuadrado que hay en el camino y valida que no haya pieza
        while (row != end.row() || column != end.column()) {

            if (squares[row][column] != null) {
                return false;
            }

            row += rowDirection;
            column += columnDirection;
        }

        return true;
    }

    //Lógica provisoria
    public boolean isCheckmated(boolean isWhite) {
        // Si no esta en jaque, no es jaque mate
        if (!isChecked(isWhite)) {
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

    //Revisa que el anterior movimiento fue el peón en cuestión siendo movido 2 casillas. Necesario para EnPassant
    //Necesita historial de juego, placeholder por ahora.
    public boolean wasTwoTilePawnMoved(Pawn pawn) {
        return true;
    }

    private boolean isValidPosition(Position position) {
        return position.row() >= 0 && position.row() < 8 && position.column() >= 0 && position.column() < 8;
    }

    private Piece findPiece(Class<? extends Piece> type, boolean isWhite) {
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() == isWhite && type.isInstance(piece)) {
                return piece;
            }
        }
        return null;
    }

    public boolean isChecked(boolean isWhite) {
        Piece kingPiece = findPiece(King.class, isWhite);
        if (kingPiece == null) {
            return false;
        }
        Position kingPosition = piecePositions.get(kingPiece);
        // Si alguna pieza enemiga puede moverse hasta el rey, es jaque
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition) == MoveVerdict.VALID) {
                return true;
            }
        }
        return false;
    }


    private MoveResult doCastle(Position start, Position end) {
        Piece king = getPiece(start);
        int row = start.row();
        // El enroque es corto si el rey se mueve hacia la derecha
        boolean shortCastle = end.column() > start.column();
        Position rookFrom = new Position(row, shortCastle ? 7 : 0);
        // La torre termina del otro lado del rey
        Position rookTo = new Position(row, shortCastle ? end.column() - 1 : end.column() + 1);
        Piece rook = getPiece(rookFrom);

        setPiece(start, null);
        setPiece(end, king);
        setPiece(rookFrom, null);
        setPiece(rookTo, rook);

        // Si al final del movimiento está en jaque se revierte
        if (isChecked(king.getColor())) {
            setPiece(rookTo, null);
            setPiece(rookFrom, rook);
            setPiece(end, null);
            setPiece(start, king);
            return MoveResult.SELF_MATE;
        }

        return MoveResult.LEGAL;
    }


    private MoveResult doEnPassant(Position start, Position end) {
        Piece pawn = getPiece(start);
        // El peón capturado está al lado del destino, en la fila de origen
        Position capturedPosition = new Position(start.row(), end.column());
        Piece capturedPawn = getPiece(capturedPosition);

        setPiece(start, null);
        setPiece(capturedPosition, null);
        setPiece(end, pawn);

        // Si al final del movimiento está en jaque se revierte
        if (isChecked(pawn.getColor())) {
            setPiece(end, null);
            setPiece(capturedPosition, capturedPawn);
            setPiece(start, pawn);
            return MoveResult.SELF_MATE;
        }

        return MoveResult.LEGAL;
    }



    private boolean tryKingMoves(boolean isWhite) {
        // Busca al rey y obtiene su posicion
        Piece kingPiece = findPiece(King.class, isWhite);
        if (kingPiece == null) {
            return false;
        }
        Position kingPosition = piecePositions.get(kingPiece);
        // Prueba las 8 casillas adyacentes al rey
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                Position to = new Position(kingPosition.row() + rowOffset, kingPosition.column() + columnOffset);
                // Simula el movimiento para validar si un movimiento lo saca del jaque
                Piece captured = getPiece(to);
                if (tryMove(kingPosition, to) == MoveResult.LEGAL) {
                    // Revertir la simulacion
                    setPiece(to, captured);
                    setPiece(kingPosition, kingPiece);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryCaptureChecker(boolean isWhite) {
        Piece kingPiece = findPiece(King.class, isWhite);
        if (kingPiece == null) {
            return false;
        }
        Position kingPosition = piecePositions.get(kingPiece);
        // Busca las piezas enemigas que estan dando jaque
        Position checkerPosition = null;
        int checkerCount = 0;
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition) == MoveVerdict.VALID) {
                checkerPosition = entry.getValue();
                checkerCount++;
            }
        }
        // Con doble jaque no alcanza con capturar una sola pieza
        if (checkerCount != 1) {
            return false;
        }
        Piece checkerPiece = getPiece(checkerPosition);
        // Prueba si alguna pieza propia puede capturar a la que da jaque
        for (Map.Entry<Piece, Position> entry : new ArrayList<>(piecePositions.entrySet())) {
            Piece piece = entry.getKey();
            Position from = entry.getValue();
            if (piece.getColor() != isWhite || piece instanceof King) {
                continue;
            }
            if (tryMove(from, checkerPosition) == MoveResult.LEGAL) {
                // Revertir la simulacion
                setPiece(checkerPosition, checkerPiece);
                setPiece(from, piece);
                return true;
            }
        }
        return false;
    }

    private boolean tryBlockMoves(boolean isWhite) {
        Piece kingPiece = findPiece(King.class, isWhite);
        if (kingPiece == null) {
            return false;
        }
        Position kingPosition = piecePositions.get(kingPiece);
        // Busca las piezas enemigas que estan dando jaque
        Position checkerPosition = null;
        int checkerCount = 0;
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition) == MoveVerdict.VALID) {
                checkerPosition = entry.getValue();
                checkerCount++;
            }
        }
        // Solo se puede bloquear un unico jaque de una pieza deslizante
        if (checkerCount != 1) {
            return false;
        }
        Piece checkerPiece = getPiece(checkerPosition);
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
            for (Map.Entry<Piece, Position> entry : new ArrayList<>(piecePositions.entrySet())) {
                Piece piece = entry.getKey();
                Position from = entry.getValue();
                if (piece.getColor() != isWhite || piece instanceof King) {
                    continue;
                }
                if (tryMove(from, between) == MoveResult.LEGAL) {
                    // Revertir la simulacion
                    setPiece(between, null);
                    setPiece(from, piece);
                    return true;
                }
            }
            row += rowDirection;
            column += columnDirection;
        }
        return false;
    }

}
