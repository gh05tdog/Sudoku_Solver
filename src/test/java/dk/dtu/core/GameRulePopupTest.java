package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dk.dtu.engine.graphics.GameRulePopup;
import dk.dtu.game.core.Config;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

public class GameRulePopupTest {

    @BeforeAll
    static void setUpHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        // Ensure headless mode is set before any GUI operations
    }

    @Test
    public void testAddJSwitchBox() {
        if (!GraphicsEnvironment.isHeadless()) {
            GameRulePopup gameRulePopup = new GameRulePopup();
            gameRulePopup.addJSwitchBox("Test", true, (b) -> {});
            assertEquals(2, gameRulePopup.getContentPane().getComponentCount());
        } else {
            GameRulePopup gameRulePopup = new GameRulePopup();
            gameRulePopup.addJSwitchBox("Test", true, (b) -> {});
            assertEquals(0, gameRulePopup.getContentPane().getComponentCount());
        }
    }

    @Test
    public void testConfig() {
        Config.setN(3);
        Config.setK(3);
        Config.setCellSize(50);
        Config.setDifficulty("Easy");
        assertEquals(3, Config.getN());
        assertEquals(3, Config.getK());
        assertEquals(50, Config.getCellSize());
        assertEquals("Easy", Config.getDifficulty());
        Config.setEnableLives(false);
        Config.setEnableTimer(false);
        Config.setEnableEasyMode(false);
        assertFalse(Config.getEnableLives());
        assertFalse(Config.getEnableTimer());
    }
}
