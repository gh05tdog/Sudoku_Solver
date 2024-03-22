package dk.dtu.core;

import dk.dtu.game.core.Board;
import dk.dtu.game.solver.solverAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
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
        try {
            board = new Board(3, 3);
            solverAlgorithm.fillBoard(board);
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
            if (uniqueRows.contains(Arrays.toString(board.getRow(i))) || uniqueCols.contains(Arrays.toString(board.getColumn(i))) ) {
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
                uniqueSquares.add(Arrays.toString(board.getSquare(row,col)));
            }
        }
        return true; // Passed all checks
    }
    @Test
    @DisplayName("Test creating a small 4x4 Sudoku board")
    void testSmallBoardCreation() throws Exception {
        Board board = new Board(2, 2);
        assertNotNull(board, "Small board should be created successfully.");
        assertEquals(4, board.getDimensions(), "Board should have correct dimensions for a 4x4 board.");
    }

    @Test
    @DisplayName("Test solving a small 4x4 Sudoku board")
    void testSmallBoardSolving() throws Exception {
        Board board = new Board(2, 2);
        solverAlgorithm.fillBoard(board);
        assertTrue(isValidSudoku(board), "Small board should adhere to Sudoku rules.");
    }

    @Test
    @DisplayName("Test board validation with incorrect Sudoku")
    void testInvalidSudokuValidation() throws Exception {
        Board board = new Board(3, 3);
        board.setNumber(0, 0, 1);
        board.setNumber(0, 1, 1); // Duplicate in the same row
        assertFalse(isValidSudoku(board), "Board validation should detect incorrect Sudoku.");
    }

    @Test
    @DisplayName("Test board validation with correct Sudoku")
    void testValidSudokuValidation() throws Exception {
        // Fill the board with a correct Sudoku solution first
        Board board = new Board(3, 3);
        solverAlgorithm.fillBoard(board);
        assertTrue(isValidSudoku(board), "Board validation should confirm correct Sudoku.");
    }

    @Test
    @DisplayName("Test solving an impossible Sudoku board")
    void testSolvingImpossibleBoard() throws Exception {
        Board board = new Board(3, 3);
        board.setNumber(0, 0, 1);
        board.setNumber(0, 1, 1); // Create a conflict
        assertFalse(solverAlgorithm.sudoku(board.getBoard()), "Solver should detect the board is unsolvable.");
    }

    @Test
    @DisplayName("Performance test for solving a standard Sudoku")
    void testSolverPerformance() throws Exception {
        Board board = new Board(3, 3);
        long startTime = System.currentTimeMillis();
        solverAlgorithm.fillBoard(board);
        long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) < 1000, "Solver should complete within reasonable time for a standard board.");
    }

    @Test
    @DisplayName("Create sudoku, solve it and test if valid")
    void testCreateSudoku() throws Exception {
        Board board = new Board(3, 3);
        solverAlgorithm.createSudoku(board);
        solverAlgorithm.sudoku(board.getBoard());
        assertTrue(isValidSudoku(board), "Board should adhere to Sudoku rules.");
    }
}
