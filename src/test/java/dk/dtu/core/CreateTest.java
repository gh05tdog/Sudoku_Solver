package dk.dtu.core;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.Creater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateTest {

    @Test
    @DisplayName("Test creating an empty board is working")
    void testBoardCreating() {
        Exception exception = null;
        Board board = null;

        try {
            board = new Board(3, 3);
        } catch (Exception e) {
            exception = e;
        }

        // Ensure no exceptions were thrown during board creation
        assertNull(exception, "Board creation should not throw an exception.");

        // Ensure the board is not null
        assertNotNull(board, "Board should be created successfully.");

        // Ensure the board is correctly initialized with zeros and has the correct size
        int expectedDimension = 3 * 3;
        assertEquals(expectedDimension, board.getDimensions(), "Board should have correct dimensions.");

        // Verify that all cells are initialized to zero
        boolean allZeros = true;
        for (int x = 0; x < board.getDimensions(); x++) {
            for (int y = 0; y < board.getDimensions(); y++) {
                if (board.getNumber(x, y) != 0) {
                    allZeros = false;
                    break;
                }
            }
            if (!allZeros) break;
        }
        assertTrue(allZeros, "All cells in the board should be initialized to zero.");
    }

    @Test
    @DisplayName("Test creating a filled board adheres to Sudoku rules")
    void testBoardFilling() {
        Board board = null;
        Creater creater = new Creater();
        try {
            board = new Board(3, 3);
            creater.fillBoard(board);
        } catch (Exception e) {
            fail("Creating or filling the board should not throw an exception.");
        }

        // Ensure the board is completely filled
        for (int x = 0; x < board.getDimensions(); x++) {
            for (int y = 0; y < board.getDimensions(); y++) {
                assertTrue(board.getNumber(x, y) != 0, "All cells in the board should be filled.");
            }
        }

        // Ensure the board adheres to Sudoku rules
        assertTrue(isValidSudoku(board), "The board should adhere to Sudoku rules.");
    }

    @Test
    @DisplayName("Test Sudoku puzzle is unique")
    void testSudokuUniqueness() {
        Board board = null;
        Creater creater = new Creater();

        try {
            board = new Board(3, 3);
            creater.createSudoku(board);
        } catch (Exception e) {
            fail("Creating the Sudoku puzzle should not throw an exception.");
        }

        // Check if the created Sudoku puzzle has a unique solution
        assertTrue(creater.hasUniqueSolution(board), "The Sudoku puzzle should have a unique solution.");
    }

    @Test
    @DisplayName("Test invalid sudoku board")
    void invalidSudokuBoard() {
        Exception exception = null;
        try {
            new Board(2, 3);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception, "This board should not be able to be created");
    }

    private boolean isValidSudoku(Board board) {
        // Check all rows and columns for duplicates
        for (int i = 0; i < board.getDimensions(); i++) {
            if (isUnique(board.getRow(i)) || isUnique(board.getColumn(i))) {
                return false;
            }
        }

        // Check all 3x3 sub grids for duplicates
        for (int row = 0; row < board.getDimensions(); row += 3) {
            for (int col = 0; col < board.getDimensions(); col += 3) {
                if (isUnique(board.getSquare(row, col))) {
                    return false;
                }
            }
        }
        return true; // Passed all checks
    }

    // Helper method to check if all elements in a list are unique
    private boolean isUnique(List<Integer> list) {
        Set<Integer> set = new HashSet<>(list);
        return set.size() != list.size();
    }

}
