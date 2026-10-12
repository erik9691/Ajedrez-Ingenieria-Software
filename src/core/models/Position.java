package core.models;

public record Position(int row, int column) {

    public static Position fromNotation(String notation) {
        if (notation == null || !notation.matches("[a-h][1-8]")) {
            throw new IllegalArgumentException(
                "Posición inválida: " + notation
            );
        }

        int column = notation.charAt(0) - 'a';
        int row = notation.charAt(1) - '1';

        return new Position(row, column);
    }
}