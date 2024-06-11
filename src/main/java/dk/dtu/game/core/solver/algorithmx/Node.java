package dk.dtu.game.core.solver.algorithmx;

public class Node {
    private Node left;
    private Node right;
    private Node up;
    private Node down;

    private final int rowIndex;

    private ColumnNode column;

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
        column.decrementSize();
    }

    public void reinsertNode() {
        // Reinsert the node into the horizontal list
        this.left.right = this;
        this.right.left = this;

        // Reinsert the node into the vertical list
        this.up.down = this;
        this.down.up = this;

        // Increase the column size by 1 since this node is being reinserted
        this.column.incrementSize();
    }

    // Getter methods for private fields
    public Node getLeft() {return left;}

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Node getUp() {
        return up;
    }

    public void setUp(Node up) {
        this.up = up;
    }

    public Node getDown() {
        return down;
    }

    public void setDown(Node down) {
        this.down = down;
    }

    public ColumnNode getColumn() {
        return column;
    }

    public void setColumn(ColumnNode column) {
        this.column = column;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}