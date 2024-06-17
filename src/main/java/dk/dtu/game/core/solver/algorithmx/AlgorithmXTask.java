package dk.dtu.game.core.solver.algorithmx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class AlgorithmXTask extends RecursiveTask<Boolean> implements Serializable  {
    private final ColumnNode header;
    private final List<Node> solution;
    private final boolean isTopLevel;

    public AlgorithmXTask(ColumnNode header, List<Node> solution, boolean isTopLevel) {
        this.header = header;
        this.solution = solution;
        this.isTopLevel = isTopLevel;
    }

    @Override
    protected Boolean compute() {
        if (header.getRight() == header) {
            return true;
        }

        ColumnNode c = AlgorithmXSolver.chooseHeuristicColumn(header);
        c.cover();

        boolean result;

        for (Node r = c.getDown(); r != c; r = r.getDown()) {
            AlgorithmXSolver.selectRow(r);
            for (Node j = r.getRight(); j != r; j = j.getRight()) {
                j.getColumn().cover();
            }

            if (isTopLevel) {
                AlgorithmXTask task = new AlgorithmXTask(header, new ArrayList<>(solution), false);
                task.fork();
                result = task.join();
            } else {
                result = new AlgorithmXTask(header, solution, false).compute();
            }

            if (result) {
                return true;
            }

            for (Node j = r.getLeft(); j != r; j = j.getLeft()) {
                j.getColumn().uncover();
            }
            AlgorithmXSolver.deselectRow(r);
        }

        c.uncover();
        return false;
    }
}