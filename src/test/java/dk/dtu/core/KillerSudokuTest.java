/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.engine.utility.CustomComponentGroup;
import dk.dtu.game.core.*;

import javax.swing.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KillerSudokuTest {

    private SudokuGame game;
    private SudokuBoardCanvas sudokuBoardCanvasBoard;

    @BeforeEach
    void setUp() throws Exception {
        JFrame mockedFrame = mock(JFrame.class);
        Config.setEnableKillerSudoku(true);

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
        CustomComponentGroup componentGroup = new CustomComponentGroup();

        // Create mock panels
        CustomBoardPanel panel1 = new CustomBoardPanel();
        CustomBoardPanel panel2 = new CustomBoardPanel();
        CustomBoardPanel panel3 = new CustomBoardPanel();

        // Add panels to the group
        componentGroup.addComponent(panel1);
        componentGroup.addComponent(panel2);
        componentGroup.addComponent(panel3);

        sudokuBoardCanvasBoard = game.getBoard();

        System.setProperty("testMode", "true");
        game.clearBoard();
        Config.setEnableLives(true);
    }

    @Test
    void testGenerateKillerSudokuCages() {
        game.generateKillerSudokuCages();
        assertTrue(sudokuBoardCanvasBoard.getCages().size() > 1);
        assertTrue(sudokuBoardCanvasBoard.getCages().getFirst().getSum() > 0);
    }

}
