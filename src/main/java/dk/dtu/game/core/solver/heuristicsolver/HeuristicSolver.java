package dk.dtu.game.core.solver.heuristicsolver;

import dk.dtu.game.core.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeuristicSolver {
    static Random random = new Random();

    public static void main(String[] args) throws Board.BoardNotCreatable {
        Long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Long start = System.currentTimeMillis();
            Board board = new Board(3, 3);
            int[][][] possiblePlacements = createSetFromBoard(board);
            fillBoard(possiblePlacements, 3, 3, true);
            Long end = System.currentTimeMillis();
            System.out.println("Time taken: " + (end - start) + "ms");
        }
        Long endTime = System.currentTimeMillis();
        Long averageTime = (endTime - startTime) / 10;
        System.out.println("Average time: " + averageTime + "ms");


        Long startTime1 = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Long start = System.currentTimeMillis();
            Board board = new Board(3, 3);
            int[][][] possiblePlacements = createSetFromBoard(board);
            fillBoard(possiblePlacements, 3, 3, false);
            Long end = System.currentTimeMillis();
            System.out.println("Time taken: " + (end - start) + "ms");
        }
        Long endTime1 = System.currentTimeMillis();
        Long averageTime1 = (endTime1 - startTime1) / 10;
        System.out.println("Average time: " + averageTime1 + "ms");

    }

    public static int[][][] createSetFromBoard(Board board) {
        int subGrid = board.getN();
        int gridSize = board.getGameBoard().length / subGrid;
        int[][] smallBoard = new int[subGrid*gridSize][subGrid*gridSize];
        board.setInitialBoard(smallBoard);
        int boardLength = subGrid * gridSize;
        int numCount = subGrid * subGrid;
        int[][][] possiblePlacements = new int[boardLength][boardLength][numCount + 1];

        int[][] initialBoard = board.getInitialBoard();

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                possiblePlacements[i][j][0] = initialBoard[i][j];

                for (int l = 1; l <= numCount; l++) {
                    possiblePlacements[i][j][l] = 1;
                }
            }
        }

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (initialBoard[i][j] != 0) {
                    removePossiblePlacements(possiblePlacements, i, j, initialBoard[i][j], subGrid, gridSize);
                }
            }
        }

        return possiblePlacements;
    }

    public static void removePossiblePlacements(int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;

        for (int i = 0; i < boardLength; i++) {
            arr[row][i][val] = 0;
            arr[i][col][val] = 0;
        }

        for (int i = (row / subgrid) * subgrid; i < subgrid + (row / subgrid) * subgrid; i++) {
            for (int j = (col / subgrid) * subgrid; j < subgrid + (col / subgrid) * subgrid; j++) {
                arr[i][j][val] = 0;
            }
        }
    }

    public static boolean fillBoard(int[][][] arr, int subgrid, int gridSize, boolean iterate) {
        int boardLength = subgrid * gridSize;
        int totalCells = boardLength * boardLength;

        int[] shuffleIndices = fisherYatesShuffle(totalCells);

        if(iterate) {
            boolean isChanged = true;

            // Initial pass to make obvious placements
            while (isChanged) {
                isChanged = false;
                for (int i = 0; i < totalCells; i++) {
                    int row = shuffleIndices[i] / boardLength;
                    int col = shuffleIndices[i] % boardLength;

                    if (placementCount(arr[row][col]) == 1 && arr[row][col][0] == 0) {
                        int value = placeableValue(arr[row][col]);
                        arr[row][col][0] = value;
                        removePossiblePlacements(arr, row, col, value, subgrid, gridSize);
                        isChanged = true;
                    }
                }
            }
        }
        if (isFullyFilled(arr)) {
            return true;
        }

        // Recursive backtracking
        for (int i = 0; i < totalCells; i++) {
            int row = shuffleIndices[i] / boardLength;
            int col = shuffleIndices[i] % boardLength;

            if (arr[row][col][0] == 0 && placementCount(arr[row][col]) > 1) {
                List<Integer> possibleValues = getPossibleValues(arr[row][col]);
                for (int value : possibleValues) {
                    int[][][] arrCopy = copyBoard(arr); // Make a copy of the board
                    arrCopy[row][col][0] = value; // Place the value in the copy
                    removePossiblePlacements(arrCopy, row, col, value, subgrid, gridSize);

                    if (fillBoard(arrCopy, subgrid, gridSize, iterate)) {
                        // If the recursive call returns true, copy the solution back to the original array
                        for (int r = 0; r < arr.length; r++) {
                            for (int c = 0; c < arr[r].length; c++) {
                                arr[r][c] = arrCopy[r][c].clone();
                            }
                        }
                        return true;
                    }
                }
                return false; // If no placement leads to a solution, return false for backtracking
            }
        }

        return false; // Return false if the board cannot be filled
    }

    private static int[][][] copyBoard(int[][][] arr) {
        int[][][] copy = new int[arr.length][arr[0].length][arr[0][0].length];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                for (int k = 0; k < arr[i][j].length; k++) {
                    copy[i][j][k] = arr[i][j][k];
                }
            }
        }
        return copy;
    }

    private static List<Integer> getPossibleValues(int[] cell) {
        List<Integer> possibleValues = new ArrayList<>();
        for (int i = 1; i < cell.length; i++) {
            if (cell[i] == 1) {
                possibleValues.add(i);
            }
        }
        return possibleValues;
    }

    private static boolean isFullyFilled(int[][][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j][0] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int placeableValue(int[] list) {
        for (int i = 1; i < list.length; i++) {
            if (list[i] == 1) {
                return i;
            }
        }
        return 0;
    }

    public static int placementCount(int[] list) {
        int count = 0;
        for (int i = 1; i < list.length; i++) {
            if (list[i] == 1) {
                count++;
            }
        }
        return count;
    }

    public static int[] fisherYatesShuffle(int size) {
        int[] shuffleIndices = new int[size];
        for (int i = 0; i < size; i++) {
            shuffleIndices[i] = i;
        }
        for (int i = size - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = shuffleIndices[index];
            shuffleIndices[index] = shuffleIndices[i];
            shuffleIndices[i] = temp;
        }
        return shuffleIndices;
    }

    public static void printBoard(int[][][] arr) {
        for (int[][] row : arr) {
            for (int[] cell : row) {
                if (cell[0] > 9) {
                    System.out.print("" + cell[0] + " ");
                } else {
                    System.out.print(" " + cell[0] + " ");
                }
            }
            System.out.println();
        }
    }
}