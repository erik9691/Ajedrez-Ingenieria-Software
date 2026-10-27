package adapters.driving.menu;

import java.util.Scanner;

//Provisional
//Permite elegir opciones antes de jugar

public class ConsoleMenuAdapter {
    private final Scanner scanner = new Scanner(System.in);

    public GameOption chooseOption() {
        while (true) {
            System.out.println("Elegí el modo de juego:");
            System.out.println("1. Local");
            System.out.println("2. Contra la IA");
            System.out.println("3. Contra Stockfish");
            System.out.print("Opción: ");
            if (!scanner.hasNextLine()) {
                return GameOption.LOCAL;
            }
            switch (scanner.nextLine().trim()) {
                case "1" -> { return GameOption.LOCAL; }
                case "2" -> { return GameOption.VS_AI; }
                case "3" -> { return GameOption.VS_STOCKFISH; }
                default -> System.out.println("Opción inválida. Elegí 1, 2 o 3.");
            }
        }
    }

    // true si el humano juega con blancas; en EOF se asumen blancas.
    public boolean choosePlayerColor() {
        while (true) {
            System.out.print("¿Jugás con blancas? (s/n): ");
            if (!scanner.hasNextLine()) {
                return true;
            }
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("s")) {
                return true;
            }
            if (answer.equals("n")) {
                return false;
            }
            System.out.println("Respuesta inválida. Respondé s o n.");
        }
    }
}
