/* (C)2024 */
package dk.dtu.core;

import static dk.dtu.game.core.solver.BruteForce.BruteForceAlgorithm.isValidSudoku;
import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.AlgorithmX.*;
import dk.dtu.game.core.solver.BruteForce.BruteForceAlgorithm;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SolverTest {
    List<int[]> coverList;

    public SolverTest() {
        coverList = new ArrayList<>();
        int[] coverRow1 = {1, 1, 0, 1};
        int[] coverRow2 = {0, 0, 1, 1};
        int[] coverRow3 = {0, 0, 1, 1};
        int[] coverRow4 = {1, 1, 0, 0};

        coverList.add(coverRow1);
        coverList.add(coverRow2);
        coverList.add(coverRow3);
        coverList.add(coverRow4);
    }

    @BeforeEach
    void setUp() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("Test if a column is correctly covered")
    void TestIsColumnCovered() {

        DancingLinks dl = new DancingLinks(coverList);
        printMatrix(dl);
        ColumnNode c = (ColumnNode) dl.header.right;

        c.cover();
        System.out.println("Covered column: " + c.name);
        System.out.println("After covering:");
        printMatrix(dl);
        assertTrue(isColumnCovered(c));

        c.uncover();
        System.out.println("Uncovered column: " + c.name);
        System.out.println("After uncovering:");
        printMatrix(dl);
        assertFalse(isColumnCovered(c));
    }

    @Test
    @DisplayName("Test if a column is correctly covered")
    void testCover() {
        DancingLinks dl = new DancingLinks(coverList);
        ColumnNode c = (ColumnNode) dl.header.right;
        c.cover();
        assertTrue(isColumnCovered(c));
    }

    @Test
    @DisplayName("Test if a column is correctly uncovered")
    void testColumnUncoverAssertion() {
        DancingLinks dl = new DancingLinks(coverList);
        ColumnNode c = (ColumnNode) dl.header.right;
        c.cover();
        assertTrue(isColumnCovered(c));
        c.uncover();
        assertTrue(isColumnUncovered(c, coverList));
    }

    @Test
    @DisplayName("Test that exactCoverMatrix from empty board works as expected")
    void TestExactCoverMatrix() {
        int[][] board = new int[9][9];

        List<algorithmX.Placement> placements = new ArrayList<>();

        List<int[]> exactCoverBoard = algorithmX.createExactCoverFromBoard(board, placements);

        assertEquals(729, exactCoverBoard.size());
        assertEquals(324, exactCoverBoard.getFirst().length);

        for (int i = 0; i < exactCoverBoard.size(); i++) {
            int numsInRow = 0;
            for (int j = 0; j < exactCoverBoard.getFirst().length; j++) {
                if (exactCoverBoard.get(i)[j] == 1) {
                    numsInRow++;
                }
            }
            assertEquals(4, numsInRow); // all rows must have exactly 4 numbers, highlighting the 4
            // constraints
        }

        for (int i = 0; i < exactCoverBoard.getFirst().length; i++) {
            int numsInCol = 0;
            for (int[] nums : exactCoverBoard) {
                if (nums[i] == 1) {
                    numsInCol++;
                }
            }
            assertEquals(
                    board.length,
                    numsInCol); // as there are no placements, all columns must have 9 numbers,
            // highlighting the 9 possible numbers.
        }
    }

    @Test
    @DisplayName("Test dancing links are created as expected")
    void testDancingLinks() {
        int[][] board = new int[9][9];
        int size = board.length;
        int constraint = size * size * 4;
        List<algorithmX.Placement> placements = new ArrayList<>();
        List<int[]> exactCoverBoard = algorithmX.createExactCoverFromBoard(board, placements);
        DancingLinks dl = new DancingLinks(exactCoverBoard);
        ColumnNode header = dl.header;
        // all columNodes must have size equal to the number of numbers in the board
        for (Node node = header.right; node != header; node = node.right) {
            ColumnNode columnNode = (ColumnNode) node;
            assertEquals(9, columnNode.size);
        }
        // there must columns equal to the size of the constraint:
        int totalColumns = 0;
        for (Node node = header.right; node != header; node = node.right) {
            totalColumns++;
        }
        assertEquals(constraint, totalColumns);

        // The number of rows must then be 4*size*size*size
        int totalNodes = 0;
        for (Node node = header.right; node != header; node = node.right) {
            ColumnNode columnNode = (ColumnNode) node;
            totalNodes += columnNode.size;
        }
        assertEquals(4 * size * size * size, totalNodes);
    }

    public static void printMatrix(DancingLinks dl) {
        ColumnNode header = dl.header;
        ColumnNode columnNode = (ColumnNode) header.right;

        // Traverse all columns from the header
        while (columnNode != header) {
            System.out.println("Column " + columnNode.name + " size: " + columnNode.size);
            Node rowNode = columnNode.down;

            // Traverse all rows in the current column
            while (rowNode != columnNode) {
                // Print details about each node in the row for the current column
                System.out.print("Row " + getRowIndex(rowNode) + " -> ");
                Node rightNode = rowNode.right;

                // Traverse all nodes in this row (right direction from the current column's node)
                while (rightNode != rowNode) {
                    System.out.print(rightNode.column.name + " ");
                    rightNode = rightNode.right;
                }

                System.out.println(); // New line for each row
                rowNode = rowNode.down;
            }
            columnNode = (ColumnNode) columnNode.right;
        }
    }

    // Utility function to check if a column is correctly covered
    public static boolean isColumnCovered(ColumnNode column) {
        // Check that the column's left and right links bypass this column
        if (column.left.right != column.right || column.right.left != column.left) {
            return false;
        }

        // Ensure no node in this column is accessible from other parts of the matrix
        for (Node node = column.down; node != column; node = node.down) {
            for (Node rightNode = node.right; rightNode != node; rightNode = rightNode.right) {
                if (rightNode.column == column) {
                    // If any node still points back to this column, it's not fully covered
                    return false;
                }
            }
        }

        return true;
    }

    // Utility function to check if a column is correctly uncovered
    public static boolean isColumnUncovered(ColumnNode column, List<int[]> matrix) {
        // Verify column links are restored
        if (column.left.right != column || column.right.left != column) {
            return false;
        }

        // Check if all nodes are restored in the column as per the matrix definition
        int currentRow = 0;
        for (Node node = column.down; node != column; node = node.down, currentRow++) {
            if (matrix.get(node.rowIndex)[Integer.parseInt(column.name)] != 1) {
                return false; // The node exists where matrix indicates there should be no node
            }
        }

        return true;
    }

    private static int getRowIndex(Node node) {
        return node.rowIndex; // Directly return the stored row index
    }

    @Test
    @DisplayName("CreateXSudoku correctly builds a playable sudokuBoard")
    void testCreateXSudoku() throws Exception {
        Board board = new Board(3, 3);
        algorithmX.createXSudoku(board);
        assertTrue(isValidSudoku(board.getGameBoard()));
    }

    @Test
    @DisplayName("CreateXSudoku creates a sudoku with a unique solution")
    void testUniqueXSudoku() throws Exception {
        Board board = new Board(3, 3);
        algorithmX.createXSudoku(board);
        assertEquals(1, BruteForceAlgorithm.checkUniqueSolution(board.getGameBoard()));
    }

    @Test
    @DisplayName("Test if solveExistingBoard works")
    void testSolveExistingBoard() throws Exception {
        Board board = new Board(3, 3);
        algorithmX.createXSudoku(board);
        int[][] solvedBoard = algorithmX.solveExistingBoard(board);

        assertArrayEquals(algorithmX.getSolutionBoard(), solvedBoard);
    }
}
