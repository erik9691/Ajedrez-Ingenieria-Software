package ports.dto;

// Representación de solo lectura de una pieza, pensada para mostrarla en pantalla.
// No expone la lógica del dominio: solo el tipo (letra) y el color.
public record PieceView(char symbol, boolean isWhite) {
}
