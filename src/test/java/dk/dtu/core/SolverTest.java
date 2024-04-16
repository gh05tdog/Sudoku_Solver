package dk.dtu.core;

import dk.dtu.game.solver.solverAlgorithm;
import dk.dtu.game.solver.ColumnNode;
import dk.dtu.game.solver.Node;
import dk.dtu.game.solver.dancingLinks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class SolverTest {
    int[][] matrix = solverAlgorithm.createExactCoverMatrix(2, 2); // For a 4x4 Sudoku grid
    dancingLinks dl = new dancingLinks(matrix);
    ColumnNode header = dl.header;

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
    @DisplayName("Test column cover and uncover")
    void testColumnCoverAndUncover() {
        ColumnNode temp = (ColumnNode) header.right;
        while (temp != header) {
            int sizeBefore = temp.size;
            temp.cover();

            // Test if all nodes in the column are not accessible from other columns
            for (Node i = temp.down; i != temp; i = i.down) {
                for (Node j = i.right; j != i; j = j.right) {
                    assert j.column.size != sizeBefore : "Node should not be accessible from other columns";
                }
            }

            temp.uncover();

            // Check if the column is correctly restored
            assert temp.size == sizeBefore : "Column size should be restored after uncovering";
            temp = (ColumnNode) temp.right;
        }
        System.out.println("Column cover and uncover is correct.");
    }

    @Test
    @DisplayName("Test row removal and reinsertion")
    void testRowRemovalAndReinsertion() {
        Node temp = header.right;
        while (temp != header) {
            Node downNode = temp.down;
            while (downNode != temp) {
                downNode.removeRow();
                assert downNode.left.right == downNode.right : "Row should be removed correctly";
                downNode.reinsertRow();
                assert downNode.left.right == downNode : "Row should be reinserted correctly";
                downNode = downNode.down;
            }
            temp = temp.right;
        }
        System.out.println("Row removal and reinsertion is correct.");
    }



}
