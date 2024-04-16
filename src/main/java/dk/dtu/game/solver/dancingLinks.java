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
            last = columnNodes[i];
        }
        last.right = header; // Close the circular list for column headers
        header.left = last;

        // Setting up nodes for each 1 in the matrix and linking them
        for (int[] ints : matrix) {
            Node rowHeader = null; // Start of the row
            Node lastNodeInRow = null; // Last node added in the row
            for (int col = 0; col < matrix[0].length; col++) {
                if (ints[col] == 1) {
                    Node newNode = new Node();
                    if (lastNodeInRow == null) {
                        rowHeader = newNode; // First node in the row
                    } else {
                        newNode.left = lastNodeInRow;
                        lastNodeInRow.right = newNode;
                    }
                    lastNodeInRow = newNode; // Update last node in the row

                    // Linking vertically
                    newNode.column = columnNodes[col];
                    newNode.down = columnNodes[col].down;
                    newNode.up = columnNodes[col];
                    columnNodes[col].down.up = newNode;
                    columnNodes[col].down = newNode;
                    columnNodes[col].size++;
                }
            }
            // Closing the circular linkage for the row
            if (rowHeader != null && lastNodeInRow != null) {
                lastNodeInRow.right = rowHeader;
                rowHeader.left = lastNodeInRow;
            }
        }
    }
}