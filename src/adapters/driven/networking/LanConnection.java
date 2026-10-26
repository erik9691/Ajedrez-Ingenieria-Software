package adapters.driven.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import core.flags.PromotionId;
import core.models.Move;
import core.models.Position;

// Conexión TCP entre las dos máquinas: el host comunica el color en el
// handshake y después los mensajes son bytes simples (0=resign, 1=move, 2=promotion).
public class LanConnection implements AutoCloseable {
    public static final int DEFAULT_PORT = 5000;

    private static final byte RESIGN = 0;
    private static final byte MOVE = 1;
    private static final byte PROMOTION = 2;

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private boolean localIsWhite;

    private LanConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    // Espera al oponente y le comunica qué color juega el host
    public static LanConnection host(int port, boolean hostIsWhite) throws IOException {
        ServerSocket server = new ServerSocket(port);
        Socket client = server.accept();
        server.close();
        LanConnection connection = new LanConnection(client);
        connection.out.writeBoolean(hostIsWhite);
        connection.out.flush();
        connection.localIsWhite = hostIsWhite;
        return connection;
    }

    // Se conecta al host y recibe el color asignado en el handshake
    public static LanConnection join(String hostIp, int port) throws IOException {
        LanConnection connection = new LanConnection(new Socket(hostIp, port));
        connection.localIsWhite = !connection.in.readBoolean();
        return connection;
    }

    public boolean isLocalWhite() {
        return localIsWhite;
    }

    public void sendMove(Move move) throws IOException {
        out.writeByte(MOVE);
        out.writeByte(move.start().row());
        out.writeByte(move.start().column());
        out.writeByte(move.end().row());
        out.writeByte(move.end().column());
        out.flush();
    }

    public void sendPromotion(PromotionId promotion) throws IOException {
        out.writeByte(PROMOTION);
        out.writeByte(promotion.ordinal());
        out.flush();
    }

    public void sendResign() throws IOException {
        out.writeByte(RESIGN);
        out.flush();
    }

    // null = el oponente abandonó o se cortó la conexión
    public Move receiveMove() {
        try {
            if (in.readByte() != MOVE) {
                return null;
            }
            Position start = new Position(in.readUnsignedByte(), in.readUnsignedByte());
            Position end = new Position(in.readUnsignedByte(), in.readUnsignedByte());
            return new Move(start, end);
        } catch (IOException e) {
            return null;
        }
    }

    public PromotionId receivePromotion() {
        try {
            if (in.readByte() != PROMOTION) {
                return null;
            }
            return PromotionId.values()[in.readUnsignedByte()];
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
