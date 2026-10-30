package main.core.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.core.models.MoveRecord;
import main.core.models.Position;
import main.core.flags.MoveVerdict;
import main.core.flags.PlayResult;
import main.core.game.MoveHistory;
import main.ports.dto.BoardView;
import main.ports.dto.PieceView;

import main.core.game.pieces.King;
import main.core.game.pieces.Rook;
import main.core.game.pieces.Pawn;


public class Board {
    protected Piece[][] squares;
    private final Map<Piece, Position> piecePositions;
    private final MoveHistory history;



    public Board(MoveHistory moveHistory) {
        this.squares = new Piece[8][8];
        this.piecePositions = new HashMap<>();
        this.history = moveHistory;
    }

    public Piece getPiece(Position position) {
        // Validar si las posicion elegida esta adentro del tablero
        if (!isValidPosition(position)) {
            return null;
        }        
         //retorna la pieza que esta en esa coordenada (fila, columna de Position)
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

    // Vista de solo lectura del tablero para los adaptadores de salida.
    public BoardView toView() {
        PieceView[][] cells = new PieceView[BoardView.SIZE][BoardView.SIZE];
        for (int row = 0; row < BoardView.SIZE; row++) {
            for (int column = 0; column < BoardView.SIZE; column++) {
                Piece piece = squares[row][column];
                if (piece != null) {
                    cells[row][column] = new PieceView(piece.getSymbol(), piece.getColor());
                }
            }
        }
        return new BoardView(cells);
    }

    public PlayResult tryMove(Position start, Position end) {
        // Evita salirse del tablero (ej. el rey en un borde probando casillas adyacentes)
        if (!isValidPosition(start) || !isValidPosition(end)) {
            return PlayResult.OUT_OF_BOUNDS;
        }
        Piece startPiece = getPiece(start);
        Piece endPiece = getPiece(end);
        // Validar que se haya elegido una pieza para mover
        if (startPiece == null) {
            return PlayResult.NO_PIECE;
        }
        // Validar que la pieza a comer es de otro color
        if (endPiece != null && endPiece.getColor() == startPiece.getColor()) {
            return PlayResult.OWN_PIECE_CAPTURE;
        }
        // Validacion especifica de cada pieza
        MoveVerdict verdict = startPiece.canMove(this, start, end);
        if (verdict == MoveVerdict.INVALID) {
            return PlayResult.ILLEGAL;
        }
        // Movimiento valido/de promocion
        if(verdict == MoveVerdict.VALID || verdict == MoveVerdict.PROMOTION) {
            // Mueve la pieza
            setPiece(start, null);
            setPiece(end, startPiece);
            // Si al final del movimiento está en jaque no se permite el movimiento
            if (isChecked(startPiece.getColor())){
                setPiece(end, endPiece);
                setPiece(start, startPiece);
                return PlayResult.SELF_MATE;
            }
            // Marca el rey/la torre como movida para validar el enroque
            if (startPiece instanceof King k) k.markAsMoved();
            else if (startPiece instanceof Rook r) r.markAsMoved();
            // El return cambia si el movimiento dejo la pieza en promocion
            return verdict == MoveVerdict.PROMOTION ? PlayResult.PROMOTION_PENDING : PlayResult.LEGAL;
        }
        // Enroque
        if(verdict == MoveVerdict.CASTLE) {
            return doCastle(start, end);
        }
        // Al paso
        if(verdict == MoveVerdict.EN_PASSANT) {
            return doEnPassant(start, end);
        }
        

        return PlayResult.ILLEGAL; 
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


    public boolean isChecked(boolean isWhite) {
        Position kingPosition = findPiecePosition(King.class, isWhite);
        if (kingPosition == null) {
            return false;
        }
        
        // Si alguna pieza enemiga puede moverse hasta el rey, es jaque
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() != isWhite && piece.canMove(this, entry.getValue(), kingPosition) == MoveVerdict.VALID) {
                return true;
            }
        }
        return false;
    }

    //Revisa que el ultimo movimiento fue el peon en cuestion avanzando 2 casillas.
    //Necesario para EnPassant.
    public boolean wasTwoTilePawnMoved(Pawn pawn) {
        List<MoveRecord> lastMoves = history.getNMoves(1);
        if (lastMoves.isEmpty()) {
            return false;
        }
        MoveRecord last = lastMoves.get(0);
        return last.movedPiece() == pawn
                && Math.abs(last.move().end().row() - last.move().start().row()) == 2;
    }

    // Obtiene una pieza segun el tipo y color
    public Position findPiecePosition(Class<? extends Piece> type, boolean isWhite) {
        for (Map.Entry<Piece, Position> entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            if (piece.getColor() == isWhite && type.isInstance(piece)) {
                return entry.getValue();
            }
        }
        return null;
    }



    public Piece[][] getSquares(){
        return squares;
    }

    public Map<Piece, Position> getPiecePositions(){
        return piecePositions;
    }

    private boolean isValidPosition(Position position) {
        return position.row() >= 0 && position.row() < 8 && position.column() >= 0 && position.column() < 8;
    }




    private PlayResult doCastle(Position start, Position end) {
        Piece king = getPiece(start);
        int row = start.row();

        // Si esta en jaque no puede hacer enroque
        boolean color = king.getColor();
        if (isChecked(color)) return PlayResult.ILLEGAL;
        
        // Si la casilla intermedia esta siendo atacada no puede hacer enroque
        Position passing = new Position(row, (start.column() + end.column()) / 2);
        setPiece(start, null);
        setPiece(passing, king);
        boolean passesThroughCheck = isChecked(color);
        setPiece(passing, null);
        setPiece(start, king);
        if (passesThroughCheck) return PlayResult.ILLEGAL;

        // El enroque es corto si el rey se mueve hacia la derecha
        boolean shortCastle = end.column() > start.column();
        Position rookFrom = new Position(row, shortCastle ? 7 : 0);

        // La torre termina del otro lado del rey
        Position rookTo = new Position(row, shortCastle ? end.column() - 1 : end.column() + 1);
        Piece rook = getPiece(rookFrom);

        // Hace el movimiento
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
            return PlayResult.SELF_MATE;
        }

        return PlayResult.LEGAL;
    }


    private PlayResult doEnPassant(Position start, Position end) {
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
            return PlayResult.SELF_MATE;
        }

        return PlayResult.LEGAL;
    }




}
