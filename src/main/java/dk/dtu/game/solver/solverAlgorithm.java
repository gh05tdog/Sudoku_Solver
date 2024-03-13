package dk.dtu.game.solver;

import dk.dtu.game.core.Board;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.sqrt;

public class solverAlgorithm {
    public int [][] solve(int [][] gameBoard) {
        runSolver(gameBoard);
        printBoard(gameBoard);
        return gameBoard;
    }

    public static int[][] convertTo2DArray(ArrayList<ArrayList<Integer>> gameBoard) {
        int[][] board = new int[gameBoard.size()][gameBoard.size()];
        for (int i = 0; i < gameBoard.size(); i++) {
            for (int j = 0; j < gameBoard.size(); j++) {
                board[i][j] = gameBoard.get(i).get(j);
            }
        }
        return board;
    }

    private static ArrayList<ArrayList<Integer>> convertToArrayList(int[][] board) {
        ArrayList<ArrayList<Integer>> gameBoard = new ArrayList<>();
        for (int[] integers : board) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < board.length; j++) {
                row.add(integers[j]);
            }
            gameBoard.add(row);
        }
        return gameBoard;
    }

    static void runSolver(int[][] board) {
        long startTime = System.nanoTime();

        if (sudoku(board)) {
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("Time taken: " + duration / 1000 + " microseconds");
        } else {
            System.out.println("No solution exists");
        }
    }

    static boolean sudoku(int[][] board) {
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

    public static void printBoard(int [][] gameBoard) {
        System.out.println("Solved board: ");
        for (int[] row : gameBoard) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
        System.out.println("End of solved board");
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

    public static void fillBoard(Board board) throws Exception {
        int size = board.getDimensions();
        int[][] arr = new int[size][size];

        if (sudoku(arr)) {
            board.setBoard(arr);
        } else {
            System.out.println("No solution exists");
        }

    }

    public static void removeNumsRecursive(Board board) throws Exception {
        int[][] tempBoard = deepCopy(board.getBoard());
        int[][] initialBoard;
        int numRemoved = 0;
        while (numRemoved < 200) {
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
    public static int [][] deepCopy (int [][] arr) {
        int [][] copy = new int [arr.length][arr.length];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                copy[i][j] = arr[i][j];
            }
        }
        return copy;
    }
}
