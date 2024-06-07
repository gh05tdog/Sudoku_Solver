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
        long starttime = System.currentTimeMillis();
        int[][] sudokuBoard = solveExistingBoard(board);
        solvedBoard = deepSetSolutionBoard(sudokuBoard);
        board.setSolvedBoard(solvedBoard);
        removeXNumbers(sudokuBoard);
        board.setInitialBoard(sudokuBoard);
        board.setBoard(sudokuBoard);
        long endtime = System.currentTimeMillis();
        System.out.println("Time taken to solve: " + (endtime - starttime));
    }

    public static int[][] solveExistingBoard(Board board) {
        int[][] arr = board.getGameBoard();
        arraySize = arr.length;
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(arr, placements);
        DancingLinks dl = new DancingLinks(xBoard);
        ColumnNode header = dl.getHeader();

        Solution.clear(); // Clear the solution list before each run

        if (algorithmXSolver(header)) {
            arr = convertSolutionToBoard(Solution, placements);
        }
        return arr;
    }

    public static List<int[]> createExactCoverFromBoard(int[][] board, List<Placement> placements) {

        List<int[]> coverList = new ArrayList<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                int num = board[i][j];
                List<Integer> nums;
                if (num == 0) {
                    nums = SolverAlgorithm.getPossiblePlacements(board, i, j, (int) Math.sqrt(board.length));
                } else {
                    nums = Collections.singletonList(num);
                }
                for (int n : nums) {
                    int[] cover = new int[4];
                    setCoverRow(coverList, i, j, n, cover, board.length, (int) Math.sqrt(board.length));
                    placements.add(new Placement(i, j, n));
                }
            }
        }

        return coverList;
    }

    private static void setCoverRow(
            List<int[]> coverList, int i, int j, int n, int[] cover, int boardSize, int subGridSize) {
        cover[0] = i * boardSize + j; // Cell constraint
        cover[1] = boardSize * boardSize + i * boardSize + n - 1; // Row constraint
        cover[2] = 2 * boardSize * boardSize + j * boardSize + n - 1; // Column constraint
        int subGridID = (i / subGridSize) * subGridSize + (j / subGridSize);
        cover[3] = 3 * boardSize * boardSize + subGridID * boardSize + n - 1; // Sub-grid constraint
        coverList.add(cover);
    }

    public static void removeXNumbers(int[][] arr) {
        long maxTime = 0;
        Solution.clear();

        int numRemoved = 0;
        long startTime;
        long endTime = 0;
        int maxRemoved = SolverAlgorithm.setNumsRemoved(arr);

        int[] randRow = fisherYatesShuffle(arr.length);
        int[] randCol = fisherYatesShuffle(arr.length);

        int x = 0;
        int y = 0;

        while (numRemoved < maxRemoved && y < arr.length) {
            startTime = System.nanoTime();
            if (arr[randRow[x]][randCol[y]] != 0) {

                int tempNumber = arr[randRow[x]][randCol[y]];
                arr[randRow[x]][randCol[y]] = 0;

                if (checkUniqueSolution(arr) == 1) {
                    endTime = System.nanoTime();
                    numRemoved++;
                    randRow = fisherYatesShuffle(arr.length);
                    randCol = fisherYatesShuffle(arr.length);
                    x = 0;
                    y = 0;

                    maxTime = ((maxTime * (numRemoved - 1) + (endTime - startTime)) / numRemoved);

                    System.out.println((endTime - startTime));

                    if (endTime - startTime > maxTime * 10) {
                        System.out.println("Time exceeded");
                        System.out.println("Time taken: " + (endTime - startTime));
                        System.out.println("Start time: " + startTime);
                        System.out.println("End time: " + endTime);
                        System.out.println("Limit: " + maxTime * 20);
                        break;
                    }
                } else {
                    arr[randRow[x]][randCol[y]] = tempNumber; // Restore the number if removing it doesn't lead to a unique solution.
                    x++;
                    if (x == arr.length) {
                        x = 0;
                        y++;
                    }
                }
            }
            x ++;
            if (x == arr.length) {
                x = 0;
                y++;
            }
        }
    }

    public static int checkUniqueSolution(int[][] board) {
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(board, placements);
        DancingLinks dl = new DancingLinks(xBoard);
        return countSolutions(dl.getHeader(), 0);
    }

    private static int countSolutions(ColumnNode header, int count) {
        if (header.getRight() == header) {
            return count + 1; // Found a solution
        }
        if (count > 1) {
            return count; // Early exit if more than one solution is found
        }

        ColumnNode c = chooseHeuristicColumn(header);
        c.cover();

        for (Node r = c.getDown(); r != c; r = r.getDown()) {
            for (Node j = r.getRight(); j != r; j = j.getRight()) {
                j.getColumn().cover();
            }

            count = countSolutions(header, count);
            if (count > 1) return count; // Early exit

            for (Node j = r.getLeft(); j != r; j = j.getLeft()) {
                j.getColumn().uncover();
            }
        }

        c.uncover();
        return count;
    }

    public static boolean algorithmXSolver(ColumnNode header) {
        if (header.getRight() == header) {
            return true; // Return true indicating the solution was found
        }

        ColumnNode c = chooseHeuristicColumn(header);

        c.cover();

        // Optionally, display the state of the linked list or affected columns here

        for (Node r = c.getDown(); r != c; r = r.getDown()) {
            selectRow(r);
            for (Node j = r.getRight(); j != r; j = j.getRight()) {
                j.getColumn().cover();
            }

            if (algorithmXSolver(header)) {
                return true; // Return immediately if solution was found
            }

            for (Node j = r.getLeft(); j != r; j = j.getLeft()) {
                j.getColumn().uncover();
            }
            deselectRow(r);
        }

        c.uncover();

        return false; // Return false as no solution was found in this path
    }

    public static ColumnNode chooseHeuristicColumn(ColumnNode header) {
        ColumnNode c = (ColumnNode) header.getRight();
        List<ColumnNode> columns = new ArrayList<>();
        int minSize = c.getSize();
        for (ColumnNode temp = (ColumnNode) header.getRight();
             temp != header;
             temp = (ColumnNode) temp.getRight()) {
            if (temp.getSize() < minSize) {
                minSize = temp.getSize();
                columns.clear();
                columns.add(temp);
            } else if (temp.getSize() == minSize) {
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
            Placement placement = placements.get(node.getRowIndex());
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

    public static int[] fisherYatesShuffle(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }
}
