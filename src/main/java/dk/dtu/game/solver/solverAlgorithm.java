package dk.dtu.game.solver;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.sqrt;

public class solverAlgorithm {

    public ArrayList<ArrayList<Integer>> solve(ArrayList<ArrayList<Integer>> gameboard) {
        gameboard = convertToArrayList(runSolver((convertTo2DArray(gameboard))));
        printboard(gameboard);
        return gameboard;
    }

    private int[][] convertTo2DArray(ArrayList<ArrayList<Integer>> gameboard) {
        int[][] board = new int[gameboard.size()][gameboard.size()];
        for (int i = 0; i < gameboard.size(); i++) {
            for (int j = 0; j < gameboard.size(); j++) {
                board[i][j] = gameboard.get(i).get(j);
            }
        }
        return board;
    }
    private static ArrayList<ArrayList<Integer>> convertToArrayList(int[][] board) {
        ArrayList<ArrayList<Integer>> gameboard = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < board.length; j++) {
                row.add(board[i][j]);
            }
            gameboard.add(row);
        }
        return gameboard;
    }

    static int [][] runSolver(int[][] board) {
        long startTime = System.nanoTime();

        int[] randCol = fisherYatesShuffle(new int[board.length]);
        int[] randRow = fisherYatesShuffle(new int[board.length]);

        if (sudoku(board, randRow, randCol)) {
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("Time taken: " + duration / 1000 + " microseconds");
        } else {
            System.out.println("No solution exists");
        }
        return board;
    }

    static boolean sudoku(int[][] board, int[] randRow, int[] randCol) {
        int constant = (int) sqrt(board.length);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                if (board[randRow[row]][randCol[col]] == 0) {
                    for (int c = 1; c <= board.length; c++) {
                        if (checkBoard(board, randRow[row], randCol[col], c, constant)) {
                            board[randRow[row]][randCol[col]] = c;
                            if (sudoku(board, randRow, randCol)) {
                                return true;
                            } else {
                                board[randRow[row]][randCol[col]] = 0;

                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true; // board is solved
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
    public static void printboard(ArrayList<ArrayList<Integer>> gameboard) {
        System.out.println("Solved board: ");
        for (ArrayList<Integer> row : gameboard) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
        System.out.println("End of solved board");
    }

    public static int [] fisherYatesShuffle(int[] arr) {
        for (int i = 0; i<arr.length; i++) {
            arr[i] = i;
        }

        for (int i = arr.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            int a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }
        return arr;
    }


}
