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
            System.out.println("3. Crear partida en red");
            System.out.println("4. Unirse a una partida en red");
            System.out.print("Opción: ");
            if (!scanner.hasNextLine()) {
                return GameOption.LOCAL;
            }
            switch (scanner.nextLine().trim()) {
                case "1" -> { return GameOption.LOCAL; }
                case "2" -> { return GameOption.VS_AI; }
                case "3" -> { return GameOption.HOST; }
                case "4" -> { return GameOption.JOIN; }
                default -> System.out.println("Opción inválida. Elegí 1, 2, 3 o 4.");
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

    public String requestIp() {
        while (true) {
            System.out.print("IP del host: ");
            if (!scanner.hasNextLine()) {
                return "localhost";
            }
            String ip = scanner.nextLine().trim();
            if (!ip.isEmpty()) {
                return ip;
            }
            System.out.println("IP inválida.");
        }
    }

    public String requestName() {
        System.out.print("Tu nombre: ");
        if (!scanner.hasNextLine()) {
            return "Jugador";
        }
        String name = scanner.nextLine().trim();
        return name.isEmpty() ? "Jugador" : name;
    }

    // 0 = ingresar IP manual; 1..hostCount = partida de la lista
    public int chooseHost(int hostCount) {
        while (true) {
            System.out.print("Elegí una partida (0 = ingresar IP manual): ");
            if (!scanner.hasNextLine()) {
                return 0;
            }
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 0 && choice <= hostCount) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // se pide de nuevo
            }
            System.out.println("Opción inválida.");
        }
    }
}
