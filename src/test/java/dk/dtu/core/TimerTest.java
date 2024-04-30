/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.utility.TimerFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimerTest {

    private TimerFunction timer;

    @BeforeEach
    void setUp() {
        timer = new TimerFunction();
    }

    @Test
    @DisplayName("Timer Test")
    void testTimer() throws InterruptedException {
        timer.start();
        Thread.sleep(1100);

        String time = timer.getTimeString();
        System.out.println("Time: " + time);
        assertTrue(time.compareTo("00:00:01") >= 0, "Timer should show 1 second or more, but shows:" + time);
    }
}
