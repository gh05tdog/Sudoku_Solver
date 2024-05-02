/* (C)2024 */
package dk.dtu.core;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.time.Duration.ofSeconds;

import dk.dtu.engine.utility.TimerFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

class TimerTest {

    private TimerFunction timer;

    @BeforeEach
    void setUp() {
        timer = new TimerFunction();
    }

    @Test
    @DisplayName("Timer should count at least 1 second")
    void testTimer() {
        timer.start();

        /* Use Awaitility to wait up to 2 seconds until the timer counts at least 1 second */
        await().atMost(ofSeconds(2)).until(timerHasReachedOneSecond());

        String time = timer.getTimeString();
        System.out.println("Time: " + time);
        // Assert that the timer string is "00:00:01" or more
        assertTrue(time.compareTo("00:00:01") >= 0,
                "Timer should show 1 second or more, but shows:" + time);
    }

    private Callable<Boolean> timerHasReachedOneSecond() {
        return () -> {
            String time = timer.getTimeString();
            return time.compareTo("00:00:01") >= 0;
        };
    }
}
