/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.graphics.NumberHub;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
import dk.dtu.game.core.SudokuGame;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListenerTests {
    private TestableSudokuGame game;
    private KeyboardListener keyboardListener;
    private MouseActionListener mouseListener;
    private SudokuBoardCanvas boardCanvas;
    private NumberHub numbersBoard;

    static class TestableSudokuGame extends SudokuGame {
        boolean numberTyped = false;
        boolean boardClicked = false;
        boolean numbersBoardClicked = false;
        int lastX = -1;
        int lastY = -1;

        public TestableSudokuGame() throws Exception {
            super(null, 9, 9, 40); // Assuming these are reasonable defaults
        }

        @Override
        public void typeNumberWithKeyboard(KeyEvent e) {
            numberTyped = true;
        }

        @Override
        public void onSudokuBoardClicked(int x, int y) {
            boardClicked = true;
            lastX = x;
            lastY = y;
        }

        @Override
        public void onNumbersBoardClicked(int x, int y) {
            numbersBoardClicked = true;
            lastX = x;
            lastY = y;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        game = new TestableSudokuGame();
        keyboardListener = new KeyboardListener(game);
        mouseListener = new MouseActionListener(game);

        boardCanvas = new SudokuBoardCanvas(9, 9, 40); // Assuming constructor parameters as required
        numbersBoard = new NumberHub(9, 40) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                // Do nothing
            }
        };
    }

    @Test
    @DisplayName("Test Keyboard Input Handling")
    void testKeyboardInputHandling() {
        KeyEvent keyEvent =
                new KeyEvent(
                        new JButton(),
                        KeyEvent.KEY_TYPED,
                        System.currentTimeMillis(),
                        0,
                        KeyEvent.VK_UNDEFINED,
                        '5');
        keyboardListener.keyTyped(keyEvent);
        assertTrue(game.numberTyped, "Number should have been typed.");
    }

    @Test
    @DisplayName("Test Mouse Clicks on Sudoku Board")
    void testMouseClicksOnSudokuBoard() {
        MouseEvent clickEvent =
                new MouseEvent(
                        boardCanvas,
                        MouseEvent.MOUSE_CLICKED,
                        System.currentTimeMillis(),
                        0,
                        100,
                        100,
                        1,
                        false);
        mouseListener.mouseEntered(
                new MouseEvent(
                        boardCanvas,
                        MouseEvent.MOUSE_ENTERED,
                        System.currentTimeMillis(),
                        0,
                        100,
                        100,
                        0,
                        false));
        mouseListener.mouseClicked(clickEvent);

        assertTrue(game.boardClicked, "Mouse should have clicked Sudoku board.");
        assertEquals(100, game.lastX, "X coordinate should match.");
        assertEquals(100, game.lastY, "Y coordinate should match.");
    }

    @Test
    @DisplayName("Test Mouse Clicks on Numbers Board")
    void testMouseClicksOnNumbersBoard() {
        MouseEvent clickEvent =
                new MouseEvent(
                        numbersBoard,
                        MouseEvent.MOUSE_CLICKED,
                        System.currentTimeMillis(),
                        0,
                        50,
                        50,
                        1,
                        false);
        mouseListener.mouseEntered(
                new MouseEvent(
                        numbersBoard,
                        MouseEvent.MOUSE_ENTERED,
                        System.currentTimeMillis(),
                        0,
                        50,
                        50,
                        0,
                        false));
        mouseListener.mouseClicked(clickEvent);

        assertTrue(game.numbersBoardClicked, "Mouse should have clicked Numbers board.");
        assertEquals(50, game.lastX, "X coordinate should match.");
        assertEquals(50, game.lastY, "Y coordinate should match.");
    }

    @Test
    @DisplayName("Test Mouse Exit Resets State")
    void testMouseExitResetsState() {
        mouseListener.mouseEntered(
                new MouseEvent(
                        boardCanvas,
                        MouseEvent.MOUSE_ENTERED,
                        System.currentTimeMillis(),
                        0,
                        100,
                        100,
                        0,
                        false));
        mouseListener.mouseExited(
                new MouseEvent(
                        boardCanvas,
                        MouseEvent.MOUSE_EXITED,
                        System.currentTimeMillis(),
                        0,
                        100,
                        100,
                        0,
                        false));

        assertFalse(
                mouseListener.getIsInsideSudokuBoard(),
                "Mouse should not be considered inside Sudoku board after exit.");
    }
}
