package dk.dtu.core;

import dk.dtu.engine.core.WindowManager;
import dk.dtu.game.core.Move;
import dk.dtu.game.core.SudokuGame;
import dk.dtu.game.solver.solverAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UndoTest {
    @Test
    @DisplayName("Test undo reverses the last move")
    void testUndoReversesLastMove() throws Exception {
        // Initialize the game and its components
        SudokuGame game = new SudokuGame(new WindowManager(new JFrame()), 3, 3, 50);

        // Manually initialize the board component to avoid NullPointerException
        game.createBoard(3, 3, 50); // Assuming this is the correct way to initialize the board in your game

        solverAlgorithm.createSudoku(game.gameboard);

        // No need to call displayNumbersVisually here if it's just for visual representation and not part of the test logic

        // Deep copy the board for later comparison
        int[][] initialBoardState = game.deepCopyBoard(game.gameboard.getBoard());

        // Make a move on a valid cell
        int row = 2, col = 2, number = 9;
        game.makeMoveTest(row, col, number);

        // Undo the move
        game.undoMove();

        // Assert the board's state is unchanged from the initial state
        assertTrue(Arrays.deepEquals(initialBoardState, game.gameboard.getBoard()), "Board should return to its initial state after undo.");
    }


    @Test
    @DisplayName("Apply all hints and check for valid Sudoku")
    void testHintsLeadToValidSudoku() throws Exception {
        // Initialize the game
        SudokuGame game = new SudokuGame(new WindowManager(new JFrame()), 3, 3, 50);
        game.createBoard(3, 3, 50); // Set up the board

        // Generate a solvable Sudoku puzzle
        solverAlgorithm.createSudoku(game.gameboard);

        // Remove numbers to generate hints (if not already part of createSudoku)
        // Assuming fillHintList() populates the hintList based on the current board state
        game.fillHintList();

        // Apply all hints to the board
        for (Move hint : game.getHintList()) {
            game.gameboard.setNumber(hint.getRow(), hint.getColumn(), hint.getNumber());
        }

        // Validate the Sudoku board
        assertTrue(solverAlgorithm.isValidSudoku(game.gameboard.getBoard()), "Applying all hints should result in a valid Sudoku.");
    }

}
