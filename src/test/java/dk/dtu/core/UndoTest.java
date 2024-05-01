/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Move;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.SudokuGame;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import dk.dtu.game.core.solver.bruteforce.BruteForceAlgorithm;
import java.util.Arrays;
import javax.swing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UndoTest {

    private SudokuGame game;

    @BeforeEach
    void setUp() throws Board.BoardNotCreatable {
        // Mock the JFrame to avoid real GUI initialization
        JFrame mockedFrame = mock(JFrame.class);
        StartMenuWindowManager startMenuManager =
                new StartMenuWindowManager(mockedFrame, 1000, 700);
        StartMenu startMenu = new StartMenu(startMenuManager);
        startMenu.initialize();
        startMenu.getStartButton().doClick();
        WindowManager windowManager = new WindowManager(startMenuManager.getFrame(), 800, 800);
        game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        game.initialize(3, 3, 550 / 9);
    }

    @Test
    @DisplayName("Test undo reverses the last move")
    void testUndoReversesLastMove() {
        // Assuming AlgorithmX.createXSudoku is a static method
        AlgorithmXSolver.createXSudoku(game.gameboard);

        // Deep copy the board for later comparison
        int[][] initialBoardState =
                Arrays.stream(game.gameboard.getGameBoard())
                        .map(int[]::clone)
                        .toArray(int[][]::new);

        // Make a move on a valid cell
        int row = 2, col = 2, number = 9;
        game.makeMove(row, col, number);

        // Undo the move
        game.undoMove();

        // Assert the board's state is unchanged from the initial state
        assertTrue(
                Arrays.deepEquals(initialBoardState, game.gameboard.getGameBoard()),
                "Board should return to its initial state after undo.");
    }

    @Test
    @DisplayName("Apply all hints and check for valid Sudoku")
    void testHintsLeadToValidSudoku() {
        AlgorithmXSolver.createXSudoku(game.gameboard);
        game.fillHintList();

        // Apply all hints to the board
        for (Move hint : game.getHintList()) {
            game.gameboard.setNumber(hint.getRow(), hint.getColumn(), hint.getNumber());
        }

        // Validate the Sudoku board
        assertTrue(
                BruteForceAlgorithm.isValidSudoku(game.gameboard.getGameBoard()),
                "Applying all hints should result in a valid Sudoku.");
    }
}