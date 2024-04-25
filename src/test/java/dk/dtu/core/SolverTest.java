/* (C)2024 */
package dk.dtu.core;

import dk.dtu.game.core.solver.AlgorithmX.ColumnNode;
import dk.dtu.game.core.solver.AlgorithmX.DancingLinks;
import dk.dtu.game.core.solver.AlgorithmX.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SolverTest {

    @Test
    @DisplayName("Test if a column is correctly covered")
    void testIsColumnCovered() {
        List<int[]> coverList = new ArrayList<>();
        int[] coverRow1 = {1, 1, 0, 1};
        int[] coverRow2 = {0, 0, 1, 1};
        int[] coverRow3 = {0, 0, 1, 1};
        int[] coverRow4 = {1, 1, 0, 0};

        coverList.add(coverRow1);
        coverList.add(coverRow2);
        coverList.add(coverRow3);
        coverList.add(coverRow4);

        DancingLinks dl = new DancingLinks(coverList);
        printMatrix(dl);
        ColumnNode c = (ColumnNode) dl.header.right;

        c.cover();
        System.out.println("Covered column: " + c.name);
        System.out.println("After covering:");
        printMatrix(dl);

        c.uncover();
        System.out.println("Uncovered column: " + c.name);
        System.out.println("After uncovering:");
        printMatrix(dl);
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
    public static boolean isColumnUncovered(ColumnNode column, int[][] matrix) {
        // Verify column links are restored
        if (column.left.right != column || column.right.left != column) {
            return false;
        }

        // Check if all nodes are restored in the column as per the matrix definition
        int currentRow = 0;
        for (Node node = column.down; node != column; node = node.down, currentRow++) {
            if (matrix[getRowIndex(node)][Integer.parseInt(column.name)] != 1) {
                return false; // The node exists where matrix indicates there should be no node
            }
        }

        return true;
    }

    private static int getRowIndex(Node node) {
        return node.rowIndex; // Directly return the stored row index
    }
}
