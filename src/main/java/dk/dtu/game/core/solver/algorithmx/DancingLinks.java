package dk.dtu.game.core.solver.algorithmx;

import java.util.List;

public class DancingLinks {
    private final ColumnNode header;

    public DancingLinks(List<int[]> matrix) {
        if (matrix == null || matrix.isEmpty() || matrix.getFirst().length != 4) {
            System.out.println(matrix.get(0).length);
            throw new IllegalArgumentException("Matrix must not be null or empty and must have 4 columns");
        }

        // Determine the number of columns needed based on the maximum index in the sparse matrix
        int numCols = setNumCols(matrix);

        header = new ColumnNode("header");
        ColumnNode[] columnNodes = new ColumnNode[numCols];

        columnNodes = setColumnNodes(columnNodes);

        // Setting up nodes for each constraint in the sparse matrix and linking them
        for (int row = 0; row < matrix.size(); row++) {
            int[] constraints = matrix.get(row);
            Node firstNodeInRow = null;
            Node lastNodeInRow = null;

            for (int col : constraints) {
                Node newNode = new Node(row);
                newNode.setColumn(columnNodes[col]); // Linking node to its column

                // Link nodes horizontally
                if (firstNodeInRow == null) {
                    firstNodeInRow = newNode;
                } else {
                    lastNodeInRow.setRight(newNode);
                    newNode.setLeft(lastNodeInRow);
                }
                lastNodeInRow = newNode;

                // Vertical linking within the column
                verticalLinkNodes(columnNodes[col], newNode);

                columnNodes[col].incrementSize();
            }

            // Close the horizontal row loop
            if (firstNodeInRow != null) {
                lastNodeInRow.setRight(firstNodeInRow);
                firstNodeInRow.setLeft(lastNodeInRow);
            }
        }
        linkColumnNodes(columnNodes); // circularly link the columnNodes
    }

    public void linkColumnNodes(ColumnNode[] columnNodes) {
        for (ColumnNode colNode : columnNodes) {
            if (colNode.getUp() == colNode) {
                colNode.setDown(colNode);
            }
        }
    }

    public static int setNumCols(List<int[]> matrix) {
       int numCols = 0;
        for (int[] row : matrix) {
            for (int col : row) {
                if (col >= numCols) {
                    numCols = col + 1;
                }
            }
        }
        return numCols;
    }

    public void verticalLinkNodes(ColumnNode colNode, Node node) {
        if (colNode.getDown() == colNode) { // Column is empty
            colNode.setDown(node);
            node.setUp(colNode);
            node.setDown(colNode);
            colNode.setUp(node);
        } else { // Column already has nodes
            node.setDown(colNode.getDown());
            node.setUp(colNode);
            colNode.getDown().setUp(node);
            colNode.setDown(node);
        }
    }

    public ColumnNode[] setColumnNodes(ColumnNode[] columnNodes) {
        ColumnNode last = header;
        for (int i = 0; i < columnNodes.length; i++) {
            columnNodes[i] = new ColumnNode(Integer.toString(i));
            last.setRight(columnNodes[i]);
            columnNodes[i].setLeft(last);
            // Initially, set up and down to point to itself
            columnNodes[i].setUp(columnNodes[i]);
            columnNodes[i].setDown(columnNodes[i]);
            last = columnNodes[i];
        }
        last.setRight(header); // Close the circular list for column headers
        header.setLeft(last);
        return columnNodes;
    }

    public ColumnNode getHeader() {
        return header;
    }
}