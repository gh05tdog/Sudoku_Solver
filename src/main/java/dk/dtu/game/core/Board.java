/* (C)2024 */
package dk.dtu.game.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Board {

    private static final Logger logger = LoggerFactory.getLogger(Board.class);

    private final int n;
    private final int k;

    private final int size;

    private int[][] gameBoard;

    private int[][] initialBoard;

    private int[][] solvedBoard;

    public void setBoard(int[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.arraycopy(arr[i], 0, this.gameBoard[i], 0, arr[i].length);
        }
    }

    public int[][] getGameBoard() {
        return gameBoard;
    }

    public void clearInitialBoard() {
        this.initialBoard = new int[size][size];
    }

    public boolean equalsSolvedBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gameBoard[i][j] != solvedBoard[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class BoardNotCreatable extends Exception {
        public BoardNotCreatable(String errorMessage) {
            super(errorMessage);
        }
    }

    public Board(int n, int k) throws BoardNotCreatable {
        this.n = n;
        this.k = k;
        this.size = n * k;

        if (!boardIsPossible(k, n)) {
            logger.error("This board is not possible to create");
            throw new BoardNotCreatable("This board is not possible to create");
        }
        this.gameBoard = new int[n * k][n * k];
        this.initialBoard = new int[n * k][n * k];
        // Initialize the board with zeros
        fillZeros(gameBoard);
        fillZeros(initialBoard);
    }

    public boolean boardIsPossible(int k, int n) {
        return (k * n) <= (n * n);
    }

    public void setNumber(int x, int y, int num) {
        gameBoard[x][y] = num;
    }

    public boolean validPlace(int x, int y, int num) {
        // Check rows and check square
        logger.info("Checking if {} can be placed at {}, {}", num, x, y);
        return contains(getRow(x), num)
                && contains(getColumn(y), num)
                && !squareContains(getSquare(x, y), num);
    }

    public int[] getRow(int rowIndex) {
        int[] row = new int[size];
        System.arraycopy(gameBoard[rowIndex], 0, row, 0, size);
        return row;
    }

    public int[] getColumn(int colIndex) {
        int[] column = new int[size];
        for (int rowIndex = 0; rowIndex < size; rowIndex++) {
            column[rowIndex] = gameBoard[rowIndex][colIndex];
        }
        return column;
    }

    public boolean contains(int[] arr, int num) {
        for (int j : arr) {
            if (j == num) {
                return false;
            }
        }
        return true;
    }

    public boolean squareContains(int[][] square, int num) {
        for (int[] integers : square) {
            for (int anInt : integers) {
                if (anInt == num) {
                    logger.info("Found {} in the array", num);
                    return true;
                }
            }
        }
        return false;
    }

    public void fillZeros(int[][] board) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }
    }

    public int[][] getSquare(int x, int y) {
        int[][] square = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(gameBoard[(x / n) * n + i], (y / n) * n, square[i], 0, n);
        }
        return square;
    }

    public int getDimensions() {
        return n * k;
    }

    public void clear() {
        this.gameBoard = new int[size][size];
        int numRows = n * k;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numRows; j++) {
                gameBoard[i][j] = 0;
            }
        }
    }

    public int getNumber(int x, int y) {
        return gameBoard[x][y];
    }

    public void setGameBoard(int[][] tempBoard) {
        for (int i = 0; i < tempBoard.length; i++) {
            System.arraycopy(tempBoard[i], 0, this.gameBoard[i], 0, tempBoard[i].length);
        }
    }

    public void setInitialBoard(int[][] tempBoard) {
        for (int i = 0; i < tempBoard.length; i++) {
            System.arraycopy(tempBoard[i], 0, this.initialBoard[i], 0, tempBoard[i].length);
        }
    }

    public int[][] getInitialBoard() {
        return initialBoard;
    }

    public void setInitialNumber(int x, int y, int num) {
        initialBoard[x][y] = num;
    }

    public int getInitialNumber(int x, int y) {
        return initialBoard[x][y];
    }

    public int getN() {
        return n;
    }

    public void setSolvedBoard(int[][] solvedBoard) {
        this.solvedBoard = solvedBoard;
    }

    public int[][] getSolvedBoard() {
        return solvedBoard;
    }
}
