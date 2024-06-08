package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.GameRulePopup;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.engine.utility.CustomComponentGroup;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.SudokuGame;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

public class GameRulePopupTest {

    private SudokuGame game;
    private CustomComponentGroup componentGroup;
    private CustomBoardPanel panel2;
    private SudokuBoardCanvas sudokuBoardCanvasBoard;

    @BeforeAll
    static void setUpHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws Board.BoardNotCreatable {
        JFrame mockedFrame = mock(JFrame.class);

        StartMenuWindowManager startMenuWindowManager =
                new StartMenuWindowManager(mockedFrame, 1000, 700);
        StartMenu startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();
        startMenu.getStartButton().doClick();
        WindowManager windowManager =
                new WindowManager(startMenuWindowManager.getFrame(), 800, 800);
        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        game.initialize(3, 3, 550 / 9);
        windowManager.setHeart();
        componentGroup = new CustomComponentGroup();

        // Create mock panels
        CustomBoardPanel panel1 = new CustomBoardPanel();
        panel2 = new CustomBoardPanel();
        CustomBoardPanel panel3 = new CustomBoardPanel();

        // Add panels to the group
        componentGroup.addComponent(panel1);
        componentGroup.addComponent(panel2);
        componentGroup.addComponent(panel3);
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
