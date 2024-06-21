package dk.dtu.game.core.solver.algorithmx;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.SolverAlgorithm;


import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class AlgorithmXSolver {
    private static final List<Node> Solution = new ArrayList<>();
    private static int arraySize;

    static int[][] solvedBoard;

    private static final Random rand = new Random();

    public record Placement(int row, int col, int value) {}

    public static void createXSudoku(Board board) { // Create a playable sudoku game with a unique solution
        int[][] sudokuBoard = solveExistingBoard(board); // fill out the board with a solution
        board.setSolvedBoard(solvedBoard); // send the solved board to board for easy storage
        removeXNumbers(sudokuBoard);
        board.setInitialBoard(sudokuBoard); // send the playable board to board for easy storage
        board.setBoard(sudokuBoard); // set the board to the playable board
    }

    public static int[][] solveExistingBoard(Board board) {
        int[][] arr = board.getInitialBoard(); // takes the board loaded into Board as input
        arraySize = arr.length;
        List<Placement> placements = new ArrayList<>(); // create list to store found placements
        List<int[]> xBoard = createExactCoverFromBoard(arr, placements); // create exact cover matrix
        DancingLinks dl = new DancingLinks(xBoard); // Nodes are created and doubly linked based on the exact cover matrix
        ColumnNode header = dl.getHeader(); // A header node is defined to keep track of the columns

        Solution.clear(); // Clear the solution list before each run

        try (ForkJoinPool pool = new ForkJoinPool()) { // forkJoin pool task is a build in method which allows for parallel processing
            AlgorithmXTask task = new AlgorithmXTask(header, Solution, true); // create a task through ForkJoinPool to solve the exact cover matrix
            if (Boolean.TRUE.equals(pool.invoke(task))) { // if a solved board is found the task is invoked, and the board is converted and returned
                arr = convertSolutionToBoard(Solution, placements);
            }
        }
        solvedBoard = deepSetSolutionBoard(arr); // set the solved board as a global variable for easy access
        board.setSolvedBoard(solvedBoard); // send the solved board to board for easy storage
        return arr;
    }

    public static List<int[]> createExactCoverFromBoard(int[][] board, List<Placement> placements) {
        List<int[]> coverList = new ArrayList<>(); // create a list to store all possible number placements as constraints

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) { // run through each cell in the given board
                int num = board[i][j];
                List<Integer> nums;
                if (num == 0) { // if the cell is empty, find all possible placements
                    nums =
                            SolverAlgorithm.getPossiblePlacements(
                                    board, i, j, (int) Math.sqrt(board.length));
                } else {
                    nums = Collections.singletonList(num); // if the cell is not empty, only one placement is possible
                }
                for (int n : nums) { // for each possible placement, create a constraint
                    int[] cover = new int[4]; // constraint array is sparse. It contains the cell, row, column and subgrid constraint
                    setCoverRow(coverList, i, j, n, cover, board.length, (int) Math.sqrt(board.length)); // calculate the constraints
                    placements.add(new Placement(i, j, n)); // add the placement to the list of placements
                }
            }
        }

        return coverList;
    }

    private static void setCoverRow(
            List<int[]> coverList, int i, int j, int n, int[] cover, int boardSize, int subGridSize) { // calculate the constraints
        cover[0] = i * boardSize + j; // Cell constraint
        cover[1] = boardSize * boardSize + i * boardSize + n - 1; // Row constraint
        cover[2] = 2 * boardSize * boardSize + j * boardSize + n - 1; // Column constraint
        int subGridID = (i / subGridSize) * subGridSize + (j / subGridSize);
        cover[3] = 3 * boardSize * boardSize + subGridID * boardSize + n - 1; // Sub-grid constraint
        coverList.add(cover);

    }

    public static void removeXNumbers(int[][] arr) {
        Solution.clear(); // Clear the solution list before each run

        int numRemoved = 0;
        int maxRemoved = SolverAlgorithm.setNumsRemoved(arr); // Set the number of numbers to remove, based on the difficulty input
        int size = arr.length;
        int[] shuffleIndices = fisherYatesShuffle(size * size); // Shuffle all cell indices

        for (int i = 0; i < size * size && numRemoved < maxRemoved; i++) {
            int row = shuffleIndices[i] / size;
            int col = shuffleIndices[i] % size; // run through the board in a random order

            if (arr[row][col] != 0) {
                int tempNumber = arr[row][col];
                arr[row][col] = 0; // if a number is not 0, remove it and store as a temporary value

                if (checkUniqueSolution(arr) == 1) { // check to ensure only 1 solution is possible for the new board
                    numRemoved++;
                } else {
                    arr[row][col] = tempNumber; // Restore the number if removing it doesn't lead to a unique solution.
                }
            }
        }
    }

    public static int checkUniqueSolution(int[][] board) {
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(board, placements);
        DancingLinks dl = new DancingLinks(xBoard); // initialize the dancing links again to solve
        return countSolutions(dl.getHeader(), 0);
    }

    private static int countSolutions(ColumnNode header, int count) {
        if (header.getRight() == header) { // recursively checked, if the header is the only node left, a solution is found
            return count + 1; // Found a solution
        }
        if (count > 1) {
            return count; // Early exit if more than one solution is found
        }

        ColumnNode c = chooseHeuristicColumn(header); // MRV heuristic is used to choose the column which contains the fewest nodes
        c.cover(); // cover the column as well as all rows which contain a constraint the column

        for (Node r = c.getDown(); r != c; r = r.getDown()) {
            for (Node j = r.getRight(); j != r; j = j.getRight()) {
                j.getColumn().cover();
            }  // cover all columns as well as all rows which contain a constraint the column

            count = countSolutions(header, count); // recursively check for solutions
            if (count > 1) return count; // Early exit

            for (Node j = r.getLeft(); j != r; j = j.getLeft()) {
                j.getColumn().uncover();
            } // uncover all columns as well as all rows which contain a constraint the column
        }

        c.uncover(); // uncover the original column
        return count;
    }

    public static ColumnNode chooseHeuristicColumn(ColumnNode header) {
        ColumnNode c = (ColumnNode) header.getRight();
        List<ColumnNode> columns = new ArrayList<>();
        int minSize = c.getSize();
        for (ColumnNode temp = (ColumnNode) header.getRight();
                temp != header;
                temp = (ColumnNode) temp.getRight()) {
            if (temp.getSize() < minSize) { // check the number of 1's in each column, to find the column with the fewest 1's
                minSize = temp.getSize();
                columns.clear();
                columns.add(temp); // remove the previous MRV column and add the new one
            } else if (temp.getSize() == minSize) {
                columns.add(temp); // if multiple columns have the same number of 1's, add them all
            }
        }
        return columns.get(rand.nextInt(columns.size())); // if multiple columns are found, choose one at random
    }

    public static void selectRow(Node row) {
        Solution.add(row);
    } // add a row to the solution list

    public static void deselectRow(Node row) {
        Solution.remove(row);
    } // remove a row from the solution list

    public static int[][] convertSolutionToBoard(List<Node> solution, List<Placement> placements) {
        int[][] board = new int[arraySize][arraySize];

        for (Node node : solution) { // convert the solution to a board
            Placement placement = placements.get(node.getRowIndex()); // get the placement from the list of placements
            board[placement.row][placement.col] = placement.value; // place the value in the correct cell
        }

        return board;
    }

    public static int[][] deepSetSolutionBoard(int[][] board) {
        return SolverAlgorithm.deepCopyBoard(board);
    } // deep copy the board to ensure it is not changed

    public static int[] fisherYatesShuffle(int n) { // Fisher-Yates shuffle algorithm
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i; // create an array of n elements
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1); // randomly shuffle the array
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    public static int[][] getSolutionBoard() {
        return solvedBoard;
    }
}