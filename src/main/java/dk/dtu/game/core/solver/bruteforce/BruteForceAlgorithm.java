/* (C)2024 */
package dk.dtu.game.core.solver.bruteforce;

import static java.lang.Math.sqrt;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.solver.SolverAlgorithm;
import java.util.*;
import java.util.logging.Logger;

public class BruteForceAlgorithm {

    static Random rand = new Random();
    static Logger logger = Logger.getLogger(BruteForceAlgorithm.class.getName());
    public static int[][] solvedBoard;

    public static int n;

    private BruteForceAlgorithm() {
        throw new IllegalStateException("Utility class");
    }

    public static void createSudoku(Board board) {
        n = board.getN();
        int[][] sudokuBoard = fillBoard(board);
        solvedBoard = deepCopy(sudokuBoard);
        sudokuBoard = removeNumsRecursive(sudokuBoard);
        board.setInitialBoard(sudokuBoard);
        board.setBoard(sudokuBoard);
        board.setSolvedBoard(solvedBoard);
    }

    public static boolean sudoku(int[][] board) {

        if (!isValidSudoku(board)) {
            return false;
        }

        if (emptyCellCount(board) > 0) {
            int[] chosenCells = pickCell(board);
            assert chosenCells != null;
            int row = chosenCells[0];
            int col = chosenCells[1];

            for (int c = 1; c <= n*n; c++) {
                if (SolverAlgorithm.checkBoard(board, row, col, c, n)) {

                    board[row][col] = c;
                    if (sudoku(board)) {
                        return true;
                    } else {
                        board[row][col] = 0;
                    }
                }
            }
            return false;
        } else {
            return true; // board is solved
        }
    }

    public static int[] pickCell(int[][] arr) {
        ArrayList<int[]> possibleCells = new ArrayList<>();
        int lowestPossibleValue = Integer.MAX_VALUE;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                if (arr[i][j] == 0) {
                    int possibleValues = 0;
                    for (int k = 1; k <= arr.length; k++) {
                        if (SolverAlgorithm.checkBoard(arr, i, j, k, n)) {
                            possibleValues++;
                        }
                    }
                    lowestPossibleValue =
                            getLowestPossibleValue(
                                    possibleValues, lowestPossibleValue, possibleCells, i, j);
                }
            }
        }
        return getPossibleCells(possibleCells);
    }

    private static int[] getPossibleCells(ArrayList<int[]> possibleCells) {
        if (possibleCells.isEmpty()) {
            return new int[0];
        } else {

            return possibleCells.get(rand.nextInt(possibleCells.size()));
        }
    }

    private static int getLowestPossibleValue(
            int possibleValues,
            int lowestPossibleValue,
            ArrayList<int[]> possibleCells,
            int i,
            int j) {
        if (possibleValues < lowestPossibleValue) {
            lowestPossibleValue = possibleValues;
            possibleCells.clear();
            possibleCells.add(new int[] {i, j});
        } else if (possibleValues == lowestPossibleValue) {
            possibleCells.add(new int[] {i, j});
        }
        return lowestPossibleValue;
    }

    public static int emptyCellCount(int[][] arr) {
        int emptyCells = 0;
        for (int[] integers : arr) {
            for (int j = 0; j < arr.length; j++) {
                if (integers[j] == 0) {
                    emptyCells++;
                }
            }
        }
        return emptyCells;
    }

    public static int[][] fillBoard(Board board) {
        int size = board.getDimensions();
        int[][] arr = new int[size][size];

        if (sudoku(arr)) {
            board.setBoard(arr);
        } else {
            logger.info("No solution exists");
        }
        return arr;
    }

    public static int[][] removeNumsRecursive(int[][] sudokuBoard) {
        int[][] tempBoard = deepCopy(sudokuBoard);
        int size = sudokuBoard.length;
        int[][] initialBoard;
        int numRemoved = 0;
        int maxNumRemoved = SolverAlgorithm.setNumsRemoved(sudokuBoard);


        while (numRemoved < maxNumRemoved) {
            int possibleSols = 0;
            int randRow = rand.nextInt(size);
            int randCol = rand.nextInt(size);

            int tempNumber = tempBoard[randRow][randCol];
            tempBoard[randRow][randCol] = 0;

            for (int i = 1; i <= size; i++) {
                initialBoard = deepCopy(tempBoard);
                if (SolverAlgorithm.checkBoard(
                        initialBoard, randRow, randCol, i, n)) {
                    initialBoard[randRow][randCol] = i;
                    if (sudoku(initialBoard)) {
                        possibleSols++;
                    }
                }
            }
            if (possibleSols != 1) {
                tempBoard[randRow][randCol] = tempNumber;
            } else {
                numRemoved++;
            }
        }
        return tempBoard;
    }

    public static int[][] deepCopy(int[][] arr) {
        return SolverAlgorithm.deepCopyBoard(arr);
    }

    public static boolean isValidSudoku(int[][] board) {
        int size = n; // Assuming square board; // Calculate the size of subGrids

        // Check for row and column uniqueness
        for (int i = 0; i < size; i++) {
            if (isUnique(board[i]) || isUnique(getColumn(board, i))) {
                return false;
            }
        }

        // Check subGrids for uniqueness
        for (int row = 0; row < size; row += n) {
            for (int col = 0; col < size; col += n) {
                if (!isSubgridUnique(board, row, col)) {
                    return false;
                }
            }
        }

        return true; // Passed all checks
    }

    // Helper method to check if all elements in an array are unique (excluding zero)
    private static boolean isUnique(int[] arr) {
        Set<Integer> seen = new HashSet<>();
        for (int num : arr) {
            if (num != 0) {
                if (seen.contains(num)) {
                    return true;
                }
                seen.add(num);
            }
        }
        return false;
    }

    // Helper method to get a column from a 2D array
    private static int[] getColumn(int[][] board, int colIndex) {
        return Arrays.stream(board).mapToInt(row -> row[colIndex]).toArray();
    }

    // Check if a subgrid (n by n) is unique
    private static boolean isSubgridUnique(int[][] board, int startRow, int startCol) {
        Set<Integer> seen = new HashSet<>();
        for (int row = startRow; row < startRow + n; row++) {
            for (int col = startCol; col < startCol + n; col++) {
                int num = board[row][col];
                if (num != 0) {
                    if (seen.contains(num)) {
                        return false;
                    }
                    seen.add(num);
                }
            }
        }
        return true;
    }

    public static int[][] getSolutionBoard(int[][] board) {
        int[][] copiedBoard = deepCopy(board);

        boolean solved = sudoku(copiedBoard);

        if (solved) {
            return copiedBoard;
        } else {
            return new int[0][0];
        }
    }

    public static int checkUniqueSolution(int[][] board) {
        simplifyBoard(board);
        return countSolutions(board, 0);
    }

    private static void simplifyBoard(int[][] board) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] == 0) {
                        List<Integer> possiblePlacements =
                                SolverAlgorithm.getPossiblePlacements(board, i, j, n);
                        if (possiblePlacements.size() == 1) {
                            board[i][j] = possiblePlacements.getFirst();
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    private static int countSolutions(int[][] board, int count) {
        if (count > 1) return count; // Early exit if more than one solution found
        int[] cell = findLeastConstrainingCell(board);
        if (cell == null) return count + 1; // Increment count when a solution is found

        int row = cell[0];
        int col = cell[1];
        List<Integer> possiblePlacements = SolverAlgorithm.getPossiblePlacements(board, row, col, n);

        for (int value : possiblePlacements) {
            board[row][col] = value;
            count = countSolutions(board, count);
            if (count > 1) return count; // Early exit
            board[row][col] = 0; // Undo placement
        }

        return count;
    }

    private static int[] findLeastConstrainingCell(int[][] board) {
        int minOptions = Integer.MAX_VALUE;
        int[] cell = null;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    int numOptions = SolverAlgorithm.getPossiblePlacements(board, i, j, n).size();
                    if (numOptions < minOptions) {
                        minOptions = numOptions;
                        cell = new int[] {i, j};
                    }
                }
            }
        }
        return cell;
    }

    public static int[][] getSolvedBoard() {
        return solvedBoard;
    }
}
