package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import dk.dtu.game.core.solver.algorithmx.ColumnNode;
import dk.dtu.game.core.solver.algorithmx.DancingLinks;
import dk.dtu.game.core.solver.algorithmx.Node;
import dk.dtu.game.core.solver.bruteforce.BruteForceAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SolverTest {
    List<int[]> coverList;

    public SolverTest() {
        int [][] arr = {{3, 1, 2, 4},
                        {4, 0, 3, 1},
                        {1, 0, 4, 2},
                        {2, 4, 1, 3},
                       };
        coverList = AlgorithmXSolver.createExactCoverFromBoard(arr, new ArrayList<>());
    }

    @Test
    @DisplayName("Test if a column is correctly covered")
    void TestIsColumnCovered() {

        DancingLinks dl = new DancingLinks(coverList);

        ColumnNode c = (ColumnNode) dl.getHeader().getRight();

        c.cover();
        System.out.println("Covered column: " + c.getName());
        System.out.println("After covering:");

        assertTrue(isColumnCovered(c));

        c.uncover();
        System.out.println("Uncovered column: " + c.getName());
        System.out.println("After uncovering:");

        assertFalse(isColumnCovered(c));
    }

    @Test
    @DisplayName("Test if a column is correctly covered")
    void testCover() {
        DancingLinks dl = new DancingLinks(coverList);
        ColumnNode c = (ColumnNode) dl.getHeader().getRight();
        c.cover();

        assertTrue(isColumnCovered(c));
    }

    @Test
    @DisplayName("Test if a column is correctly uncovered")
    void testColumnUncoverAssertion() {
        DancingLinks dl = new DancingLinks(coverList);
        ColumnNode c = (ColumnNode) dl.getHeader().getRight();
        c.cover();

        assertTrue(isColumnCovered(c));
        c.uncover();
        assertTrue(isColumnUncovered(c, coverList));
    }

    @Test
    @DisplayName("Test that exactCoverMatrix from empty board works as expected")
    void TestExactCoverMatrix() {
        int[][] board = new int[9][9];

        List<AlgorithmXSolver.Placement> placements = new ArrayList<>();

        List<int[]> exactCoverBoard = AlgorithmXSolver.createExactCoverFromBoard(board, placements);

        assertEquals(729, exactCoverBoard.size());
        assertEquals(4, exactCoverBoard.getFirst().length);

        boolean isRightLength = true;
        for (int[] ints : exactCoverBoard) {
            if (ints.length != 4) {
                isRightLength = false;
                break;
            }

        }
            assertTrue(isRightLength); // all rows must have exactly 4 numbers, highlighting the 4
            // constraints


        for (int i = 0; i < exactCoverBoard.getFirst().length; i++) {
            int numsInCol = 0;
            for (int[] nums : exactCoverBoard) {
                if (nums[0] == i) {
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
        int[][] board = new int[4][4];
        int size = board.length;
        int constraint = size * size * 4;
        List<AlgorithmXSolver.Placement> placements = new ArrayList<>();
        List<int[]> exactCoverBoard = AlgorithmXSolver.createExactCoverFromBoard(board, placements);
        DancingLinks dl = new DancingLinks(exactCoverBoard);
        ColumnNode header = dl.getHeader();
        // all columNodes must have size equal to the number of numbers in the board
        for (Node node = header.getRight(); node != header; node = node.getRight()) {
            ColumnNode columnNode = (ColumnNode) node;
            assertEquals(board.length, columnNode.getSize());
        }
        // there must columns equal to the size of the constraint:
        int totalColumns = 0;
        for (Node node = header.getRight(); node != header; node = node.getRight()) {
            totalColumns++;
        }
        assertEquals(constraint, totalColumns);

        // The number of rows must then be 4*size*size*size
        int totalNodes = 0;
        for (Node node = header.getRight(); node != header; node = node.getRight()) {
            ColumnNode columnNode = (ColumnNode) node;
            totalNodes += columnNode.getSize();
        }
        assertEquals(4 * size * size * size, totalNodes);
    }

    public static void printMatrix(DancingLinks dl) {
        ColumnNode header = dl.getHeader();
        ColumnNode columnNode = (ColumnNode) header.getRight();

        // Traverse all columns from the header
        while (columnNode != header) {
            System.out.println("Column " + columnNode.getName() + " size: " + columnNode.getSize());
            Node rowNode = columnNode.getDown();

            // Traverse all rows in the current column
            while (rowNode != columnNode) {
                // Print details about each node in the row for the current column
                System.out.print("Row " + getRowIndex(rowNode) + " -> ");
                Node rightNode = rowNode.getRight();

                // Traverse all nodes in this row (right direction from the current column's node)
                while (rightNode != rowNode) {
                    System.out.print(rightNode.getColumn().getName() + " ");
                    rightNode = rightNode.getRight();
                }

                System.out.println(); // New line for each row
                rowNode = rowNode.getDown();
            }
            columnNode = (ColumnNode) columnNode.getRight();
        }
    }

    // Utility function to check if a column is correctly covered
    public static boolean isColumnCovered(ColumnNode column) {
        // Check that the column's left and right links bypass this column
        if (column.getLeft().getRight() != column.getRight() || column.getRight().getLeft() != column.getLeft()) {
            return false;
        }

        // Ensure no node in this column is accessible from other parts of the matrix
        for (Node node = column.getDown(); node != column; node = node.getDown()) {
            for (Node rightNode = node.getRight(); rightNode != node; rightNode = rightNode.getRight()) {
                if (rightNode.getColumn() == column) {
                    System.out.println("Node " + getRowIndex(rightNode) + " still points to column " + column.getName());
                    System.out.println("Node " + column.getName());

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
        if (column.getLeft().getRight() != column || column.getRight().getLeft() != column) {
            return false;
        }

        // Check if all nodes are restored in the column as per the matrix definition
        int currentRow = 0;
        for (Node node = column.getDown(); node != column; node = node.getDown(), currentRow++) {
            if (matrix.get(node.getRowIndex())[Integer.parseInt(column.getName())] != matrix.get(currentRow)[Integer.parseInt(column.getName())]) {
                System.out.println("Node " + getRowIndex(node) + " should not exist in column " + column.getName());
                return false; // The node exists where matrix indicates there should be no node
            }
        }

        return true;
    }

    private static int getRowIndex(Node node) {
        return node.getRowIndex(); // Directly return the stored row index
    }

    @Test
    @DisplayName("CreateXSudoku correctly builds a playable sudokuBoard")
    void testCreateXSudoku() throws Exception {
        Config.setDifficulty("easy");
        Board board = new Board(3, 3);
        AlgorithmXSolver.createXSudoku(board);
        assertTrue(BruteForceAlgorithm.isValidSudoku(board.getGameBoard()));
    }

    @Test
    @DisplayName("Test if solveExistingBoard works")
    void testSolveExistingBoard() throws Exception {
        Board board = new Board(3, 3);
        AlgorithmXSolver.createXSudoku(board);
        int[][] solvedBoard = AlgorithmXSolver.solveExistingBoard(board);

        assertArrayEquals(AlgorithmXSolver.getSolutionBoard(), solvedBoard);
    }
}