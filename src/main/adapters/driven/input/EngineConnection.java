package main.adapters.driven.input;

import java.io.IOException;

public interface EngineConnection extends AutoCloseable {
    String request(String commands, String responsePrefix, long timeoutMillis) throws IOException;

    @Override
    void close();
}
