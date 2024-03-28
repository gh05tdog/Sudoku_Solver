package dk.dtu.game.solver;

import dk.dtu.game.core.Board;

import java.util.*;

import static java.lang.Math.sqrt;

public class solverAlgorithm {

    public static void createSudoku(Board board) throws Exception {
        fillBoard(board);
        removeNumsRecursive(board);
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

            for (int c = 1; c <= board.length; c++) {
                if (checkBoard(board, row, col, c, (int) sqrt(board.length))) {
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

    static boolean checkBoard(int[][] board, int row, int col, int c, int constant) {
        boolean legal_row_col = true;
        boolean legal_square = true;
        for (int p = 0; p < board.length; p++) {
            if (board[row][p] == c || board[p][col] == c) {
                legal_row_col = false;
                break;
            }
        }
        for (int p = (row / constant) * constant; p < (row / constant) * constant + constant; p++) {
            for (int q = (col / constant) * constant; q < (col / constant) * constant + constant; q++) {
                if (board[p][q] == c) {
                    legal_square = false;
                    break;
                }
            }
            if (!legal_square) break;
        }

        return legal_row_col && legal_square;
    }

    public static int[] pickCell(int[][] arr) {
        ArrayList<int[]> possibleCells = new ArrayList<>();
        int lowestPossibleValue = Integer.MAX_VALUE;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                if (arr[i][j] == 0) {
                    int possibleValues = 0;
                    for (int k = 1; k <= arr.length; k++) {
                        if (checkBoard(arr, i, j, k, (int) sqrt(arr.length))) {
                            possibleValues++;
                        }
                    }
                    if (possibleValues < lowestPossibleValue) {
                        lowestPossibleValue = possibleValues;
                        possibleCells.clear();
                        possibleCells.add(new int[]{i, j});
                    } else if (possibleValues == lowestPossibleValue) {
                        possibleCells.add(new int[]{i, j});
                    }
                }
            }
        }
        if (possibleCells.isEmpty()) {
            return null;
        } else {
            Random random = new Random();

            return possibleCells.get(random.nextInt(possibleCells.size()));
        }
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

    public static void fillBoard(Board board) {
        int size = board.getDimensions();
        int[][] arr = new int[size][size];

        if (sudoku(arr)) {
            board.setBoard(arr);
        } else {
            System.out.println("No solution exists");
        }

    }

    public static void removeNumsRecursive(Board board) {
        int[][] tempBoard = deepCopy(board.getBoard());
        int[][] initialBoard;
        int numRemoved = 0;
        while (numRemoved < 150) {
            int possibleSols = 0;
            int randRow = (int) (Math.random() * board.getDimensions());
            int randCol = (int) (Math.random() * board.getDimensions());

            int tempNumber = tempBoard[randRow][randCol];
            tempBoard[randRow][randCol] = 0;

            for (int i = 1; i <= board.getDimensions(); i++) {
                initialBoard = deepCopy(tempBoard);
                if (checkBoard(initialBoard, randRow, randCol, i, (int) sqrt(board.getDimensions()))) {
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
            board.setBoard(tempBoard);
        }
    }

    public static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][arr.length];
        for (int i = 0; i < arr.length; i++) {
            System.arraycopy(arr[i], 0, copy[i], 0, arr.length);
        }
        return copy;
    }

    public static boolean isValidSudoku(int[][] board) {
        int size = board.length; // Assuming square board
        int n = (int) Math.sqrt(size); // Calculate the size of subgrids

        // Check for row and column uniqueness
        for (int i = 0; i < size; i++) {
            if (isUnique(board[i]) || isUnique(getColumn(board, i))) {
                return false;
            }
        }

        // Check subgrids for uniqueness
        for (int row = 0; row < size; row += n) {
            for (int col = 0; col < size; col += n) {
                if (!isSubgridUnique(board, row, col, n)) {
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
    private static boolean isSubgridUnique(int[][] board, int startRow, int startCol, int n) {
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
            return null;

        }
    }

}
