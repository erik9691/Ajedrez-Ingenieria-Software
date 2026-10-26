package adapters.driven.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Descubrimiento de partidas en la LAN por broadcast UDP: el que busca manda
// un probe a la red y los hosts que están esperando oponente responden con
// su nombre. El puerto es solo de descubrimiento; el juego viaja por TCP.
public class LanDiscovery {
    public static final int DISCOVERY_PORT = 4445;

    private static final String PROBE = "CHESS_PROBE";
    private static final String REPLY_PREFIX = "CHESS_REPLY|";

    public record Host(String ip, String name) {}

    // Emite el probe y junta las respuestas durante timeoutMillis
    public static List<Host> scan(long timeoutMillis) throws IOException {
        List<Host> hosts = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(500);
            byte[] probe = PROBE.getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(probe, probe.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT));

            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                try {
                    byte[] buffer = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (message.startsWith(REPLY_PREFIX)) {
                        hosts.add(new Host(packet.getAddress().getHostAddress(),
                                message.substring(REPLY_PREFIX.length())));
                    }
                } catch (SocketTimeoutException e) {
                    // sigue escuchando hasta el deadline
                }
            }
        }
        return hosts;
    }

    // Hilo que responde a los probes mientras el host espera la conexión TCP.
    // Daemon: muere solo cuando termina el proceso.
    public static Thread responder(String playerName) {
        Thread thread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                byte[] buffer = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (!message.equals(PROBE)) {
                        continue;
                    }
                    byte[] reply = (REPLY_PREFIX + playerName).getBytes(StandardCharsets.UTF_8);
                    socket.send(new DatagramPacket(reply, reply.length,
                            packet.getAddress(), packet.getPort()));
                }
            } catch (IOException e) {
                // puerto ocupado o proceso terminando: no responde más
            }
        }, "lan-discovery");
        thread.setDaemon(true);
        return thread;
    }
}
