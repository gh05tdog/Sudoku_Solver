package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.engine.utility.CustomComponentGroup;
import dk.dtu.game.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class KillerSudokuTest {

    private SudokuGame game;
    private CustomComponentGroup componentGroup;
    private CustomBoardPanel panel2;
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
        componentGroup = new CustomComponentGroup();

        // Create mock panels
        CustomBoardPanel panel1 = new CustomBoardPanel();
        panel2 = new CustomBoardPanel();
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

    @Test
    void testAdjustInitialNumbersVisibility() {
        Config.setDifficulty("easy");
        game.adjustInitialNumbersVisibility();

        long countVisibleNumbers;

        Config.setDifficulty("extreme");
        game.adjustInitialNumbersVisibility();
        countVisibleNumbers = countVisibleNumbers();
        assertEquals(0, countVisibleNumbers); // Check extreme difficulty, no numbers visible
    }

    private long countVisibleNumbers() {
        int count = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (game.gameboard.getNumber(row, col) > 0) {
                    count++;
                }
            }
        }
        return count;
    }


}
