/* (C)2024 */
package dk.dtu.game.core.solver.algorithmx;

import java.io.Serializable;

public class ColumnNode extends Node implements Serializable {
    private int size;
    private final String name;

    public ColumnNode(String name) {
        super();
        this.size = 0;
        this.name = name;
        this.setColumn(this);
    }

    public void cover() {

        getLeft().setRight(getRight());
        getRight().setLeft(getLeft());

        for (Node row = this.getDown(); row != this; row = row.getDown()) {
            for (Node node = row.getRight(); node != row; node = node.getRight()) {
                node.removeNode();
            }
        }
    }

    public void uncover() {
        for (Node row = this.getUp(); row != this; row = row.getUp()) {
            for (Node node = row.getLeft(); node != row; node = node.getLeft()) {
                node.reinsertNode();
            }
        }

        // Reconnect this column to the header list
        this.getLeft().setRight(this);
        this.getRight().setLeft(this);
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
