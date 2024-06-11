package dk.dtu.game.core.solver.heuristicsolver;

import dk.dtu.game.core.Board;

public class HeuristicSolver {
    public static void main(String[] args) throws Board.BoardNotCreatable {
        Board board = new Board(3, 3);
        int [][][] placementsBoard = createSetFromBoard(board);
        printPossiblePlacements(placementsBoard);


    }


    public static int[][][] createSetFromBoard(Board board) {
        int[][] smallBoard = new int[9][9];
        board.setInitialBoard(smallBoard);
        int subGrid = board.getN();
        int gridSize = board.getGameBoard().length / subGrid;
        int boardLength = subGrid*gridSize;
        int numCount = subGrid*subGrid;
        int[][][] possiblePlacements = new int[boardLength][boardLength][numCount+1];

        int [][] initialBoard = board.getInitialBoard();

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
                    // initialize the board with 1's in all 3'rd possible placements. When a not 0-number is found
                    // remove that number from row/col/subgrid
                }

            }
        }


        return possiblePlacements;
    }

    public static void removePossiblePlacements (int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid*gridSize;

        for (int i = 0; i < boardLength; i++) {
            arr[row][i][val] = 0;
            arr[i][col][val] = 0;
        }

        for (int i = (row/subgrid)*subgrid; i < subgrid + (row/subgrid)*subgrid; i++) {
            for (int j = (col/subgrid)*subgrid; j < subgrid + (col/subgrid)*subgrid; j++) {
                arr[i][j][val] = 0;
            }
        }
    }

    public static void printPossiblePlacements(int [][][] placementsBoard) {
        for (int[][] integers : placementsBoard) {
            for (int[] anInt : integers) {
                int placementCount = 0;
                for (int l = 1; l < anInt.length; l++) {
                    if (anInt[l] == 1) {
                        placementCount++;
                    }
                }
                System.out.print(placementCount + "  ");
            }
            System.out.println();
        }
    }

    public boolean fillBoard(int [][][] arr) {
     return true;
    }



}