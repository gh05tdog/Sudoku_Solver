/* (C)2024 */
package dk.dtu.game.core.solver.heuristicsolver;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.SolverAlgorithm;
import java.util.*;

public class HeuristicSolver {
    static Random random = new Random();
    static int recursionCount;

    public static void createPlayableSudoku(Board board) {
        int n = board.getN();
        int k = board.getK();
        int[][][] possiblePlacements = createSetFromBoard(board, n, k);
        fillBoard(possiblePlacements, n, k, new HashSet<>(), new HashMap<>());
        int[][] playableSudoku = deepCopy3DBoard(possiblePlacements, n, k);
        board.setSolvedBoard(playableSudoku);

        int[][] gameBoard = removeNumsFromBoard(possiblePlacements, n, k);

        board.setBoard(gameBoard);
    }

    public static int[][][] createSetFromBoard(Board board, int subGrid, int gridSize) {
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
                    removePossiblePlacements(
                            possiblePlacements, i, j, initialBoard[i][j], subGrid, gridSize);
                }
            }
        }

        return possiblePlacements;
    }

    public static void removePossiblePlacements(
            int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
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

    public static void addPossiblePlacements(
            int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;
        boolean[] rowContains = isRowPlacementPossible(arr, val, boardLength);
        boolean[] colContains = isColPlacementPossible(arr, val, boardLength);

        for (int i = 0; i < boardLength; i++) {
            if (!rowContains[i]) {
                arr[row][i][val] = 1;
            }
            if (!colContains[i]) {
                arr[i][col][val] = 1;
            }
        }
        boolean[][] subgridContains =
                isSubgridPlacementPossible(arr, row, col, val, subgrid, gridSize);
        for (int i = 0; i < subgrid; i++) {
            for (int j = 0; j < subgrid; j++) {
                if (!subgridContains[i][j]) {
                    arr[(row / subgrid) * subgrid + i][(col / subgrid) * subgrid + j][val] = 1;
                }
            }
        }
    }

    private static boolean[][] isSubgridPlacementPossible(
            int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;
        boolean[][] subgridContains = new boolean[subgrid][subgrid];
        int startRow = (row / subgrid) * subgrid;
        int startCol = (col / subgrid) * subgrid;
        for (int i = startRow; i < startRow + subgrid; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == val) {
                    for (int n = 0; n < subgrid; n++) {
                        subgridContains[i - startRow][n] = true;
                    }
                }
            }
        }
        for (int i = startCol; i < startCol + subgrid; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[j][i][0] == val) {
                    for (int n = 0; n < subgrid; n++) {
                        subgridContains[n][i - startCol] = true;
                    }
                }
            }
        }
        return subgridContains;
    }

    public static boolean[] isRowPlacementPossible(int[][][] arr, int val, int boardLength) {
        boolean[] rowContains = new boolean[boardLength];
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == val) {
                    rowContains[i] = true;
                    break;
                }
            }
        }
        return rowContains;
    }

    public static boolean[] isColPlacementPossible(int[][][] arr, int val, int boardLength) {
        boolean[] colContains = new boolean[boardLength];
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[j][i][0] == val) {
                    colContains[i] = true;
                    break;
                }
            }
        }
        return colContains;
    }

    public static boolean fillBoard(
            int[][][] arr,
            int subgrid,
            int gridSize,
            Set<int[]> conflictSet,
            Map<String, Integer> conflictMap) {
        recursionCount++;
        int boardLength = subgrid * gridSize;

        boolean isChanged = true;

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

        if (isFullyFilled(arr)) {
            return true;
        }
        // Find the cell with the minimum remaining values (MRV) and highest degree
        int[] mrvCell = findMRVAndHighestDegreeCell(arr, subgrid, gridSize, conflictMap);

        if (mrvCell == null) {
            return false;
        }

        int row = mrvCell[0];
        int col = mrvCell[1];
        List<Integer> possibleValues = getLCV(arr, row, col, subgrid, gridSize);
        Collections.shuffle(
                possibleValues, random); // Shuffle possible values to introduce randomness

        for (int value : possibleValues) {
            int[][][] arrCopy = copyBoard(arr); // Make a copy of the board
            arrCopy[row][col][0] = value; // Place the value in the copy
            removePossiblePlacements(arrCopy, row, col, value, subgrid, gridSize);

            // Perform forward checking
            boolean consistent = isConsistent(arrCopy, subgrid, gridSize);

            if (consistent && fillBoard(arrCopy, subgrid, gridSize, conflictSet, conflictMap)) {
                // If the recursive call returns true, copy the solution back to the original array
                for (int r = 0; r < arr.length; r++) {
                    for (int c = 0; c < arr[r].length; c++) {
                        arr[r][c] = arrCopy[r][c].clone();
                    }
                }
                return true;
            }
            conflictSet.add(new int[] {row, col});
            String conflictKey = row + "-" + col + "-" + value;
            conflictMap.put(conflictKey, conflictMap.getOrDefault(conflictKey, 0) + 1);

            // Back jumping
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
                System.arraycopy(arr[i][j], 0, copy[i][j], 0, arr[i][j].length);
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
        for (int[][] integer : arr) {
            for (int[] anInt : integer) {
                if (anInt[0] == 0) {
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

    private static int[] findMRVAndHighestDegreeCell(
            int[][][] arr, int subgrid, int gridSize, Map<String, Integer> conflictMap) {
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
                        cell = new int[] {i, j};
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

    private static List<Integer> getLCV(
            int[][][] arr, int row, int col, int subgrid, int gridSize) {
        List<Integer> possibleValues = getPossibleValues(arr[row][col]);
        Collections.shuffle(possibleValues, random); // Shuffle to introduce randomness
        possibleValues.sort(
                Comparator.comparingInt(
                        a -> countConstraints(arr, row, col, a, subgrid, gridSize)));
        return possibleValues;
    }

    private static int countConstraints(
            int[][][] arr, int row, int col, int value, int subgrid, int gridSize) {
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

    public static int[][] removeNumsFromBoard(int[][][] sudokuBoard, int n, int k) {

        int[] row = new int[n * k];
        int[] col = new int[n * k];
        for (int i = 0; i < n * k; i++) {
            row[i] = i;
            col[i] = i;
        }
        row = fisherYatesShuffle(row);
        col = fisherYatesShuffle(col);

        int numsRemoved = 0;
        int maxNumRemoved = SolverAlgorithm.setNumsRemoved(new int[n * k][n * k]);
        int tempVal;

        int i = 0;
        int j = 0;

        while (numsRemoved < maxNumRemoved && i < n * k) {
            if (sudokuBoard[row[i]][col[j]][0] != 0) {

                List<Integer> possibleValues = getPossibleValues(sudokuBoard[row[i]][col[j]]);
                Collections.shuffle(possibleValues, random);

                tempVal = sudokuBoard[row[i]][col[j]][0];
                sudokuBoard[row[i]][col[j]][0] = 0;
                addPossiblePlacements(sudokuBoard, row[i], col[j], tempVal, n, k);

                int[][][] copyBoard = copyBoard(sudokuBoard);

                boolean uniqueSolution = true;

                for (int val : possibleValues) {
                    copyBoard[row[i]][col[j]][0] = val;
                    removePossiblePlacements(copyBoard, row[i], col[j], val, n, k);

                    if (fillBoard(copyBoard, n, k, new HashSet<>(), new HashMap<>())) {
                        uniqueSolution = false;
                        break;
                    }

                    addPossiblePlacements(copyBoard, row[i], col[j], val, n, k);
                }

                if (uniqueSolution) {
                    row = fisherYatesShuffle(row);
                    col = fisherYatesShuffle(col);
                    i = 0;
                    j = 0;
                    numsRemoved++;

                } else {
                    sudokuBoard[row[i]][col[j]][0] = tempVal;
                    removePossiblePlacements(sudokuBoard, row[i], col[j], tempVal, n, k);
                }
            }

            j++;
            if (j >= n * k) {
                j = 0;
                i++;
            }
        }

        return deepCopy3DBoard(sudokuBoard, n, k);
    }

    public static int[][] deepCopy3DBoard(int[][][] arr, int n, int k) {
        int[][] returnBoard = new int[n * k][n * k];
        for (int i = 0; i < n * k; i++) {
            for (int j = 0; j < n * k; j++) {
                returnBoard[i][j] = arr[i][j][0];
            }
        }
        return returnBoard;
    }

    public static int[] fisherYatesShuffle(int[] list) {
        for (int i = list.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = list[index];
            list[index] = list[i];
            list[i] = temp;
        }
        return list;
    }
}
