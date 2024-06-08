/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.graphics.GameRulePopup;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.StartMenu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;


public class GameRulePopupTest {
    @BeforeEach
    void setUp() {
        JFrame mockedFrame = mock(JFrame.class);
        StartMenuWindowManager startMenuWindowManager =
                new StartMenuWindowManager(mockedFrame, 800, 600);
        StartMenu startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();

        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void testAddJSwitchBox() {
        GameRulePopup gameRulePopup = new GameRulePopup();
        gameRulePopup.addJSwitchBox("Test", true, (b) -> {});
        assertEquals(2, gameRulePopup.getContentPane().getComponentCount());
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
