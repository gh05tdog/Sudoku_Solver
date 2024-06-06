/* (C)2024 */
package dk.dtu.game.core.solver.algorithmx;

import java.util.List;

public class DancingLinks {
    private final ColumnNode header;

    public DancingLinks(List<int[]> matrix) {
        if (matrix == null || matrix.isEmpty() || matrix.getFirst().length == 0) {
            throw new IllegalArgumentException("Matrix must not be null or empty");
        }

        header = new ColumnNode("header");


        ColumnNode[] columnNodes = new ColumnNode[matrix.getFirst().length];

        columnNodes = setColumnNodes(columnNodes);

        // Setting up the column nodes and linking them into a circular list

        // Setting up nodes for each 1 in the matrix and linking them
        for (int row = 0; row < matrix.size(); row++) {
            Node firstNodeInRow = null;
            Node lastNodeInRow = null;

            for (int col = 0; col < matrix.getFirst().length; col++) {
                if (matrix.get(row)[col] == 1) {
                    Node newNode =
                            new Node(row); // Assuming Node constructor sets column and row indexes
                    // correctly
                    newNode.setColumn(columnNodes[col]); // Linking node to its column

                    // Link nodes horizontally
                    if (firstNodeInRow == null) {
                        firstNodeInRow = newNode; // This is the first node in this row
                    } else {
                        lastNodeInRow.setRight(newNode);
                        newNode.setLeft(lastNodeInRow); // Link new node back to the last node
                    }
                    lastNodeInRow = newNode; // Update this node to be the last in the row

                    // Vertical linking within the column
                    verticalLinkNodes(columnNodes[col], newNode);

                    columnNodes[col].incrementSize();
                }
            }

            // Close the horizontal row loop
            if (firstNodeInRow != null) {
                lastNodeInRow.setRight(firstNodeInRow); // Last node links back to the first
                firstNodeInRow.setLeft(lastNodeInRow); // First node links back to the last
            }
        }
        linkColumnNodes(columnNodes); // circularly link the columnNodes
    }

    public void linkColumnNodes (ColumnNode [] columnNodes) {
        for (ColumnNode colNode : columnNodes) {
            if (colNode.getUp() == colNode) {
                colNode.setDown(colNode);
            }
        }
    }

    public void verticalLinkNodes (ColumnNode colNode, Node node) {
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

    public ColumnNode [] setColumnNodes (ColumnNode [] columnNodes) {
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
