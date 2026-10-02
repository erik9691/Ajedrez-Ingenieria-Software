package core.boards;

import core.models.Position;
import core.pieces.Piece;

public class Board {
    private Piece[][] squares;

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
        setPiece(end, startPiece);
        return true;
    }

    private boolean isValidPosition(Position position) {
        return position.row() >= 0 && position.row() < 8 && position.column() >= 0 && position.column() < 8;
    }

    private boolean isChecked(boolean isWhite) {
        // logica de validar si esta en jaque
    }

    public Piece getPiece(Position position) {
        // Validar si las posicion elegida esta adentro del tablero
        if (!isValidPosition(position)) {
            return null;
        }        
        return squares[position.row()][position.column()];
    }

    public void setPiece(Position position, Piece piece) {
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
