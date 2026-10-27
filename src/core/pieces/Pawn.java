package core.pieces;

import core.boards.Board;
import core.models.Piece;
import core.models.Position;
import core.models.MoveVerdict;


public class Pawn extends Piece {

    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public MoveVerdict canMove(Board board, Position start, Position end) {

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
            if (board.getPiece(end) != null) {
                return MoveVerdict.INVALID;
            }
        }

        // Si esta en la posicion inicial, se permite el movimiento de 2 hacia delante
        else if (columnDifference == 0 && start.row() == (isWhite ? 6 : 1) && rowDifference == 2 * direction) {
            Position middle = new Position(start.row() + direction, start.column());
            // Valida que no haya pieza en ninguna de las 2 posiciones por el que se quiere pasar
            if (board.getPiece(middle) != null || board.getPiece(end) != null) {
                return MoveVerdict.INVALID;
            }
        }
        // Para comer en diagonal
        else if (columnDifference == 1 && rowDifference == direction) {
            Piece target = board.getPiece(end);

            // Verifica si es un movimiento de En Passant o solo es una pieza nula
            if(target == null) {
                Position adjacentPawnPosition = new Position(start.row(), end.column());
                Piece adjacentPawn = board.getPiece(adjacentPawnPosition);
                if (adjacentPawn instanceof Pawn && adjacentPawn.getColor() != isWhite) {
                    
                    // Verifica si el peon adyacente se movio 2 casillas en el ultimo turno
                    if (board.wasTwoTilePawnMoved((Pawn) adjacentPawn)) {
                        return MoveVerdict.EN_PASSANT;
                    }
                }
                return MoveVerdict.INVALID;
            }

            // Si la pieza a no es de otro color
            if (target.getColor() == isWhite) {
                return MoveVerdict.INVALID;
            }

        }

        // Peon que llega a la ultima fila: promocion
        if (end.row() == (isWhite ? 0 : 7)) {
            return MoveVerdict.PROMOTION;
        }

        return MoveVerdict.VALID;
    }

    @Override
    public char getSymbol() {
        return 'P';
    }
}
