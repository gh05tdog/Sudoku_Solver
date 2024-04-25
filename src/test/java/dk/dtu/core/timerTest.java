package dk.dtu.core;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dk.dtu.engine.utility.Timer;


import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

public class timerTest {

    private Timer timer;

    @BeforeEach
    void setUp(){
        timer = new Timer();
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
