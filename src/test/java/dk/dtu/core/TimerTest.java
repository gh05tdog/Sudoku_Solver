package dk.dtu.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dk.dtu.engine.utility.TimerFunction;


import static org.junit.jupiter.api.Assertions.*;
class TimerTest {

    private TimerFunction timer;

    @BeforeEach
    void setUp(){
        timer = new TimerFunction();
    }

    @Test
    @DisplayName("Timer Test")
    void testTimer() {
        //Tests if the timer is working correctly
        timer.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
        String time = timer.getTimeString();
        assertEquals("00:00:02", time);
    }




}
