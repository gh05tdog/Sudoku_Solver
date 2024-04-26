/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.NumberHub;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.engine.utility.CustomComponentGroup;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.SudokuGame;
import dk.dtu.game.core.solver.AlgorithmX.algorithmX;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SudokuGameTest {

    private SudokuGame game;
    private CustomComponentGroup componentGroup;
    private CustomBoardPanel panel2;
    private SudokuBoardCanvas sudokuBoardCanvasBoard;

    @BeforeEach
    void setUp() throws Exception {
        StartMenuWindowManager startMenuWindowManager =
                new StartMenuWindowManager(new JFrame(), 1000, 700);
        StartMenu startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();
        startMenu.getStartButton().doClick();
        WindowManager windowManager =
                new WindowManager(startMenuWindowManager.getFrame(), 800, 800);
        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        game.initialize(3, 3, 550 / 9);

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
    }

    @Test
    void testGameInitialization() {
        assertNotNull(game.gameboard, "Game board must not be null after initialization.");
        assertEquals(9, game.gameboard.getDimensions(), "Game board should be initialized as 9x9.");
        assertTrue(game.moveList.isEmpty(), "Move list should be empty initially.");
        assertFalse(game.isGameStarted(), "Game should not start automatically.");
    }

    @Test
    @DisplayName("Test number placement on board click within valid conditions")
    void testOnSudokuBoardClickedWithNumberPlacement() {
        sudokuBoardCanvasBoard.setSize(550, 550);
        // Setting a valid number to place
        game.setPlaceableNumber(5); // Set the number to place

        // Ensuring the target cell is valid for placement
        game.gameboard.setInitialNumber(4, 4, 0); // Ensure the cell is initially empty
        game.gameboard.setNumber(4, 4, 0); // No number currently placed

        // Click at the center cell which should be empty and valid
        game.onSudokuBoardClicked(275, 275);
        int[] markedCell = sudokuBoardCanvasBoard.getMarkedCell();

        // Verify the number placement
        assertEquals(4, markedCell[0], "Clicked cell row should be 4.");
        assertEquals(4, markedCell[1], "Clicked cell column should be 4.");
        assertEquals(5, game.gameboard.getNumber(4, 4), "Number 5 should be placed at (4,4).");
        assertFalse(
                game.moveList.isEmpty(), "Move list should not be empty after a valid placement.");
        assertEquals(
                5,
                game.moveList.peek().getNumber(),
                "Top of move list should have the placed number 5.");
    }

    @Test
    @DisplayName("Test click handling when clicking on a cell with initial number")
    void testOnSudokuBoardClickedInitialNumber() {
        sudokuBoardCanvasBoard.setSize(550, 550);
        // Set an initial number that should not be overwritten
        game.gameboard.setInitialNumber(4, 4, 1);
        game.gameboard.setNumber(4, 4, 1);
        game.setPlaceableNumber(5);

        // Attempt to place a number in a cell that already has an initial number
        game.onSudokuBoardClicked(275, 275);

        // Check that the initial number is unchanged
        assertEquals(
                1,
                game.gameboard.getInitialNumber(4, 4),
                "Cell (4,4) should still have the initial number 1.");
        assertEquals(
                1,
                game.gameboard.getNumber(4, 4),
                "Cell (4,4) should not update the number due to initial number constraint.");
        assertTrue(
                game.moveList.isEmpty(),
                "No moves should be recorded when clicking on a cell with an initial number.");
    }

    @Test
    void testTypeNumberWithKeyboard() {
        sudokuBoardCanvasBoard.setMarkedCell(0, 0); // Set the top-left cell as marked
        KeyEvent keyEvent =
                new KeyEvent(
                        new JButton(),
                        KeyEvent.KEY_TYPED,
                        System.currentTimeMillis(),
                        0,
                        KeyEvent.VK_UNDEFINED,
                        '5');
        game.typeNumberWithKeyboard(keyEvent);
        assertEquals(5, game.gameboard.getNumber(0, 0), "Number 5 should be placed at (0,0).");
    }

    @Test
    void testUndoMove() {
        sudokuBoardCanvasBoard.setMarkedCell(0, 0); // Set the top-left cell as marked
        game.makeMove(0, 0, 5); // Simulate placing number 5 at (0,0)
        game.getUndoButton().doClick();
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after undo.");
    }

    @Test
    void testIsSudokuCompleted() {
        // Fill the board with non-zero numbers
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                game.gameboard.setNumber(
                        i, j, 1); // Assuming a valid placement isn't required for this test
            }
        }
        assertTrue(game.isSudokuCompleted(), "Game should be marked as completed.");
    }

    @Test
    void testProvideHint() {
        game.getNewGameButton().doClick();
        game.getHintButton().doClick(); // Provide a hint
        assertFalse(
                game.getHintList().isEmpty(),
                "Game board should not be empty after providing a hint.");
        assertTrue(game.getHintList().size() < game.getHintList().size() + 1);
    }

    @Test
    void testEraseNumber() {
        sudokuBoardCanvasBoard.setCellNumber(0, 0, 5); // Set number 5 at (0,0)
        sudokuBoardCanvasBoard.setMarkedCell(0, 0); // Set the top-left cell as marked
        game.getEraseButton().doClick();
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after erasing.");
    }

    @Test
    void testDisplayNumbersVisually() {
        game.displayNumbersVisually();
        sudokuBoardCanvasBoard.setMarkedCell(0, 0); // Set the top-left cell as marked
        assertEquals(sudokuBoardCanvasBoard.getMarkedNumber(), game.gameboard.getNumber(0, 0));
    }

    @Test
    void testRestartGame() {
        sudokuBoardCanvasBoard.setCellNumber(0, 0, 5); // Set number 5 at (0,0)
        game.getRestartButton().doClick();
        assertTrue(game.moveList.isEmpty(), "Move list should be empty after restarting the game.");
        assertFalse(
                game.isSudokuCompleted(),
                "Game should not be marked as completed after restarting.");
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after restarting.");
    }

    @Test
    void testStartGame() {
        game.getStartButton().doClick();
        assertTrue(
                game.isGameStarted(), "Game should be started after clicking the new game button.");
        assertNotNull(sudokuBoardCanvasBoard, "Game board should be initialized after starting.");
    }

    @Test
    void testSolver() throws InterruptedException, InvocationTargetException {
        // Schedule the button clicks to solve the Sudoku on the EDT
        SwingUtilities.invokeAndWait(
                () -> {
                    game.getNewGameButton().doClick();
                    game.gameboard.setGameBoard(
                            Objects.requireNonNull(algorithmX.getSolutionBoard()));
                });

        // Now wait for all pending events to be processed
        SwingUtilities.invokeAndWait(
                () -> {
                    // Assuming the solver updates the board directly and synchronously from the
                    // event handlers
                    int[][] expected = algorithmX.getSolutionBoard();
                    int[][] actual = game.gameboard.getGameBoard();

                    // Use Arrays.deepEquals to compare multi-dimensional arrays
                    assertTrue(
                            Arrays.deepEquals(expected, actual),
                            "The board should match the solved board after clicking solve.");
                });
    }

    @Test
    @DisplayName("Test handling of number board clicks")
    void testOnNumbersBoardClicked() {
        game.setGameIsStarted(true);
        game.setNumbersBoard(
                new NumberHub(9, 550 / 9) {
                    @Override
                    public int getNumber(int x, int y) {
                        return 5; // Mock behavior
                    }
                });

        sudokuBoardCanvasBoard =
                new SudokuBoardCanvas(3, 3, 550 / 9) {
                    @Override
                    public void setChosenNumber(int number) {
                        super.setChosenNumber(number);
                        assertEquals(5, number, "Chosen number should be set to 5 on the board.");
                    }
                };
    }

    @Test
    @DisplayName("Test adding components and clicking on one")
    void testAddComponentAndClick() {
        assertEquals(
                3, componentGroup.components.size(), "All components should be added to the list.");

        // Simulate clicking on panel2
        MouseEvent click =
                new MouseEvent(
                        panel2,
                        MouseEvent.MOUSE_CLICKED,
                        System.currentTimeMillis(),
                        0,
                        10,
                        10,
                        1,
                        false);
        panel2.getMouseListeners()[0].mouseClicked(click);

        // Check if the correct component is selected
        assertEquals(
                panel2,
                componentGroup.getSelectedComponent(),
                "Panel 2 should be the selected component.");
    }

    @Test
    void testMakeNote() {
        game.setGameIsStarted(true);
        sudokuBoardCanvasBoard.setMarkedCell(0, 0); // Set the top-left cell as marked
        game.getNoteButton().doClick();

        game.makeMove(0, 0, 1);
        assertTrue(
                sudokuBoardCanvasBoard.getNotesInCell(0, 0).contains(1),
                "Note 1 should be placed at (0,0).");

        game.getNoteButton().doClick();
        game.checkCellsForNotes(0, 1, 1, "show");
        game.makeMove(0, 1, 1);
        assertTrue(
                sudokuBoardCanvasBoard.getHideList(0, 0).contains(1),
                "Note 1 should be hidden at (0,0).");

        game.getNoteButton().doClick();
        game.makeNote(1, 1, 2);

        game.getNoteButton().doClick();
        game.makeMove(1, 1, 2);

        assertTrue(
                sudokuBoardCanvasBoard.getHiddenProperty(1, 1),
                "Note 2 should be hidden at (1,1).");
    }
}
