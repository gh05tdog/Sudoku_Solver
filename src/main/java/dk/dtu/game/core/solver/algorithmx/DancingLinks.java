package dk.dtu.game.core.solver.algorithmx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DancingLinks {
    private final ColumnNode header;

    public DancingLinks(List<int[]> matrix) {
        if (matrix == null || matrix.isEmpty() || matrix.getFirst().length != 4) {
            throw new IllegalArgumentException("Matrix must not be null or empty and must have 4 columns");
        } // Check if the matrix is empty or null

        // Determine the number of columns needed based on the maximum index in the sparse matrix
        int numCols = setNumCols(matrix); // Set the number of columns based on the matrix

        header = new ColumnNode("header"); // Create a header node to keep track of the columns
        ColumnNode[] columnNodes = new ColumnNode[numCols]; // Create an array of column nodes

        columnNodes = setColumnNodes(columnNodes); // Set up the column nodes

        // Setting up nodes for each constraint in the sparse matrix and linking them
        for (int row = 0; row < matrix.size(); row++) {
            int[] constraints = matrix.get(row);
            Node firstNodeInRow = null; // Create a node to keep track of the first node in the row
            Node lastNodeInRow = null; // Create a node to keep track of the last node in the row

            for (int col : constraints) { // Run through each constraint in the row
                Node newNode = new Node(row); // Create a new node for the constraint
                newNode.setColumn(columnNodes[col]); // Linking node to its column

                // Link nodes horizontally
                if (firstNodeInRow == null) {
                    firstNodeInRow = newNode; // Set the first node in the row
                } else {
                    lastNodeInRow.setRight(newNode); // Set the right node to the new node
                    newNode.setLeft(lastNodeInRow); // Set the left node to the last node
                }
                lastNodeInRow = newNode;

                // Vertical linking within the column
                verticalLinkNodes(columnNodes[col], newNode); // Link the node vertically to the column

                columnNodes[col].incrementSize();
            }

            // Close the horizontal row loop
            if (firstNodeInRow != null) {
                lastNodeInRow.setRight(firstNodeInRow); // Set the right node of the last node to the first node
                firstNodeInRow.setLeft(lastNodeInRow); // Set the left node of the first node to the last node
            }
        }
        linkColumnNodes(columnNodes); // circularly link the columnNodes
    }

    public void linkColumnNodes(ColumnNode[] columnNodes) { // Circularly link the column nodes
        for (ColumnNode colNode : columnNodes) { // Run through each column node
            if (colNode.getUp() == colNode) { // If the column is empty
                colNode.setDown(colNode); // Set the down node to the column
            }
        }
    }

    public static int setNumCols(List<int[]> matrix) { // Set the number of columns based on the matrix
        Set<Integer> uniqueColumns = new HashSet<>(); // Create a set to store unique columns
        for (int[] row : matrix) { // Run through each row in the matrix
            for (int col : row) { // Run through each column in the row
                if (col >= 0) { // If the column is greater than or equal to 0
                    uniqueColumns.add(col); // Add the column to the set
                }
            }
        }
        return uniqueColumns.size(); // Return the number of unique columns
    }


    public void verticalLinkNodes(ColumnNode colNode, Node node) {
        if (colNode.getDown() == colNode) {  // Column is empty
            colNode.setDown(node); // Set the down node to the node
            node.setUp(colNode); // Set the up node to the column
            node.setDown(colNode); // Set the down node to the column
            colNode.setUp(node); // Set the up node to the node
        } else { // Column already has nodes
            node.setDown(colNode.getDown()); // Set the down node to the column's down node
            node.setUp(colNode); // Set the up node to the column
            colNode.getDown().setUp(node); // Set the down node of the column's down node to the node
            colNode.setDown(node); // Set the down node to the node
        }
    }

    public ColumnNode[] setColumnNodes(ColumnNode[] columnNodes) { // Set up the column nodes
        ColumnNode last = header; // Set the last node to the header
        for (int i = 0; i < columnNodes.length; i++) { // Run through each column node
            columnNodes[i] = new ColumnNode(Integer.toString(i)); // Create a new column node
            last.setRight(columnNodes[i]); // Set the right node to the column node
            columnNodes[i].setLeft(last);  // Set the left node to the last node
            // Initially, set up and down to point to itself
            columnNodes[i].setUp(columnNodes[i]); // Set the up node to the column node
            columnNodes[i].setDown(columnNodes[i]); // Set the down node to the column node
            last = columnNodes[i]; // Set the last node to the column node
        }
        last.setRight(header); // Close the circular list for column headers
        header.setLeft(last); // Close the circular list for column headers
        return columnNodes;
    }

    public ColumnNode getHeader() {
        return header;
    }
}