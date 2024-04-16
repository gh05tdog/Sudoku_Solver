package dk.dtu.core;

import dk.dtu.engine.core.WindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.SudokuGame;
import dk.dtu.game.core.Move;
import dk.dtu.game.core.config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class SudokuGameTest {

    private SudokuGame game;
    private Board board;

    @BeforeEach
    void setUp() throws Exception {
        WindowManager mockWindowManager = new WindowManager(800, 800);
        board = new Board(3, 3);  // A 9x9 board
        game = new SudokuGame(mockWindowManager, 3, 3, 550/(3*3)); // A 9x9 board with cells of size 50
        game.initialize(3, 3, 550/(3*3));  // To setup board and related components
        config.setK(3);
        config.setN(3);
        config.setCellSize(550/(3*3));
    }

    @Test
    @DisplayName("Test game initialization sets up board correctly")
    void testGameInitialization() {
        assertNotNull(board.getBoard(), "Board should be initialized.");
        assertEquals(9, board.getDimensions(), "Board should be 9x9.");
    }

    @Test
    @DisplayName("Test correct handling of valid number placement")
    void testValidNumberPlacement() throws InterruptedException, InvocationTargetException {
        game.onNumbersBoardClicked(150, 150);  // Simulating click on number '5'
        SwingUtilities.invokeAndWait(() -> game.onSudokuBoardClicked(75, 75));  // Placing number 5 at row 1, column 1
        assertEquals(5, board.getNumber(1, 1), "Number 5 should be placed at (1,1).");
    }

    @Test
    @DisplayName("Test move undo functionality")
    void testUndoMove() {
        game.onNumbersBoardClicked(150, 150);  // Assume this selects number 5
        game.onSudokuBoardClicked(75, 75);     // Place number 5 at row 1, column 1
        game.undoMove();
        assertEquals(0, board.getNumber(1, 1), "Undo should clear the number at (1,1).");
    }

    @Test
    @DisplayName("Test number erase functionality")
    void testEraseNumber() {
        game.onNumbersBoardClicked(150, 150);  // Assume this selects number 5
        game.onSudokuBoardClicked(75, 75);     // Place number 5 at row 1, column 1
        game.eraseNumber();
        assertEquals(0, board.getNumber(1, 1), "Erase should clear the number at (1,1).");
    }

    @Test
    @DisplayName("Test providing a hint places correct number")
    void testProvideHint() {
        game.provideHint();  // Assume hints are preloaded correctly in `initialize`
        assertTrue(board.getBoard()[game.getHintList().getFirst().getRow()][game.getHintList().getFirst().getColumn()] != 0, "Hint should place a number on the board.");
    }

    @Test
    @DisplayName("Test keyboard interaction")
    void testKeyboardInteraction() {
        KeyEvent key = new KeyEvent(new JButton(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '5');
        game.typeNumberWithKeyboard(key);
        // Assuming (0,0) is highlighted initially or by some other interaction logic
        assertEquals(5, board.getNumber(0, 0), "Key press '5' should place number 5 at (0,0).");
    }

    @Test
    @DisplayName("Test new game creation resets the board")
    void testNewGameCreation() throws Exception {
        game.newGame();
        assertEquals(0, board.getNumber(0, 0), "New game should reset the board to initial state.");
    }

    @Test
    @DisplayName("Check if game completion detection is correct")
    void testGameCompletionDetection() {
        fillBoardForCompletion();  // Helper method to fill the board to complete state except one cell
        assertFalse(game.isSudokuCompleted(), "Game should not be marked as completed.");
        board.setNumber(8, 8, 9);  // Fill last cell to complete the game
        assertTrue(game.isSudokuCompleted(), "Game should be marked as completed.");
    }

    // Helper method to simulate filling the board except one cell
    private void fillBoardForCompletion() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (row != 8 || col != 8) { // Leave one cell empty
                    board.setNumber(row, col, row+1);
                }
            }
        }
    }
}
