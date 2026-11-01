package main.core.time;

import java.util.function.LongSupplier;

// Reloj de un jugador: cuenta el tiempo restante mientras el jugador piensa.
// Lo arrancan y frenan los adaptadores de input alrededor de la espera de la
// jugada; ChessService lo consulta para TIME_OUT y para el snapshot del display.
// No es thread-safe: se usa solo desde el hilo principal.
public class PlayerClock {

    private final LongSupplier timeSource; // nanoTime inyectable para los tests
    private final long initialMillis;
    private long remainingMillis;
    private long runningSinceNanos = -1;   // -1 = detenido

    public PlayerClock(long initialMillis) {
        this(initialMillis, System::nanoTime);
    }

    public PlayerClock(long initialMillis, LongSupplier timeSource) {
        this.initialMillis = initialMillis;
        this.remainingMillis = initialMillis;
        this.timeSource = timeSource;
    }

    // Reloj nuevo, detenido, con el mismo tiempo inicial y la misma fuente de tiempo.
    // Sirve para crear el reloj de cada jugador a partir de un control de tiempo.
    public PlayerClock copy() {
        return new PlayerClock(initialMillis, timeSource);
    }

    // Arranca el conteo; si ya estaba corriendo no hace nada.
    public void resume() {
        if (runningSinceNanos == -1) {
            runningSinceNanos = timeSource.getAsLong();
        }
    }

    // Descuenta lo transcurrido y detiene el conteo.
    public void pause() {
        if (runningSinceNanos != -1) {
            remainingMillis = remainingMillis();
            runningSinceNanos = -1;
        }
    }

    // Baja el tiempo restante directamente, sin arrancar ni frenar.
    public void reduceTime(long millis) {
        remainingMillis = Math.max(0, remainingMillis - millis);
    }

    // Restante, descontando lo transcurrido si está corriendo. Nunca baja de 0.
    public long remainingMillis() {
        if (runningSinceNanos == -1) {
            return remainingMillis;
        }
        long elapsed = (timeSource.getAsLong() - runningSinceNanos) / 1_000_000;
        return Math.max(0, remainingMillis - elapsed);
    }

    public boolean isExpired() {
        return remainingMillis() <= 0;
    }
}
