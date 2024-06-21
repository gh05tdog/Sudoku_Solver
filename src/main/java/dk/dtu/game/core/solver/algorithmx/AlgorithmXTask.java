package dk.dtu.game.core.solver.algorithmx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class AlgorithmXTask extends RecursiveTask<Boolean> {
    private final transient ColumnNode header;
    private final transient List<Node> solution;
    private final boolean isTopLevel;

    public AlgorithmXTask(ColumnNode header, List<Node> solution, boolean isTopLevel) {
        this.header = header;
        this.solution = solution;
        this.isTopLevel = isTopLevel;
    }

    @Override
    protected Boolean compute() { // Recursive method to solve the exact cover matrix
        if (header.getRight() == header) { // If the header is the only node left, the matrix is solved
            return true;
        }

        ColumnNode c = AlgorithmXSolver.chooseHeuristicColumn(header); // Choose the column with the least amount of nodes
        c.cover(); // Cover the column to avoid it being chosen again

        boolean result;

        for (Node r = c.getDown(); r != c; r = r.getDown()) { // Run through each row in the chosen column
            AlgorithmXSolver.selectRow(r); // add row to the solution
            for (Node j = r.getRight(); j != r; j = j.getRight()) {
                j.getColumn().cover(); // cover all columns in the row to avoid them being chosen again
            }

            if (isTopLevel) { // If the task is the top level task, fork a new task
                AlgorithmXTask task = new AlgorithmXTask(header, new ArrayList<>(solution), false);
                task.fork(); // Fork the task to allow for parallel processing
                result = task.join(); // Join the task to get the result
            } else {
                result = new AlgorithmXTask(header, solution, false).compute(); // If the task is not the top level task, compute the task
            }

            if (result) { // If a solution is found, return true
                return true;
            }

            for (Node j = r.getLeft(); j != r; j = j.getLeft()) { // If no solution is found, uncover the row and columns
                j.getColumn().uncover(); // uncover all columns as well as all rows which contain a constraint the column
            }
            AlgorithmXSolver.deselectRow(r); // remove the row from the solution
        }

        c.uncover(); // uncover the original column
        return false;
    }
}