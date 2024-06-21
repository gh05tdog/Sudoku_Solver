/* (C)2024 */
package dk.dtu.game.core.solver.algorithmx;

import java.io.Serializable;

public class ColumnNode extends Node implements Serializable {
    private int size;
    private final String name;

    public ColumnNode(String name) { // Constructor for ColumnNode
        super();
        this.size = 0;
        this.name = name;
        this.setColumn(this);
    }

    public void cover() { // Method for covering a column

        getLeft().setRight(getRight()); // set left column to be equal to column
        getRight().setLeft(getLeft());  // set right column to be equal to column

        for (Node row = this.getDown(); row != this; row = row.getDown()) {
            for (Node node = row.getRight(); node != row; node = node.getRight()) {
                node.removeNode(); // remove the node from the column
            }
        }
    }

    public void uncover() {
        for (Node row = this.getUp(); row != this; row = row.getUp()) {
            for (Node node = row.getLeft(); node != row; node = node.getLeft()) {
                node.reinsertNode(); // reinsert the node into the column
            }
        }

        // Reconnect this column to the header list
        this.getLeft().setRight(this); // the right of the left column is set to the column
        this.getRight().setLeft(this); // the left of the right column is set to the column
    }

    public int getSize() {
        return size;
    }

    public void incrementSize() {
        size++;
    }

    public void decrementSize() {
        size--;
    }

    public String getName() {
        return name;
    }

    public void setSize(int val) {
        size = val;
    }
}
