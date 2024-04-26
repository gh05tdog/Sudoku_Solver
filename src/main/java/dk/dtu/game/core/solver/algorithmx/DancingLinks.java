/* (C)2024 */
package dk.dtu.game.core.solver.algorithmx;

import java.util.List;

public class DancingLinks {
    public ColumnNode header;

    public DancingLinks(List<int[]> matrix) {
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
                    newNode.column = columnNodes[col]; // Linking node to its column

                    // Link nodes horizontally
                    if (firstNodeInRow == null) {
                        firstNodeInRow = newNode; // This is the first node in this row
                    } else {
                        lastNodeInRow.right =
                                newNode; // Link the last node in the row to this new node
                        newNode.left = lastNodeInRow; // Link new node back to the last node
                    }
                    lastNodeInRow = newNode; // Update this node to be the last in the row

                    // Vertical linking within the column
                    if (columnNodes[col].down == columnNodes[col]) { // Column is empty
                        columnNodes[col].down = newNode;
                        newNode.up = columnNodes[col];
                        newNode.down = columnNodes[col];
                        columnNodes[col].up = newNode;
                    } else { // Column already has nodes
                        newNode.down = columnNodes[col].down;
                        newNode.up = columnNodes[col];
                        columnNodes[col].down.up = newNode;
                        columnNodes[col].down = newNode;
                    }

                    columnNodes[col].size++;
                }
            }

            // Close the horizontal row loop
            if (firstNodeInRow != null) {
                lastNodeInRow.right = firstNodeInRow; // Last node links back to the first
                firstNodeInRow.left = lastNodeInRow; // First node links back to the last
            }
        }

        // Ensure each column node is circularly linked if no nodes were added
        for (ColumnNode colNode : columnNodes) {
            if (colNode.up == colNode) {
                colNode.down = colNode;
            }
        }
    }

    public ColumnNode [] setColumnNodes (ColumnNode [] columnNodes) {
        ColumnNode last = header;
        for (int i = 0; i < columnNodes.length; i++) {
            columnNodes[i] = new ColumnNode(Integer.toString(i));
            last.right = columnNodes[i];
            columnNodes[i].left = last;
            // Initially, set up and down to point to itself
            columnNodes[i].up = columnNodes[i];
            columnNodes[i].down = columnNodes[i];
            last = columnNodes[i];
        }
        last.right = header; // Close the circular list for column headers
        header.left = last;
        return columnNodes;
    }
}
