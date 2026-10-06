package core.pieces;

import core.api.Piece;
import core.api.Board;
import core.models.Position;

public class Pawn extends Piece {

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public boolean canMove(Board board, Position start, Position end) {

        int direction;
        if (isWhite) {
            direction = -1;
        } else {
            direction = 1;
        }
        // rowDifference no se vuelve absoluto para saber la direccion de movimiento
        // Esto asume que los negros vienen antes en la matriz
        int rowDifference = end.row() - start.row();
        int columnDifference = Math.abs(end.column() - start.column());

        // Movimiento basico hacia adelante
        if (columnDifference == 0 && rowDifference == direction) {
            return board.getPiece(end) == null;
        }

        // Constantes para saber si esta en la posicion inicial
        int startingRow = isWhite ? 6 : 1;
        if (isWhite) {
            startingRow = 6;
        } else {
            startingRow = 1;
        }
        // Si esta en la posicion inicial, se permite el movimiento de 2 hacia delante
        if (columnDifference == 0 && start.row() == startingRow && rowDifference == 2 * direction) {

            Position middle = new Position(start.row() + direction, start.column());
            // Valida que no haya pieza en ninguna de las 2 posiciones por el que se quiere pasar
            return board.getPiece(middle) == null && board.getPiece(end) == null;
        }

        // Para comer en diagonal
        if (columnDifference == 1 && rowDifference == direction) {
            Piece target = board.getPiece(end);
            // Si la pieza a comer existe y es de otro color devuelve true
            return target != null && target.getColor() != isWhite;
        }

        return false;
    }
}