package dk.dtu.game.solver;

public class Node {
    public Node left;
    public Node right;
    public Node up;
    public Node down;

    public ColumnNode column;

    public Node() {
        this.left = this;
        this.right = this;
        this.up = this;
        this.down = this;
    }

    public void removeRow() {
        this.left.right = this.right;
        this.right.left = this.left;
    }

    public void reinsertRow() {
        this.left.right = this;
        this.right.left = this;
    }
}

