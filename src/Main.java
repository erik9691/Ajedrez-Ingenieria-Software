import java.io.IOException;
import java.util.List;

import adapters.driven.display.ConsoleDisplayAdapter;
import adapters.driven.input.ConsoleInputAdapter;
import adapters.driven.input.ForwardingInputAdapter;
import adapters.driven.input.RandomMovesAI;
import adapters.driven.input.RemoteInputAdapter;
import adapters.driven.networking.LanConnection;
import adapters.driven.networking.LanDiscovery;
import adapters.driving.menu.ConsoleMenuAdapter;
import adapters.driving.menu.GameOption;
import core.ChessService;
import core.boards.Board;
import core.managers.GameManager;
import ports.in.ChessMatchPort;
import ports.out.DisplayPort;
import ports.out.PlayerInputPort;

// Composition root: el único lugar que conoce todas las piezas y las conecta.
public class Main {
    public static void main(String[] args) {
        ConsoleMenuAdapter menu = new ConsoleMenuAdapter();
        GameOption option = menu.chooseOption();

        DisplayPort display = new ConsoleDisplayAdapter();
        GameManager gameManager = new GameManager(new Board());

        ChessMatchPort game;
        if (option == GameOption.LOCAL) {
            // Misma consola para ambos: una sola instancia evita dos Scanners compitiendo por System.in
            PlayerInputPort console = new ConsoleInputAdapter();
            game = new ChessService(gameManager, display, console, console);
        } else if (option == GameOption.VS_AI) {
            boolean playerIsWhite = menu.choosePlayerColor();
            PlayerInputPort human = new ConsoleInputAdapter();
            PlayerInputPort ai = new RandomMovesAI();
            game = new ChessService(gameManager, display,
                    playerIsWhite ? human : ai,
                    playerIsWhite ? ai : human);
        } else {
            try {
                game = startNetworkGame(menu, gameManager, display, option);
            } catch (IOException e) {
                System.out.println("Error de conexión: " + e.getMessage());
                return;
            }
        }

        game.runGame();
    }

    private static ChessMatchPort startNetworkGame(ConsoleMenuAdapter menu, GameManager gameManager,
            DisplayPort display, GameOption option) throws IOException {
        LanConnection connection;
        boolean localIsWhite;
        if (option == GameOption.HOST) {
            localIsWhite = menu.choosePlayerColor();
            LanDiscovery.responder(menu.requestName()).start();
            System.out.println("Esperando oponente en el puerto " + LanConnection.DEFAULT_PORT + "...");
            connection = LanConnection.host(LanConnection.DEFAULT_PORT, localIsWhite);
        } else {
            connection = connectToHost(menu);
            localIsWhite = connection.isLocalWhite();
        }

        System.out.println("Jugás con las " + (localIsWhite ? "blancas." : "negras."));
        PlayerInputPort local = new ForwardingInputAdapter(new ConsoleInputAdapter(), connection);
        PlayerInputPort remote = new RemoteInputAdapter(connection);
        return new ChessService(gameManager, display,
                localIsWhite ? local : remote,
                localIsWhite ? remote : local);
    }

    private static LanConnection connectToHost(ConsoleMenuAdapter menu) throws IOException {
        List<LanDiscovery.Host> hosts = LanDiscovery.scan(3000);
        if (hosts.isEmpty()) {
            System.out.println("No se encontraron partidas en la red.");
        } else {
            System.out.println("Partidas encontradas:");
            for (int i = 0; i < hosts.size(); i++) {
                System.out.println((i + 1) + ". " + hosts.get(i).name() + " (" + hosts.get(i).ip() + ")");
            }
            int choice = menu.chooseHost(hosts.size());
            if (choice > 0) {
                return LanConnection.join(hosts.get(choice - 1).ip(), LanConnection.DEFAULT_PORT);
            }
        }
        return LanConnection.join(menu.requestIp(), LanConnection.DEFAULT_PORT);
    }
}
