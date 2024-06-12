package dk.dtu.game.core.solver.heuristicsolver;

import dk.dtu.game.core.Board;

import java.util.*;

public class HeuristicSolver {
    static Random random = new Random();
    static int recursionCount;

    public static void main(String[] args) throws Board.BoardNotCreatable {

        testRuntime(2, true);
        testRuntime(2, false);
        testRuntime(3, true);
        testRuntime(3, false);
        testRuntime(4, true);
        testRuntime(4, false);
        testRuntime(5, true);
        testRuntime(5, false);
    }

    public static void testRuntime(int n, boolean iterate) throws Board.BoardNotCreatable {
        System.out.println("Size : " + n);
        long totalTime = 0L;
        int runs = 100;
        for (int i = 0; i < runs; i++) {
            Long start = System.currentTimeMillis();
            Board board = new Board(n, n);
            int[][][] possiblePlacements = createSetFromBoard(board);
            fillBoard(possiblePlacements, n, n, iterate, new HashSet<>(), new HashMap<>());
            Long end = System.currentTimeMillis();
            if (i > 0) {
                totalTime += end - start;
            }
        }
        long avgTime = totalTime / (runs - 1);
        System.out.println("Average time: " + avgTime);
        System.out.println("Average recursion count: " + recursionCount / (runs - 1));
        recursionCount = 0;
    }

    public static void createAndPrintBoard(int n, boolean iterate) throws Board.BoardNotCreatable {
        Board board = new Board(n, n);
        int[][][] possiblePlacements = createSetFromBoard(board);
        recursionCount = 0;
        fillBoard(possiblePlacements, n, n, iterate, new HashSet<>(), new HashMap<>());
        printBoard(possiblePlacements);
    }

    public static int[][][] createSetFromBoard(Board board) {
        int subGrid = board.getN();
        int gridSize = board.getGameBoard().length / subGrid;
        int[][] smallBoard = new int[subGrid * gridSize][subGrid * gridSize];
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

    private static boolean fillBoard(int[][][] arr, int subgrid, int gridSize, boolean iterate, Set<int[]> conflictSet, Map<String, Integer> conflictMap) {
        recursionCount++;
        int boardLength = subgrid * gridSize;

        if (iterate) {
            boolean isChanged = true;
            long startInitialPlacement = System.currentTimeMillis();

            // Initial pass to make obvious placements
            while (isChanged) {
                isChanged = false;
                for (int i = 0; i < boardLength; i++) {
                    for (int j = 0; j < boardLength; j++) {
                        if (placementCount(arr[i][j]) == 1 && arr[i][j][0] == 0) {
                            int value = placeableValue(arr[i][j]);
                            arr[i][j][0] = value;
                            removePossiblePlacements(arr, i, j, value, subgrid, gridSize);
                            isChanged = true;
                        }
                    }
                }
            }
            long endInitialPlacement = System.currentTimeMillis();
        }

        if (isFullyFilled(arr)) {
            return true;
        }

        long startMRVTime = System.currentTimeMillis();
        // Find the cell with the minimum remaining values (MRV) and highest degree
        int[] mrvCell = findMRVAndHighestDegreeCell(arr, subgrid, gridSize, conflictMap);
        long endMRVTime = System.currentTimeMillis();

        if (mrvCell == null) {
            return false;
        }

        int row = mrvCell[0];
        int col = mrvCell[1];

        long startLCVTime = System.currentTimeMillis();
        List<Integer> possibleValues = getLCV(arr, row, col, subgrid, gridSize);
        Collections.shuffle(possibleValues, random); // Shuffle possible values to introduce randomness
        long endLCVTime = System.currentTimeMillis();

        for (int value : possibleValues) {
            int[][][] arrCopy = copyBoard(arr); // Make a copy of the board
            arrCopy[row][col][0] = value; // Place the value in the copy
            removePossiblePlacements(arrCopy, row, col, value, subgrid, gridSize);

            // Perform forward checking
            long startForwardCheckTime = System.currentTimeMillis();
            boolean consistent = isConsistent(arrCopy, subgrid, gridSize);
            long endForwardCheckTime = System.currentTimeMillis();

            if (consistent && fillBoard(arrCopy, subgrid, gridSize, iterate, conflictSet, conflictMap)) {
                // If the recursive call returns true, copy the solution back to the original array
                for (int r = 0; r < arr.length; r++) {
                    for (int c = 0; c < arr[r].length; c++) {
                        arr[r][c] = arrCopy[r][c].clone();
                    }
                }
                return true;
            }
            conflictSet.add(new int[]{row, col});
            String conflictKey = row + "-" + col + "-" + value;
            conflictMap.put(conflictKey, conflictMap.getOrDefault(conflictKey, 0) + 1);

            // Backjumping
            if (conflictMap.get(conflictKey) > 1) {
                return false;
            }
        }
        return false; // If no placement leads to a solution, return false for backtracking
    }

    private static boolean isConsistent(int[][][] arr, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == 0 && placementCount(arr[i][j]) == 0) {
                    return false;
                }
            }
        }
        return true;
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

    private static int[] findMRVAndHighestDegreeCell(int[][][] arr, int subgrid, int gridSize, Map<String, Integer> conflictMap) {
        int boardLength = subgrid * gridSize;
        int minCount = Integer.MAX_VALUE;
        int maxDegree = -1;
        int[] cell = null;

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == 0) {
                    int count = placementCount(arr[i][j]);
                    int degree = calculateDegree(arr, i, j, subgrid, gridSize);
                    if (count < minCount || (count == minCount && degree > maxDegree)) {
                        minCount = count;
                        maxDegree = degree;
                        cell = new int[]{i, j};
                    }
                }
            }
        }
        if (cell != null) {
            String conflictKey = cell[0] + "-" + cell[1];
            if (conflictMap.containsKey(conflictKey)) {
                conflictMap.put(conflictKey, conflictMap.get(conflictKey) + 1);
            } else {
                conflictMap.put(conflictKey, 1);
            }
        }
        return cell;
    }

    private static int calculateDegree(int[][][] arr, int row, int col, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;
        int degree = 0;

        // Count the number of unassigned neighbors in the row and column
        for (int i = 0; i < boardLength; i++) {
            if (arr[row][i][0] == 0 && i != col) {
                degree++;
            }
            if (arr[i][col][0] == 0 && i != row) {
                degree++;
            }
        }

        // Count the number of unassigned neighbors in the subgrid
        int startRow = (row / subgrid) * subgrid;
        int startCol = (col / subgrid) * subgrid;
        for (int i = startRow; i < startRow + subgrid; i++) {
            for (int j = startCol; j < startCol + subgrid; j++) {
                if (arr[i][j][0] == 0 && (i != row || j != col)) {
                    degree++;
                }
            }
        }

        return degree;
    }

    private static List<Integer> getLCV(int[][][] arr, int row, int col, int subgrid, int gridSize) {
        List<Integer> possibleValues = getPossibleValues(arr[row][col]);
        Collections.shuffle(possibleValues, random); // Shuffle to introduce randomness
        possibleValues.sort((a, b) -> {
            return countConstraints(arr, row, col, a, subgrid, gridSize) - countConstraints(arr, row, col, b, subgrid, gridSize);
        });
        return possibleValues;
    }

    private static int countConstraints(int[][][] arr, int row, int col, int value, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;
        int constraints = 0;

        // Count constraints in the row
        for (int j = 0; j < boardLength; j++) {
            if (arr[row][j][0] == 0 && arr[row][j][value] == 1) {
                constraints++;
            }
        }

        // Count constraints in the column
        for (int i = 0; i < boardLength; i++) {
            if (arr[i][col][0] == 0 && arr[i][col][value] == 1) {
                constraints++;
            }
        }

        // Count constraints in the subgrid
        int startRow = (row / subgrid) * subgrid;
        int startCol = (col / subgrid) * subgrid;
        for (int i = startRow; i < startRow + subgrid; i++) {
            for (int j = startCol; j < startCol + subgrid; j++) {
                if (arr[i][j][0] == 0 && arr[i][j][value] == 1) {
                    constraints++;
                }
            }
        }

        return constraints;
    }
}