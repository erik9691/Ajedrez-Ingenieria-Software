package core.boards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import core.models.Position;
import core.pieces.Bishop;
import core.pieces.King;
import core.pieces.Piece;
import core.pieces.Queen;
import core.pieces.Rook;

public class Board {
    private Piece[][] squares;
    private final Map<Piece, Position> piecePositions = new HashMap<>();

    public Board() {
        this.squares = new Piece[8][8];
    }

    public boolean tryMove(Position start, Position end) {

        Piece startPiece = getPiece(start);
        Piece endPiece = getPiece(end);
        // Validar que la pieza a comer es de otro color
        if (endPiece != null && endPiece.getColor() == startPiece.getColor()) {
            return false;
        }
        // Validacion especifica de cada pieza
        if (!startPiece.canMove(this, start, end)) {
            return false;
        }
        setPiece(start, null);
        setPiece(end, startPiece);

        if (isChecked(startPiece.getColor())){
            setPiece(end, endPiece);
            setPiece(start, startPiece);
            return false;
        }

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

    private boolean isChecked(boolean isWhite) {
        Piece kingPiece = findPiece(King.class, isWhite);
        if (kingPiece == null) {
            return false;
        }
        Position kingPosition = piecePositions.get(kingPiece);
        // Si alguna pieza enemiga puede moverse hasta el rey, es jaque
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition)) {
                return true;
            }
        }
        return false;
    }

    public boolean tryKingMoves(boolean isWhite) {
        Piece kingPiece = findPiece(King.class, isWhite);
        if (kingPiece == null) {
            return false;
        }
        Position kingPosition = piecePositions.get(kingPiece);
        // Prueba las 8 casillas adyacentes al rey
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                if (rowOffset == 0 && columnOffset == 0) {
                    continue;
                }
                Position to = new Position(
                        kingPosition.row() + rowOffset,
                        kingPosition.column() + columnOffset);
                if (!isValidPosition(to)) {
                    continue;
                }
                Piece captured = getPiece(to);
                if (tryMove(kingPosition, to)) {
                    // Revertir la simulacion, primero el destino y despues el origen
                    setPiece(to, captured);
                    setPiece(kingPosition, kingPiece);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryCaptureChecker(boolean isWhite) {
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
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition)) {
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
            if (tryMove(from, checkerPosition)) {
                // Revertir la simulacion, primero el destino y despues el origen
                setPiece(checkerPosition, checkerPiece);
                setPiece(from, piece);
                return true;
            }
        }
        return false;
    }

    public boolean tryBlockMoves(boolean isWhite) {
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
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition)) {
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
                if (tryMove(from, between)) {
                    // Revertir la simulacion, primero el destino y despues el origen
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

    public Piece getPiece(Position position) {
        // Validar si las posicion elegida esta adentro del tablero
        if (!isValidPosition(position)) {
            return null;
        }        
        return squares[position.row()][position.column()];
    }

    public void setPiece(Position position, Piece piece) {
        Piece previous = squares[position.row()][position.column()];
        // Si se reemplaza o se saca una pieza, se la quita del diccionario
        if (previous != null) {
            piecePositions.remove(previous);
        }
        if (piece != null) {
            piecePositions.put(piece, position);
        }
        squares[position.row()][position.column()] = piece;
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
}
