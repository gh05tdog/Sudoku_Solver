package dk.dtu.game.core.solver.algorithmx;

public class Node {
    
    private Node left;
    private Node right;
    private Node up;
    private Node down;

    private final int rowIndex;

    private ColumnNode column;

    public Node(int rowIndex) { // Constructor for Node
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
        up.down = down; // Set the down node of the up node to the down node
        down.up = up; // Set the up node of the down node to the up node

        // Decrement column size
        column.decrementSize();
    }

    public void reinsertNode() {
        // Reinsert the node into the horizontal list
        this.left.right = this; // Set the right node of the left node to the node
        this.right.left = this; // Set the left node of the right node to the node

        // Reinsert the node into the vertical list
        this.up.down = this; // Set the down node of the up node to the node
        this.down.up = this; // Set the up node of the down node to the node

        // Increase the column size by 1 since this node is being reinserted
        this.column.incrementSize();
    }

    // Getter and setter methods for private fields
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