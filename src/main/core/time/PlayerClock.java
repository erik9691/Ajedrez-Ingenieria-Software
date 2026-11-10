package main.core.time;

import java.util.Objects;
import java.util.function.LongSupplier;

public class PlayerClock {
    private final LongSupplier timeSource;
    private final long initialMillis;
    private long remainingMillis;
    private long runningSinceNanos;
    private boolean running;

    public PlayerClock(long millis) {
        this(millis, System::nanoTime);
    }

    public PlayerClock(long millis, LongSupplier timeSource) {
        if (millis < 0) throw new IllegalArgumentException("Tiempo inicial negativo");
        this.initialMillis = millis;
        this.remainingMillis = millis;
        this.timeSource = Objects.requireNonNull(timeSource);
    }

    public PlayerClock copy() {
        return new PlayerClock(initialMillis, timeSource);
    }

    public synchronized void resume() {
        if (!running) {
            runningSinceNanos = timeSource.getAsLong();
            running = true;
        }
    }

    public synchronized void pause() {
        if (running) {
            remainingMillis = remainingMillis();
            running = false;
        }
    }

    public synchronized void reduceTime(long millis) {
        if (millis < 0) throw new IllegalArgumentException("Descuento negativo");
        remainingMillis = Math.max(0, remainingMillis - millis);
    }

    public synchronized long remainingMillis() {
        if (!running) return remainingMillis;
        long elapsed = Math.max(0, (timeSource.getAsLong() - runningSinceNanos) / 1_000_000);
        return Math.max(0, remainingMillis - elapsed);
    }

    public boolean isExpired() {
        return remainingMillis() <= 0;
    }
}
