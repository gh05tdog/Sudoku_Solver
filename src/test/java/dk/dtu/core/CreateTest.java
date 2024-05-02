/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.game.core.Board;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        assertEquals(
                expectedDimension, board.getDimensions(), "Board should have correct dimensions.");

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

        Set<String> uniqueRows = new HashSet<>();
        Set<String> uniqueCols = new HashSet<>();

        // Check all rows and columns for duplicates
        for (int i = 0; i < board.getDimensions(); i++) {
            if (uniqueRows.contains(Arrays.toString(board.getRow(i)))
                    || uniqueCols.contains(Arrays.toString(board.getColumn(i)))) {
                return false;
            }
            uniqueRows.add(Arrays.toString(board.getRow(i)));
            uniqueCols.add(Arrays.toString(board.getColumn(i)));
        }

        Set<String> uniqueSquares = new HashSet<>();

        // Check all 3x3 sub grids for duplicates
        for (int row = 0; row < board.getDimensions(); row += 3) {
            for (int col = 0; col < board.getDimensions(); col += 3) {
                if (uniqueSquares.contains(Arrays.toString(board.getSquare(row, col)))) {
                    return false;
                }
                uniqueSquares.add(Arrays.toString(board.getSquare(row, col)));
            }
        }

        // Check all 3x3 sub grids for duplicates

        return true; // Passed all checks
    }

    @Test
    @DisplayName("Test creating a small 4x4 Sudoku board")
    void testSmallBoardCreation() throws Exception {
        Board board = new Board(2, 2);
        assertNotNull(board, "Small board should be created successfully.");
        assertEquals(
                4, board.getDimensions(), "Board should have correct dimensions for a 4x4 board.");
    }


    @Test
    @DisplayName("Test board validation with incorrect Sudoku")
    void testInvalidSudokuValidation() throws Exception {
        Board board = new Board(3, 3);
        board.setNumber(0, 0, 1);
        board.setNumber(0, 1, 1); // Duplicate in the same row
        assertFalse(isValidSudoku(board), "Board validation should detect incorrect Sudoku.");
    }

}
