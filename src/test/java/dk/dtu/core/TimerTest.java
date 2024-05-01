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
    void testTimer() {
        // Tests if the timer is working correctly
        timer.start();
        try {
            Thread.sleep(2020);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
        String time = timer.getTimeString();
        assertEquals("00:00:02", time);
    }
}
