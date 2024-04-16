package dk.dtu.game.solver;

public class dancingLinks {
    public ColumnNode header;

    public dancingLinks(int[][] matrix) {
        header = new ColumnNode("header");
        ColumnNode[] columnNodes = new ColumnNode[matrix[0].length];

        // Setting up the column nodes and linking them into a circular list
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

        // Setting up nodes for each 1 in the matrix and linking them
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[0].length; col++) {
                if (matrix[row][col] == 1) {
                    Node newNode = new Node();
                    newNode.column = columnNodes[col];

                    // Correctly linking the first node or appending new nodes in the column
                    if (columnNodes[col].down == columnNodes[col]) { // First node in the column
                        columnNodes[col].down = newNode;
                        columnNodes[col].up = newNode;
                        newNode.down = columnNodes[col];
                        newNode.up = columnNodes[col];
                    } else {
                        newNode.down = columnNodes[col].down;
                        newNode.up = columnNodes[col];
                        columnNodes[col].down.up = newNode;
                        columnNodes[col].down = newNode;
                    }

                    columnNodes[col].size++;
                }
            }
        }

        // Ensure each column node is circularly linked if no nodes were added
        for (ColumnNode colNode : columnNodes) {
            if (colNode.up == colNode) {  // No nodes have been added
                // These lines are redundant since up and down are already set to colNode in initialization
                colNode.up = colNode;  // Ensure circular linkage
                colNode.down = colNode;
            }
        }
    }
}