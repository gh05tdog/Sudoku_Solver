/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.graphics.GameRulePopup;
import dk.dtu.game.core.Config;
import org.junit.jupiter.api.Test;

public class GameRulePopupTest {

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
