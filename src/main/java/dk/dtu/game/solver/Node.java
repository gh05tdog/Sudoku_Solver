package dk.dtu.game.solver;

public class Node {
    public Node left;
    public Node right;
    public Node up;
    public Node down;

    public int rowIndex;

    public ColumnNode column;

    public Node(int rowIndex) {
        this.left = this;
        this.right = this;
        this.up = this;
        this.down = this;
        this.rowIndex = rowIndex;

    }

    public Node() {
        this.rowIndex = -1; // Default or invalid value
    }

    public void removeNode() {

        // Remove from vertical list of the column
        up.down = down;
        down.up = up;

        // Decrement column size
        column.size--;
    }

    public void reinsertNode () {
        // Reinsert the node into the horizontal list
        this.left.right = this;
        this.right.left = this;

        // Reinsert the node into the vertical list
        this.up.down = this;
        this.down.up = this;

        // Increase the column size by 1 since this node is being reinserted
        this.column.size++;
    }
}

