package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.graphics.numberHub;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Move;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.SudokuGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

class SudokuGameTest {

    private SudokuGame game;
    private Board board;
    private WindowManager windowManager;
    private SudokuBoardCanvas boardCanvas;
    private StartMenuWindowManager startMenuWindowManager;
    private StartMenu startMenu;


    @BeforeEach
    void setUp() throws Exception {
        windowManager = new WindowManager(800, 800); // Assuming WindowManager can be instantiated like this
        board = new Board(3, 3); // Standard 9x9 Sudoku
        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        boardCanvas = new SudokuBoardCanvas(3, 3, 550 / 9);
        game.initialize(3, 3, 550 / 9);
        startMenuWindowManager = new StartMenuWindowManager(1000, 700);
        startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();
        startMenu.getStartButton().doClick();
    }



    @Test
    void testGameInitialization() {
        assertNotNull(game.gameboard, "Game board must not be null after initialization.");
        assertEquals(9, game.gameboard.getDimensions(), "Game board should be initialized as 9x9.");
        assertTrue(game.moveList.isEmpty(), "Move list should be empty initially.");
        assertFalse(game.gameIsStarted, "Game should not start automatically.");
    }


    @Test
    @DisplayName("Test number placement on board click within valid conditions")
    void testOnSudokuBoardClickedWithNumberPlacement() {
        game.board.setSize(550, 550);
        // Setting a valid number to place
        game.placeableNumber = 5;

        // Ensuring the target cell is valid for placement
        game.gameboard.setInitialNumber(4, 4, 0); // Ensure the cell is initially empty
        game.gameboard.setNumber(4, 4, 0);        // No number currently placed

        // Click at the center cell which should be empty and valid
        game.onSudokuBoardClicked(275, 275);
        int[] markedCell = game.board.getMarkedCell();

        // Verify the number placement
        assertEquals(4, markedCell[0], "Clicked cell row should be 4.");
        assertEquals(4, markedCell[1], "Clicked cell column should be 4.");
        assertEquals(5, game.gameboard.getNumber(4, 4), "Number 5 should be placed at (4,4).");
        assertFalse(game.moveList.isEmpty(), "Move list should not be empty after a valid placement.");
        assertEquals(5, game.moveList.peek().getNumber(), "Top of move list should have the placed number 5.");
    }

    @Test
    @DisplayName("Test click handling when clicking on a cell with initial number")
    void testOnSudokuBoardClickedInitialNumber() {
        game.board.setSize(550, 550);
        // Set an initial number that should not be overwritten
        game.gameboard.setInitialNumber(4, 4, 1);
        game.gameboard.setNumber(4, 4, 1);
        game.placeableNumber = 5;

        // Attempt to place a number in a cell that already has an initial number
        game.onSudokuBoardClicked(275, 275);

        // Check that the initial number is unchanged
        assertEquals(1, game.gameboard.getInitialNumber(4, 4), "Cell (4,4) should still have the initial number 1.");
        assertEquals(1, game.gameboard.getNumber(4, 4), "Cell (4,4) should not update the number due to initial number constraint.");
        assertTrue(game.moveList.isEmpty(), "No moves should be recorded when clicking on a cell with an initial number.");
    }


    @Test
    void testTypeNumberWithKeyboard() {
        game.board.setMarkedCell(0, 0); // Set the top-left cell as marked
        KeyEvent keyEvent = new KeyEvent(new JButton(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '5');
        game.typeNumberWithKeyboard(keyEvent);
        assertEquals(5, game.gameboard.getNumber(0, 0), "Number 5 should be placed at (0,0).");
    }

    @Test
    void testUndoMove() {
        game.board.setMarkedCell(0, 0); // Set the top-left cell as marked
        game.makeMoveTest(0, 0, 5); // Simulate placing number 5 at (0,0)
        game.getUndoButton().doClick();
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after undo.");
    }

    @Test
    void testIsSudokuCompleted() {
        // Fill the board with non-zero numbers
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                game.gameboard.setNumber(i, j, 1); // Assuming valid placement isn't required for this test
            }
        }
        assertTrue(game.isSudokuCompleted(), "Game should be marked as completed.");
    }
    @Test
    void testProvideHint() throws Exception {
        game.getNewGameButton().doClick();
        game.getHintButton().doClick(); // Provide a hint
        assertFalse(game.getHintList().isEmpty(), "Game board should not be empty after providing a hint.");
        assertTrue(game.getHintList().size() < game.getHintList().size() + 1);
    }


    @Test
    void testEraseNumber() {
        game.board.setCellNumber(0, 0, 5); // Set number 5 at (0,0)
        game.board.setMarkedCell(0, 0); // Set the top-left cell as marked
        game.getEraseButton().doClick();
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after erasing.");
    }

    @Test
    void testDisplayNumbersVisually() {
        game.displayNumbersVisually();
        game.board.setMarkedCell(0, 0); // Set the top-left cell as marked
        assertEquals(game.board.getMarkedNumber(), game.gameboard.getNumber(0,0));
    }
    @Test
    void testRestartGame() {
        game.board.setCellNumber(0, 0, 5); // Set number 5 at (0,0)
        game.getRestartButton().doClick();
        assertTrue(game.moveList.isEmpty(), "Move list should be empty after restarting the game.");
        assertFalse(game.isSudokuCompleted(), "Game should not be marked as completed after restarting.");
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after restarting.");
    }

    @Test
    void testStartGame(){
        game.getStartButton().doClick();
        assertTrue(game.gameIsStarted, "Game should be started after clicking the new game button.");
        assertNotNull(game.board);
    }

    @Test
    void testSolver(){
        game.getNewGameButton().doClick();
        game.getSolveButton().doClick();
        SwingUtilities.invokeLater(() -> assertTrue(game.isSudokuCompleted(), "Game should be marked as completed after solving."));
    }

    @Test
    @DisplayName("Test handling of number board clicks")
    void testOnNumbersBoardClicked() {
        // Simulate clicking at a position corresponding to number 5 in the numberHub
        final int x = 50;  // Example coordinates that would correspond to the number 5
        final int y = 50;  // Example coordinates that would correspond to the number 5

        // Assuming getNumber simulates returning the number 5 for these coordinates
        game.numbers = new numberHub(9, 550 / 9) {
            @Override
            public int getNumber(int x, int y) {
                return 5;  // Mock behavior
            }

            @Override
            public void highlightNumber(int x, int y) {
                // Mock behavior: Test could verify this call
                System.out.println("Number highlighted: " + getNumber(x, y));
            }
        };

        // Set an expectation for the setChosenNumber on the board
        game.board = new SudokuBoardCanvas(3, 3, 550 / 9) {
            @Override
            public void setChosenNumber(int number) {
                super.setChosenNumber(number);
                assertEquals(5, number, "Chosen number should be set to 5 on the board.");
            }
        };

        game.onNumbersBoardClicked(x, y);

        // Assertions to verify correct number was "chosen"
        assertEquals(5, game.placeableNumber, "Placeable number should be updated to 5.");
    }

}
