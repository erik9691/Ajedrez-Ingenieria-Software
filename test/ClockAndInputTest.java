import static org.junit.jupiter.api.Assertions.*;

import main.adapters.driven.input.ConsoleInputAdapter;
import main.core.flags.*;
import main.core.models.*;
import main.core.time.PlayerClock;
import main.ports.dto.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

class ClockAndInputTest {
    final AtomicLong now = new AtomicLong();
    final PlayerClock clock = new PlayerClock(1000, now::get);
    final BoardView view = new BoardView(new PieceView[8][8]);

    @Test
    void pausedClockDoesNotCount() {
        now.set(900_000_000);
        assertEquals(1000, clock.remainingMillis());
    }

    @Test
    void elapsedTimeIsSubtracted() {
        clock.resume();
        now.set(400_000_000);
        assertEquals(600, clock.remainingMillis());
    }

    @Test
    void resumeIsIdempotent() {
        clock.resume();
        now.set(400_000_000);
        clock.resume();
        now.set(600_000_000);
        assertEquals(400, clock.remainingMillis());
    }

    @Test
    void pauseIsIdempotent() {
        clock.resume();
        now.set(400_000_000);
        clock.pause();
        now.set(900_000_000);
        clock.pause();
        assertEquals(600, clock.remainingMillis());
    }

    @Test
    void resumeAfterPauseCountsOnlyActiveTime() {
        clock.resume();
        now.set(200_000_000);
        clock.pause();
        now.set(500_000_000);
        clock.resume();
        now.set(800_000_000);
        assertEquals(500, clock.remainingMillis());
    }

    @Test
    void expiredClockStopsAtZero() {
        clock.resume();
        now.set(2_000_000_000);
        assertEquals(0, clock.remainingMillis());
        assertTrue(clock.isExpired());
    }

    @Test
    void copyHasIndependentFullTime() {
        clock.reduceTime(800);
        PlayerClock copy = clock.copy();
        assertEquals(1000, copy.remainingMillis());
        assertEquals(200, clock.remainingMillis());
    }

    @Test
    void negativeNanoTimeIsValid() {
        now.set(-1);
        clock.resume();
        now.set(499_999_999);
        assertEquals(500, clock.remainingMillis());
    }

    @Test
    void zeroClockIsExpired() {
        assertTrue(new PlayerClock(0).isExpired());
    }

    @Test
    void negativeInitialTimeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerClock(-1));
    }

    @Test
    void negativeReductionRejected() {
        assertThrows(IllegalArgumentException.class, () -> clock.reduceTime(-1));
    }

    @Test
    void reductionWhileRunningPreservesElapsedTime() {
        clock.resume();
        now.set(200_000_000);
        clock.reduceTime(300);
        assertEquals(500, clock.remainingMillis());
    }

    @ParameterizedTest
    @ValueSource(strings = {"e2\ne4\n", " E2 \n E4 \n"})
    void inputParsesCoordinates(String text) {
        InputResult result = new ConsoleInputAdapter(new Scanner(text)).requestMove(view);
        assertEquals(new Move(new Position(6, 4), new Position(4, 4)), result.move());
        assertEquals(PlayerStatus.OK, result.status());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "e2\n", "CHAU\n", "e2\nchau\n", " Chau \n"})
    void closedInputAndForfeitAreHandled(String text) {
        assertEquals(
                PlayerStatus.FORFEIT,
                new ConsoleInputAdapter(new Scanner(text)).requestMove(view).status());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "a9\na1\ne2\ne4\n",
                "xyz\na1\ne2\ne4\n",
                "a0\na1\ne2\ne4\n",
                "a1\nz9\ne2\ne4\n"
            })
    void invalidCoordinatesRetry(String text) {
        assertEquals(
                new Move(new Position(6, 4), new Position(4, 4)),
                new ConsoleInputAdapter(new Scanner(text)).requestMove(view).move());
    }

    @ParameterizedTest
    @ValueSource(strings = {"q", " Q ", "r", "b", "n", "\nxx\nz\nq"})
    void promotionRetriesAndAcceptsValidPiece(String text) {
        assertNotNull(new ConsoleInputAdapter(new Scanner(text)).requestPromotion(view));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "CHAU", " chau "})
    void promotionCanBeAbandoned(String text) {
        assertNull(new ConsoleInputAdapter(new Scanner(text)).requestPromotion(view));
    }

    @Test
    void expiredInputDoesNotReadMove() {
        ConsoleInputAdapter input = new ConsoleInputAdapter(new Scanner("e2\ne4"));
        input.setClock(new PlayerClock(0));
        assertEquals(PlayerStatus.TIME_OVER, input.requestMove(view).status());
    }
}
