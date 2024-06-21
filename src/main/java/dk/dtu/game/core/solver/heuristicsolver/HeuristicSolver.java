package dk.dtu.game.core.solver.heuristicsolver;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.SolverAlgorithm;

import java.util.*;

public class HeuristicSolver { // Class for solving the sudoku puzzle using a heuristic approach
    static Random random = new Random();
    static int recursionCount;


    public static void createPlayableSudoku (Board board) { // Create a playable sudoku game
        int n = board.getN(); // Get the size of the subgrid
        int k = board.getK(); // Get the size of the grid
        int[][][] possiblePlacements = createSetFromBoard(board, n, k); // Create a set of possible placements
        fillBoard(possiblePlacements, n, k, new HashSet<>(), new HashMap<>()); // Fill the board with a solution
        int [][] playableSudoku = deepCopy3DBoard(possiblePlacements, n, k); // extract the solution from the 3-dimensional board
        board.setSolvedBoard(playableSudoku); // send the solved board to board for easy storage


        int [][] gameBoard = removeNumsFromBoard(possiblePlacements, n, k); // remove numbers from the board to create a playable board


        board.setBoard(gameBoard); // set the board to the playable board
        board.setInitialBoard(gameBoard);
    }

    public static int[][][] createSetFromBoard(Board board, int subGrid, int gridSize) { // 3D board containing each cell of the sudoku, as well as each possible placement for each cell
        int boardLength = subGrid * gridSize;
        int numCount = subGrid * subGrid;
        int[][][] possiblePlacements = new int[boardLength][boardLength][numCount + 1]; // Create a 3-dimensional array to store the possible placements for each cell

        int[][] initialBoard = board.getInitialBoard(); // Get the initial board from the board object

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                possiblePlacements[i][j][0] = initialBoard[i][j]; // Set the initial board values

                for (int l = 1; l <= numCount; l++) {
                    possiblePlacements[i][j][l] = 1; // Set all possible placements to 1
                }
            }
        }

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (initialBoard[i][j] != 0) { // iterate through the entire board and remove possible placements for each cell
                    removePossiblePlacements(possiblePlacements, i, j, initialBoard[i][j], subGrid, gridSize);
                }
            }
        }

        return possiblePlacements;
    }

    public static void removePossiblePlacements(int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize; // Get the length of the board

        for (int i = 0; i < boardLength; i++) {
            arr[row][i][val] = 0; // Remove the value from the row
            arr[i][col][val] = 0; // Remove the value from the column
        }

        for (int i = (row / subgrid) * subgrid; i < subgrid + (row / subgrid) * subgrid; i++) {
            for (int j = (col / subgrid) * subgrid; j < subgrid + (col / subgrid) * subgrid; j++) {
                arr[i][j][val] = 0; // Remove the value from the subgrid
            }
        }
    }

    public static void addPossiblePlacements (int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;
        boolean [] rowContains = isRowPlacementPossible(arr, row, val, boardLength); // Check if the row contains the value
        boolean [] colContains = isColPlacementPossible(arr, col, val, boardLength); // Check if the column contains the value

        for (int i = 0; i < boardLength; i++) {
            if (!rowContains[i]) {
                arr[row][i][val] = 1; // for each cell in the row of the selected value, if the column of that cell does not contain the value, set the value to 1
            }
            if (!colContains[i]) {
                arr[i][col][val] = 1; // for each cell in the column of the selected value, if the row of that cell does not contain the value, set the value to 1
            }
        }
        boolean [][] subgridContains = isSubgridPlacementPossible(arr, row, col, val, subgrid, gridSize);
        for (int i = 0; i < subgrid; i++) {
            for (int j = 0; j < subgrid; j++) {
                if (!subgridContains[i][j]) { // for each cell in the subgrid of the selected value, if the cell does not contain the value, set the value to 1
                    arr[(row / subgrid) * subgrid + i][(col / subgrid) * subgrid + j][val] = 1;
                }
            }
        }

    }

    private static boolean[][] isSubgridPlacementPossible(int[][][] arr, int row, int col, int val, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;
        boolean[][] subgridContains = new boolean[subgrid][subgrid];
        int startRow = (row / subgrid) * subgrid;
        int startCol = (col / subgrid) * subgrid; // Get the starting row and column of the subgrid
        for (int i = startRow; i < startRow + subgrid; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == val) {
                    for (int n = 0; n < subgrid; n++) {
                        subgridContains[i - startRow][n] = true;
                    }
                }
            }
        }
        // for each cell in the subgrid, ensure no cell of the row in which the cell is located contains the value
        for (int i = startCol; i < startCol + subgrid; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[j][i][0] == val) {
                    for (int n = 0; n < subgrid; n++) {
                        subgridContains[n][i - startCol] = true;
                    }
                }
            }
        }
        // for each cell in the subgrid, ensure no cell of the column in which the cell is located contains the value
        return subgridContains;
    }

    public static boolean [] isRowPlacementPossible(int[][][]arr, int row, int val, int boardLength) {
        boolean[] rowContains = new boolean[boardLength];
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == val) {
                    rowContains[i] = true;
                    break;
                }
            }
        } // for each cell in the row, check if the cell contains the value
        return rowContains;
    }

    public static boolean [] isColPlacementPossible(int[][][]arr, int col, int val, int boardLength) {
        boolean[] colContains = new boolean[boardLength];
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[j][i][0] == val) {
                    colContains[i] = true;
                    break;
                }
            }
        } // for each cell in the column, check if the cell contains the value
        return colContains;
    }



    public static boolean fillBoard(int[][][] arr, int subgrid, int gridSize, Set<int[]> conflictSet, Map<String, Integer> conflictMap) {
        recursionCount++; // Increment the recursion count, only used for test-purposes
        int boardLength = subgrid * gridSize;


            boolean isChanged = true; // boolean value to ensure function only exits when no more placements can be made

            // Initial pass to make obvious placements
            while (isChanged) { // while placements can be made
                isChanged = false;
                for (int i = 0; i < boardLength; i++) {
                    for (int j = 0; j < boardLength; j++) { // run through each cell in the board
                        if (placementCount(arr[i][j]) == 1 && arr[i][j][0] == 0) { // if the cell has only one possible placement, or is empty
                            int value = placeableValue(arr[i][j]);  // get the placement value
                            arr[i][j][0] = value; // place the value in the cell
                            removePossiblePlacements(arr, i, j, value, subgrid, gridSize); // remove this value as a possible placement from the row, column and subgrid
                            isChanged = true; // a change has been made
                        }
                    }
                }
            }

        if (isFullyFilled(arr)) { // if the board is fully filled end the recursion
            return true;
        }
        // Find the cell with the minimum remaining values (MRV) and highest degree
        int[] mrvCell = findMRVAndHighestDegreeCell(arr, subgrid, gridSize, conflictMap); // find the cell with the minimum remaining values and highest degree

        if (mrvCell == null) { // if no cell is found, return false
            return false;
        }

        int row = mrvCell[0];
        int col = mrvCell[1];
        List<Integer> possibleValues = getLCV(arr, row, col, subgrid, gridSize);
        Collections.shuffle(possibleValues, random); // Shuffle possible values to introduce randomness

        for (int value : possibleValues) {
            int[][][] arrCopy = copyBoard(arr); // Make a copy of the board
            arrCopy[row][col][0] = value; // Place the value in the copy
            removePossiblePlacements(arrCopy, row, col, value, subgrid, gridSize);

            boolean consistent = isConsistent(arrCopy, subgrid, gridSize); // Perform forward checking

            if (consistent && fillBoard(arrCopy, subgrid, gridSize, conflictSet, conflictMap)) {
                // If the recursive call returns true, copy the solution back to the original array
                for (int r = 0; r < arr.length; r++) {
                    for (int c = 0; c < arr[r].length; c++) {
                        arr[r][c] = arrCopy[r][c].clone();
                    }
                }
                return true;
            }
            conflictSet.add(new int[]{row, col}); // Add the cell to the conflict set
            String conflictKey = row + "-" + col + "-" + value;
            conflictMap.put(conflictKey, conflictMap.getOrDefault(conflictKey, 0) + 1); // Add the conflict to the conflict map

            // Backjumping
            if (conflictMap.get(conflictKey) > 1) { // If the conflict has occurred more than once
                return false;
            }
        }
        return false; // If no placement leads to a solution, return false for backtracking
    }

    private static boolean isConsistent(int[][][] arr, int subgrid, int gridSize) {
        int boardLength = subgrid * gridSize;

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == 0 && placementCount(arr[i][j]) == 0) { // If a cell is empty and has no possible placements
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
        } // Create a deep copy of the board
        return copy;
    }

    private static List<Integer> getPossibleValues(int[] cell) {
        List<Integer> possibleValues = new ArrayList<>();
        for (int i = 1; i < cell.length; i++) {
            if (cell[i] == 1) {
                possibleValues.add(i);
            }
        } // Get the possible values for a cell
        return possibleValues;
    }

    private static boolean isFullyFilled(int[][][] arr) {
        for (int[][] integer : arr) {
            for (int[] anInt : integer) {
                if (anInt[0] == 0) {
                    return false;
                }
            }
        } // Check if the board is fully filled
        return true;
    }

    public static int placeableValue(int[] list) {
        for (int i = 1; i < list.length; i++) {
            if (list[i] == 1) {
                return i;
            }
        } // Get the value that can be placed in a cell
        return 0;
    }

    public static int placementCount(int[] list) {
        int count = 0;
        for (int i = 1; i < list.length; i++) {
            if (list[i] == 1) {
                count++;
            }
        } // Get the number of possible placements for a cell
        return count;
    }

    private static int[] findMRVAndHighestDegreeCell(int[][][] arr, int subgrid, int gridSize, Map<String, Integer> conflictMap) {
        int boardLength = subgrid * gridSize;
        int minCount = Integer.MAX_VALUE;
        int maxDegree = -1;
        int[] cell = null;

        // 2 heuristics in one, Minimum Remaining Values (MRV) and Highest Degree

        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                if (arr[i][j][0] == 0) {
                    int count = placementCount(arr[i][j]); // Get the number of possible placements for a cell
                    int degree = calculateDegree(arr, i, j, subgrid, gridSize); // Get the degree of the cell
                    if (count < minCount || (count == minCount && degree > maxDegree)) {
                        minCount = count;
                        maxDegree = degree;
                        cell = new int[]{i, j}; // Find the cell with the minimum remaining values and highest degree
                    }
                }
            }
        }
        if (cell != null) { // If a cell is found, add it to the conflict map
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

    public static int[][] removeNumsFromBoard(int[][][] sudokuBoard, int n, int k) {


        int[] row = new int[n * k];
        int[] col = new int[n * k];
        for (int i = 0; i < n * k; i++) {
            row[i] = i;
            col[i] = i;
        }
        row = fisherYatesShuffle(row);
        col = fisherYatesShuffle(col); // Shuffle the rows and columns

        int numsRemoved = 0;
        int maxNumRemoved = SolverAlgorithm.setNumsRemoved(new int[n * k][n * k]); // Set the number of numbers to remove based on the difficulty
        int tempVal;


        int i = 0;
        int j = 0;

        while (numsRemoved < maxNumRemoved && i < n * k) { // While the number of removed numbers is less than the maximum number of numbers to remove, continue iterating
            if (sudokuBoard[row[i]][col[j]][0] != 0) { // If the cell is not empty

                List<Integer> possibleValues = getPossibleValues(sudokuBoard[row[i]][col[j]]);
                Collections.shuffle(possibleValues, random); // Shuffle the possible values to introduce randomness

                tempVal = sudokuBoard[row[i]][col[j]][0];
                sudokuBoard[row[i]][col[j]][0] = 0;
                addPossiblePlacements(sudokuBoard, row[i], col[j], tempVal, n, k); // a number has been removed, and possible placements are added to the affected cells

                int [][][] copyBoard = copyBoard(sudokuBoard);

                boolean uniqueSolution = true;

                for (int val : possibleValues) {
                    copyBoard[row[i]][col[j]][0] = val; // Place a possible value in the cell
                    removePossiblePlacements(copyBoard, row[i], col[j], val, n, k); // Remove the value as a possible placement from the row, column and subgrid

                    if (fillBoard(copyBoard, n, k, new HashSet<>(), new HashMap<>())) {
                        uniqueSolution = false;
                        break;
                    } // If a unique solution is not found, break the loop

                    addPossiblePlacements(copyBoard, row[i], col[j], val, n, k); // Add the value as a possible placement again
                }

                if (uniqueSolution) { // If a unique solution is found, remove the number
                    row = fisherYatesShuffle(row);
                    col = fisherYatesShuffle(col);
                    i = 0;
                    j = 0;
                    numsRemoved++;
                    // reshuffle and begin at the start to ensure randomness

                } else {
                    sudokuBoard[row[i]][col[j]][0] = tempVal;
                    removePossiblePlacements(sudokuBoard, row[i], col[j], tempVal, n, k);
                } // If a unique solution is not found, restore the number
            }

            j++;
            if (j >= n * k) {
                j = 0;
                i++;
            }
        }

        return deepCopy3DBoard(sudokuBoard, n, k); // Extract the playable board from the 3-dimensional board
    }

    public static int[][] deepCopy3DBoard (int [][][] arr, int n, int k) {
        int [][] returnBoard = new int[n*k][n*k];
        for (int i = 0; i < n*k; i++) {
            for (int j = 0; j < n*k; j++) {
                returnBoard[i][j] = arr[i][j][0];
            }
        }
        return returnBoard; // Extract the playable board from the 3-dimensional board
    }

    public static int [] fisherYatesShuffle (int [] list) {
        for (int i = list.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = list[index];
            list[index] = list[i];
            list[i] = temp;
        }
        return list;
    } // Fisher-Yates shuffle algorithm, shuffles the list
}