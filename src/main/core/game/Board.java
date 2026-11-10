package main.core.game;

import main.core.flags.*;
import main.core.game.pieces.*;
import main.core.models.*;
import main.ports.dto.*;

import java.util.*;

public class Board {
    public static final int SIZE = 8;
    private final Piece[][] squares = new Piece[SIZE][SIZE];
    private final Map<Piece, Position> piecePositions = new HashMap<>();
    private final MoveHistory history;

    public Board(MoveHistory history) {
        this.history = Objects.requireNonNull(history);
    }

    public static boolean isValidPosition(Position p) {
        return p != null && p.row() >= 0 && p.row() < SIZE && p.column() >= 0 && p.column() < SIZE;
    }

    public Piece getPiece(Position p) {
        return isValidPosition(p) ? squares[p.row()][p.column()] : null;
    }

    public void setPiece(Position p, Piece piece) {
        if (!isValidPosition(p)) throw new IllegalArgumentException("Posicion fuera del tablero");
        Piece previous = getPiece(p);
        if (previous != null) piecePositions.remove(previous);
        if (piece != null) {
            Position old = piecePositions.put(piece, p);
            if (old != null && !old.equals(p)) squares[old.row()][old.column()] = null;
        }
        squares[p.row()][p.column()] = piece;
    }

    public void clear() {
        for (Piece[] row : squares) Arrays.fill(row, null);
        piecePositions.clear();
    }

    public Map<Piece, Position> getPiecePositions() {
        return Map.copyOf(piecePositions);
    }

    public BoardView toView() {
        PieceView[][] cells = new PieceView[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++)
            for (int col = 0; col < SIZE; col++) {
                Piece piece = squares[row][col];
                if (piece != null)
                    cells[row][col] = new PieceView(piece.getSymbol(), piece.getColor());
            }
        return new BoardView(cells);
    }

    public PlayResult tryMove(Position start, Position end) {
        return move(start, end, true);
    }

    public boolean isLegalMove(Position start, Position end) {
        return move(start, end, false).isLegal();
    }

    public List<Move> legalMoves(boolean white) {
        List<Move> moves = new ArrayList<>();
        for (var entry : getPiecePositions().entrySet()) {
            if (entry.getKey().getColor() != white) continue;
            for (int row = 0; row < SIZE; row++)
                for (int col = 0; col < SIZE; col++) {
                    Position to = new Position(row, col);
                    if (isLegalMove(entry.getValue(), to))
                        moves.add(new Move(entry.getValue(), to));
                }
        }
        return List.copyOf(moves);
    }

    private PlayResult move(Position start, Position end, boolean commit) {
        if (!isValidPosition(start) || !isValidPosition(end)) return PlayResult.OUT_OF_BOUNDS;
        Piece piece = getPiece(start), target = getPiece(end);
        if (piece == null) return PlayResult.NO_PIECE;
        if (start.equals(end)) return PlayResult.ILLEGAL;
        if (target != null && target.getColor() == piece.getColor())
            return PlayResult.OWN_PIECE_CAPTURE;
        if (target instanceof King) return PlayResult.ILLEGAL;
        MoveVerdict verdict = piece.canMove(this, start, end);
        if (verdict == MoveVerdict.INVALID) return PlayResult.ILLEGAL;
        Position rookFrom = null, rookTo = null;
        if (verdict == MoveVerdict.CASTLE) {
            if (isChecked(piece.getColor())) return PlayResult.ILLEGAL;
            Position passing = new Position(start.row(), (start.column() + end.column()) / 2);
            if (!isLegalMove(start, passing)) return PlayResult.ILLEGAL;
            boolean shortCastle = end.column() > start.column();
            rookFrom = new Position(start.row(), shortCastle ? 7 : 0);
            rookTo = new Position(start.row(), shortCastle ? 5 : 3);
        }
        // Toda consulta restaura las casillas y el indice, incluso ante una excepcion.
        Piece[][] before = new Piece[SIZE][];
        for (int row = 0; row < SIZE; row++) before[row] = squares[row].clone();
        Map<Piece, Position> positionsBefore = new HashMap<>(piecePositions);
        boolean accepted = false;
        try {
            setPiece(start, null);
            if (verdict == MoveVerdict.EN_PASSANT)
                setPiece(new Position(start.row(), end.column()), null);
            setPiece(end, piece);
            Piece rook = null;
            if (rookFrom != null) {
                rook = getPiece(rookFrom);
                setPiece(rookFrom, null);
                setPiece(rookTo, rook);
            }
            if (isChecked(piece.getColor())) return PlayResult.SELF_MATE;
            if (commit) {
                if (piece instanceof King king) king.markAsMoved();
                if (piece instanceof Rook movedRook) movedRook.markAsMoved();
                if (rook instanceof Rook castleRook) castleRook.markAsMoved();
                accepted = true;
            }
            return verdict == MoveVerdict.PROMOTION
                    ? PlayResult.PROMOTION_PENDING
                    : PlayResult.LEGAL;
        } finally {
            if (!accepted) {
                for (int row = 0; row < SIZE; row++)
                    System.arraycopy(before[row], 0, squares[row], 0, SIZE);
                piecePositions.clear();
                piecePositions.putAll(positionsBefore);
            }
        }
    }

    public boolean isPathClear(Position start, Position end) {
        if (!isValidPosition(start) || !isValidPosition(end) || start.equals(end)) return false;
        int dr = end.row() - start.row(), dc = end.column() - start.column();
        if (dr != 0 && dc != 0 && Math.abs(dr) != Math.abs(dc)) return false;
        int sr = Integer.signum(dr), sc = Integer.signum(dc);
        for (int row = start.row() + sr, col = start.column() + sc;
                row != end.row() || col != end.column();
                row += sr, col += sc) if (squares[row][col] != null) return false;
        return true;
    }

    public boolean isChecked(boolean white) {
        Position king = findKingPosition(white);
        return king != null && isSquareAttacked(king, !white);
    }

    public boolean isSquareAttacked(Position to, boolean byWhite) {
        for (var entry : piecePositions.entrySet()) {
            Piece piece = entry.getKey();
            Position from = entry.getValue();
            if (piece.getColor() != byWhite) continue;
            int dr = to.row() - from.row(), dc = Math.abs(to.column() - from.column());
            if (piece instanceof Pawn) {
                if (dr == (byWhite ? -1 : 1) && dc == 1) return true;
            } else if (piece instanceof King) {
                if (Math.abs(dr) <= 1 && dc <= 1 && (dr != 0 || dc != 0)) return true;
            } else if (piece.canMove(this, from, to) == MoveVerdict.VALID) return true;
        }
        return false;
    }

    public Position findKingPosition(boolean white) {
        for (var e : piecePositions.entrySet())
            if (e.getKey() instanceof King && e.getKey().getColor() == white) return e.getValue();
        return null;
    }

    public boolean wasTwoTilePawnMoved(Pawn pawn) {
        List<MoveRecord> last = history.getNMoves(1);
        return !last.isEmpty()
                && last.get(0).movedPiece() == pawn
                && Math.abs(last.get(0).move().start().row() - last.get(0).move().end().row()) == 2;
    }

    public String castlingRights() {
        StringBuilder rights = new StringBuilder();
        for (boolean white : new boolean[] {true, false}) {
            int row = white ? 7 : 0;
            if (getPiece(new Position(row, 4)) instanceof King king
                    && king.getColor() == white
                    && !king.hasMoved()) {
                for (int col : new int[] {7, 0})
                    if (getPiece(new Position(row, col)) instanceof Rook rook
                            && rook.getColor() == white
                            && !rook.hasMoved())
                        rights.append(white ? (col == 7 ? 'K' : 'Q') : (col == 7 ? 'k' : 'q'));
            }
        }
        return rights.isEmpty() ? "-" : rights.toString();
    }

    public Position enPassantTarget() {
        List<MoveRecord> last = history.getNMoves(1);
        if (last.isEmpty()) return null;
        MoveRecord m = last.get(0);
        if (!(m.movedPiece() instanceof Pawn)
                || Math.abs(m.move().start().row() - m.move().end().row()) != 2) return null;
        return new Position(
                (m.move().start().row() + m.move().end().row()) / 2, m.move().end().column());
    }

    public String repetitionKey(boolean white) {
        StringBuilder key = new StringBuilder();
        for (Piece[] row : squares)
            for (Piece piece : row)
                key.append(
                        piece == null
                                ? '.'
                                : piece.getColor()
                                        ? piece.getSymbol()
                                        : Character.toLowerCase(piece.getSymbol()));
        key.append(white ? 'w' : 'b').append(castlingRights());
        Position ep = enPassantTarget();
        if (ep != null) {
            // Solo importa el derecho al paso si existe una captura legal.
            for (var e : getPiecePositions().entrySet())
                if (e.getKey() instanceof Pawn
                        && e.getKey().getColor() == white
                        && e.getValue().column() != ep.column()
                        && isLegalMove(e.getValue(), ep)) {
                    key.append(ep);
                    break;
                }
        }
        return key.toString();
    }
}
