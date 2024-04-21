package dk.dtu.core;

import dk.dtu.game.solver.solverAlgorithm;
import dk.dtu.game.solver.ColumnNode;
import dk.dtu.game.solver.Node;
import dk.dtu.game.solver.DancingLinks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class SolverTest {
    int[][] matrix = solverAlgorithm.createExactCoverMatrix(2, 2); // For a 4x4 Sudoku grid
    DancingLinks dl = new DancingLinks(matrix);
    ColumnNode header = dl.header;

    @Test
    @DisplayName("Test exact cover matrix creation")
    void testExactCoverMatrixCreation() {
        assert matrix.length == 64 : "Matrix should have 16 rows";
        assert matrix[0].length == 64 : "Matrix should have 16 columns";

        // Check each row has exactly 4 1's
        for (int[] row : matrix) {
            int sum = 0;
            for (int col : row) {
                sum += col;
            }
            assert sum == 4 : "Each row should have exactly 4 1's, one for each constraint";
        }

        // Check each column has exactly 4 1's
        for (int col = 0; col < matrix[0].length; col++) {
            int sum = 0;
            for (int[] row : matrix) {
                sum += row[col];
            }
            assert sum == 4 : "Each column should have exactly 4 1's, one for each constraint";
        }

        System.out.println("Exact cover matrix created successfully.");
    }



    @Test
    @DisplayName("Test column headers are linked correctly")
    void testColumnHeaders() {
        assert header.right != header : "Header should not point to itself initially";
        assert header.right.left == header : "Right node's left pointer should point back to header";
        System.out.println("Column headers linked correctly.");
    }

    @Test
    @DisplayName("Test row and column linkage")
    void testRowAndColumnLinkage() {
        Node temp = dl.header.right;
        while (temp != header) {
            Node downNode = temp.down;
            while (downNode != temp) {
                assert downNode.right.left == downNode : "Right and left pointers in row are not linked correctly";
                assert downNode.down.up == downNode : "Up and down pointers in column are not linked correctly";
                downNode = downNode.down;
            }
            temp = temp.right;
        }
        System.out.println("Row and column linkage is correct.");
    }


    @Test
    @DisplayName("Test circular linkage of column nodes")
    void testRightCircularLinkage() {
        ColumnNode temp = (ColumnNode) header.right;
        boolean foundHeader = false;

        assert(header.right != header) : "Header should not point to itself initially";

        // Assuming there is at least one column node apart from header.
        while (temp != header) { // Continue until we loop back to the header.
            temp = (ColumnNode) temp.right;
        }

        // If the loop exits, temp must be the header, confirming a circular list.
        assert temp == header : "Right pointer of last column should point to header";
    }


    @Test
    @DisplayName("Test column covering and uncovering")
    void testColumnCoveringAndUncovering() {
        ColumnNode temp = (ColumnNode) header.right;
        while (temp != header) {
            // First, check that all nodes can be reached before covering
            Node downNode = temp.down;
            while (downNode != temp) {
                assertSame(downNode.column, temp, "Node should be linked to its column before covering");
                downNode = downNode.down;
            }

            // Cover the column
            temp.cover();

            // Check that the column is no longer accessible from the header list
            assert temp.left.right == temp.right : "Column should be covered correctly";
            assert temp.right.left == temp.left : "Column should be covered correctly";

            // Check that nodes in the covered column are not accessible from other nodes in their rows
            downNode = temp.down;
            while (downNode != temp) {
                Node rightNode = downNode.right;
                while (rightNode != downNode) {
                    assertNotEquals(rightNode.column, temp, "Node's column should not be accessible after covering");
                    rightNode = rightNode.right;
                }
                downNode = downNode.down;
            }

            // Uncover the column
            temp.uncover();

            // Check the column is restored in the header list
            assert temp.left.right == temp : "Column should be uncovered correctly";
            assert temp.right.left == temp : "Column should be uncovered correctly";

            // Check that all nodes are correctly relinked vertically and horizontally
            downNode = temp.down;
            while (downNode != temp) {
                assertSame(downNode.column, temp, "Node should be linked back to its column after uncovering");
                Node rightNode = downNode.right;
                while (rightNode != downNode) {
                    assertEquals(rightNode.column.column, rightNode.column, "Node should be linked correctly in row after uncovering");
                    rightNode = rightNode.right;
                }
                downNode = downNode.down;
            }

            temp = (ColumnNode) temp.right;
        }
        System.out.println("Column covering and uncovering is correct.");
    }


    @Test
    @DisplayName("Test circular linkage of column nodes")
    void testCircularLinkage() {
        ColumnNode temp = (ColumnNode) header.right;
        while (temp != header) {
            assert temp.up.down == temp : "Up and down pointers in column are not circularly linked";
            assert temp.down.up == temp : "Up and down pointers in column are not circularly linked";
            temp = (ColumnNode) temp.right;
        }
        System.out.println("Circular linkage of column nodes is correct.");
    }

    @Test
    @DisplayName("Test row removal and reinsertion")
    void testCoverUncover () {
        // 1. Set up a simple Dancing Links matrix

        solverAlgorithm solve= new solverAlgorithm();
        int[][] matrix = {
                {1, 0, 1, 0},
                {0, 1, 1, 1},
                {1, 1, 0, 1},
                {0, 1, 1, 0}
        };
        DancingLinks dl = new DancingLinks(matrix);
        ColumnNode testColumn = (ColumnNode) dl.header.right; // Assume we test the first column after the header

        // 2. Print initial state
        System.out.println("Initial state:");
        solve.printMatrix(dl);

        // 3. Cover the column
        testColumn.cover();
        System.out.println("After covering column " + testColumn.name + ":");
        solve.printMatrix(dl);

        // 4. Check if the column is correctly covered
        if (solve.isColumnCovered(testColumn)) {
            System.out.println("Column " + testColumn.name + " is correctly covered.");
        } else {
            System.out.println("Error in covering column " + testColumn.name);
        }

        // 5. Uncover the column
        testColumn.uncover();
        System.out.println("After uncovering column " + testColumn.name + ":");
        solve.printMatrix(dl);

        // 6. Check if the column is correctly uncovered
        if (solve.isColumnUncovered(testColumn, matrix)) {
            System.out.println("Column " + testColumn.name + " is correctly uncovered.");
        } else {
            System.out.println("Error in uncovering column " + testColumn.name);
        }
    }



}
