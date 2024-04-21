package dk.dtu.game.solver;

public class ColumnNode extends Node {
    public int size;
    public String name;

    public ColumnNode(String name) {
        super();
        this.size = 0;
        this.name = name;
        this.column = this;
    }

    public void cover() {

        left.right = right;
        right.left = left;

        // If the loop starts now, there should be nodes to cover
        for (Node row = this.down; row != this; row = row.down) {
            for (Node node = row.right; node != row; node = node.right) {
                node.removeNode();
                node.column.size--;
            }
        }
    }

    public void uncover() {
        for (Node row = this.up; row != this; row = row.up) {
            for (Node node = row.left; node != row; node = node.left) {
                node.reinsertNode();
                node.column.size++;
            }
        }

        // Reconnect this column to the header list
        this.left.right = this;
        this.right.left = this;
    }
}