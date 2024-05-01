/* (C)2024 */
package dk.dtu.game.core.solver.algorithmx;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.SolverAlgorithm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AlgorithmXSolver {
    private static final List<Node> Solution = new ArrayList<>();
    private static int arraySize;

    static int[][] solvedBoard;

    private static final Random rand = new Random();

    public record Placement(int row, int col, int value) {}

    public static void createXSudoku(Board board) {
        int[][] sudokuBoard = solveExistingBoard(board);
        solvedBoard = deepSetSolutionBoard(sudokuBoard);
        removeXRecursive(sudokuBoard, arraySize * arraySize / 2);
        board.setInitialBoard(sudokuBoard);
        board.setBoard(sudokuBoard);
    }

    public static int[][] solveExistingBoard(Board board) {
        int[][] arr = board.getGameBoard();
        arraySize = arr.length;
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(arr, placements);
        DancingLinks dl = new DancingLinks(xBoard);
        ColumnNode header = dl.header;

        Solution.clear(); // Clear the solution list before each run

        if (algorithmXSolver(header)) {
            arr = convertSolutionToBoard(Solution, placements);
        }
        return arr;
    }

    public static List<int[]> createExactCoverFromBoard(int[][] board, List<Placement> placements) {

        List<int[]> coverList = new ArrayList<>();
        int constraints = board.length * board.length * 4;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                int num = board[i][j];
                List<Integer> nums;
                if (num == 0) {
                    nums = SolverAlgorithm.getPossiblePlacements(board, i, j);
                } else {
                    nums = Collections.singletonList(num);
                }
                for (int n : nums) {
                    int[] cover = new int[constraints];
                    setCoverRow(board, coverList, i, j, n, cover);
                    placements.add(new Placement(i, j, n));
                }
            }
        }

        return coverList;
    }

    private static void setCoverRow(
            int[][] board, List<int[]> coverList, int i, int j, int n, int[] cover) {
        cover[i * board.length + j] = 1;
        cover[board.length * board.length + i * board.length + n - 1] = 1;
        cover[2 * board.length * board.length + j * board.length + n - 1] = 1;
        int subSize = (int) Math.sqrt(board.length);
        int subGridID = (i / subSize) * subSize + (j / subSize);
        cover[3 * board.length * board.length + subGridID * board.length + n - 1] = 1;
        coverList.add(cover);
    }

    public static void removeXRecursive(int[][] arr, int maxRemoved) {
        Solution.clear();

        int numRemoved = 0;

        while (numRemoved < maxRemoved) {
            int randRow = rand.nextInt(arr.length);
            int randCol = rand.nextInt(arr.length);

            if (arr[randRow][randCol] == 0) {
                continue; // Skip already removed numbers.
            }

            int tempNumber = arr[randRow][randCol];
            arr[randRow][randCol] = 0;

            if (checkUniqueSolution(arr) == 1) {
                numRemoved++; // Only remove the number permanently if there's exactly one solution.
            } else {
                arr[randRow][randCol] =
                        tempNumber; // Restore the number if removing it doesn't lead to a unique
                // solution.
            }
        }
    }

    public static int checkUniqueSolution(int[][] board) {
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(board, placements);
        DancingLinks dl = new DancingLinks(xBoard);
        return countSolutions(dl.header, 0);
    }

    private static int countSolutions(ColumnNode header, int count) {
        if (header.right == header) {
            return count + 1; // Found a solution
        }
        if (count > 1) {
            return count; // Early exit if more than one solution is found
        }

        ColumnNode c = chooseHeuristicColumn(header);
        c.cover();

        for (Node r = c.down; r != c; r = r.down) {
            for (Node j = r.right; j != r; j = j.right) {
                j.column.cover();
            }

            count = countSolutions(header, count);
            if (count > 1) return count; // Early exit

            for (Node j = r.left; j != r; j = j.left) {
                j.column.uncover();
            }
        }

        c.uncover();
        return count;
    }

    public static boolean algorithmXSolver(ColumnNode header) {
        if (header.right == header) {
            return true; // Return true indicating the solution was found
        }

        ColumnNode c = chooseHeuristicColumn(header);

        c.cover();

        // Optionally, display the state of the linked list or affected columns here

        for (Node r = c.down; r != c; r = r.down) {
            selectRow(r);
            for (Node j = r.right; j != r; j = j.right) {
                j.column.cover();
            }

            if (algorithmXSolver(header)) {
                return true; // Return immediately if solution was found
            }

            for (Node j = r.left; j != r; j = j.left) {
                j.column.uncover();
            }
            deselectRow(r);
        }

        c.uncover();

        return false; // Return false as no solution was found in this path
    }

    public static ColumnNode chooseHeuristicColumn(ColumnNode header) {
        ColumnNode c = (ColumnNode) header.right;
        List<ColumnNode> columns = new ArrayList<>();
        int minSize = c.size;
        for (ColumnNode temp = (ColumnNode) header.right;
                temp != header;
                temp = (ColumnNode) temp.right) {
            if (temp.size < minSize) {
                minSize = temp.size;
                columns.clear();
                columns.add(temp);
            } else if (temp.size == minSize) {
                columns.add(temp);
            }
        }
        return columns.get(rand.nextInt(columns.size()));
    }

    private static void selectRow(Node row) {
        Solution.add(row);
    }

    private static void deselectRow(Node row) {
        Solution.remove(row);
    }

    public static int[][] convertSolutionToBoard(List<Node> solution, List<Placement> placements) {
        int[][] board = new int[arraySize][arraySize];

        for (Node node : solution) {
            Placement placement = placements.get(node.rowIndex);
            board[placement.row][placement.col] = placement.value;
        }

        return board;
    }

    public static int[][] deepSetSolutionBoard(int[][] board) {
        int[][] copy = new int[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board.length);
        }
        return copy;
    }

    public static int[][] getSolutionBoard() {
        return solvedBoard;
    }
}
