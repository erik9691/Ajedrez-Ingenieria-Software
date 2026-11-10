package main.adapters.driven.input;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class UciEngine implements EngineConnection {
    private final Process process;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final ExecutorService reads =
            Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "stockfish-output");
                thread.setDaemon(true);
                return thread;
            });
    private volatile boolean closed;

    public UciEngine(String path) throws IOException {
        this(java.util.List.of(path));
    }

    public UciEngine(java.util.List<String> command) throws IOException {
        process =
                new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        writer =
                new PrintWriter(
                        new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8),
                        true);
        reader =
                new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        try {
            request("uci", "uciok", 5000);
            request("isready", "readyok", 5000);
        } catch (IOException failure) {
            close();
            throw failure;
        }
    }

    @Override
    public String request(String commands, String prefix, long timeout) throws IOException {
        if (closed) throw new IOException("Motor cerrado");
        writer.println(commands);
        if (writer.checkError()) throw new IOException("No se pudo escribir al motor");
        Future<String> response =
                reads.submit(() -> {
                    String line;
                    while ((line = reader.readLine()) != null)
                        if (line.startsWith(prefix)) return line;
                    throw new EOFException("El motor cerro su salida");
                });
        try {
            return response.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            close();
            throw new IOException("Lectura interrumpida", e);
        } catch (TimeoutException | ExecutionException e) {
            close();
            throw new IOException("El motor no respondio correctamente", e);
        } finally {
            response.cancel(true);
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        process.destroyForcibly();
        reads.shutdownNow();
        writer.close();
        try {
            reader.close();
        } catch (IOException ignored) {
        }
    }
}
