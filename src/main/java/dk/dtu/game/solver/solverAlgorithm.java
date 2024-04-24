package dk.dtu.game.solver;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.config;

import java.util.*;

import static java.lang.Math.sqrt;

public class solverAlgorithm {

    public static List<Node> solution = new ArrayList<>();
    private static int arraySize;

    public static void main(String[] args) {
        arraySize = 4;
        removeXRecursive();
    }

    public static void createXSudoku(Board board) throws Exception {

    }

    public static void createSudoku(Board board) throws Exception {
        double startTime = System.nanoTime();
        fillBoard(board);
        double endTime = System.nanoTime();
        double duration = (endTime - startTime);
        System.out.println("Time taken to bruteforce board: " + duration / 1000000 + "ms");
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
        int maxNumRemoved = 0;
        String difficulty = config.getDifficulty();

        maxNumRemoved = switch (difficulty) {
            case "easy" -> 30;
            case "medium" -> 60;
            case "hard" -> 80;
            case "extreme" -> 200;
            default -> 30;
        };

        while (numRemoved < maxNumRemoved) {
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

    public static List<Integer> getPossiblePlacements(int[][] board, int row, int col) {
        List<Integer> possiblePlacements = new ArrayList<>();
        int subsize = (int) sqrt(board.length);
        for (int i = 1; i <= board.length; i++) {
            if (checkBoard(board, row, col, i, subsize)) {
                possiblePlacements.add(i);
            }
        }
        return possiblePlacements;
    }


// __________________________________________________________________________________________
// AlgorithmX using Dancing Links

 /*   public static int [][] createXBoard(int size) {

        int [][] arr = createExactCoverMatrix(size, size);

        printBoard(arr);

        DancingLinks dl = new DancingLinks(arr);


        ColumnNode header = dl.header;

        if (algorithmX(header)) {
            System.out.println("Solution found");
        } else {
            System.out.println("No solution found");
        }
        printBoard(arr);
        return convertSolutionToBoard(solution);
    } */

    public static int[][] createExactCoverMatrix(int n, int k) {
        int size = k * n;
        int matrix_size = size * size * size;
        int constraint = size * size * 4;

        int[][] exactCoverMatrix = new int[matrix_size][constraint];

        // exactCoverMatrix is set up. Now we need to fill it with the constraints
        // 1. Each cell must contain exactly one number
        // 2. Each number must appear exactly once in each row
        // 3. Each number must appear exactly once in each column
        // 4. Each number must appear exactly once in each subgrid
        // 5. Each cell must contain a number
        // 6. Each number must appear in the grid (not necessary, but makes it easier to check if the solution is correct)
        //
        // the matrix has size^3 rows, representing each cell filled with each number once.
        // the matrix has size^2*4 columns, representing the constraints
        // constraint 1: 81 cells to check each number is in each row
        // constraint 2: 81 cells to check each number is in each column
        // constraint 3: 81 cells to check each number is in each subgrid
        // constraint 4: 81 cells to check each cell is filled with a number

        int subSize = (int) Math.sqrt(size);

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                for (int num = 0; num < size; num++) {
                    int x = row * size * size + col * size + num;
                    // constraint 1 - each cell must contain exactly one number
                    exactCoverMatrix[x][row * size + col] = 1;

                    // constraint 2 - each number must appear exactly once in each row
                    exactCoverMatrix[x][size * size + row * size + num] = 1;

                    // constraint 3 - each number must appear exactly once in each column
                    exactCoverMatrix[x][2 * size * size + col * size + num] = 1;

                    // constraint 4 - each number must appear exactly once in each subgrid
                    int subGridID = (row / subSize) * subSize + (col / subSize);
                    exactCoverMatrix[x][3 * size * size + subGridID * size + num] = 1;
                }
            }
        }
        return exactCoverMatrix;
    }

    public static List<int[]> createExactCoverfromBoard(int [][] board, List<Placement> placements) {

        List<int[]> coverList = new ArrayList<>();
        int constraints = board.length * board.length * 4;

        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                int num = board[i][j];
                List<Integer> nums;
                if(num == 0) {
                    nums = getPossiblePlacements(board, i, j);
                } else {
                    nums = Collections.singletonList(num);
                }
                    for(int n : nums) {
                        int [] cover = new int[constraints];
                        cover[i * board.length + j] = 1;
                        cover[board.length * board.length + i * board.length + n - 1] = 1;
                        cover[2 * board.length * board.length + j * board.length + n - 1] = 1;
                        int subSize = (int) Math.sqrt(board.length);
                        int subGridID = (i / subSize) * subSize + (j / subSize);
                        cover[3 * board.length * board.length + subGridID * board.length + n - 1] = 1;
                        coverList.add(cover);
                        placements.add(new Placement(i, j, n));
                    }
                }
            }


        return coverList;
    }

    public static void removeXRecursive() {

        int[][] arr = { {1, 4, 2, 3},
                              {2, 3, 1, 4},
                              {4, 1, 3, 2},
                              {3, 2, 4, 1}};
        int numRemoved = 0;
        int maxNumRemoved = 4;

        int randRow = (int) (Math.random() * arr.length);
        int randCol = (int) (Math.random() * arr.length);

        arr [randRow][randCol] = 0;


        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverfromBoard(arr, placements);

        randRow = (int) (Math.random() * arr.length);
        randCol = (int) (Math.random() * arr.length);


        while (numRemoved < maxNumRemoved) {

            System.out.println("row and col: " + randRow + " " + randCol);

            printBoard(arr);

            int possibleSols = 0;

            while (arr[randRow][randCol] == 0) {
                randRow = (int) (Math.random() * arr.length);
                randCol = (int) (Math.random() * arr.length);
            }

            int tempNumber = arr[randRow][randCol];
            arr[randRow][randCol] = 0;

            int cellIndex = randRow * arraySize + randCol;                  // Cell index
            int rowIndex = arraySize*arraySize + randRow * arraySize + (tempNumber - 1); // Row constraint index
            int colIndex = 2*arraySize*arraySize + randCol * arraySize + (tempNumber - 1); // Column constraint index
            int boxIndex = 3*arraySize*arraySize + (randRow / 3 * 3 + randCol / 3) * arraySize + (tempNumber - 1); // Box constraint index

            // Iterate through the exact cover list to find and remove the row

            for(int i = 0; i < xBoard.size(); i++) {
                int[] coverRow = xBoard.get(i);
                if (coverRow[cellIndex] == 1 && coverRow[rowIndex] == 1 &&
                        coverRow[colIndex] == 1 && coverRow[boxIndex] == 1) {
                    xBoard.remove(coverRow);  // Remove this row from the cover matrix
                    break;
                }

            List<Integer> values = getPossiblePlacements(arr, randRow, randCol);

            int possibleNums = 0;
            for (int value : values) {
                int [] cover = new int[arraySize*arraySize*4];
                cover[randRow * arraySize + randCol] = 1;
                cover[arraySize * arraySize + randRow * arraySize + value - 1] = 1;
                cover[2 * arraySize * arraySize + randCol * arraySize + value - 1] = 1;
                int subSize = (int) Math.sqrt(arraySize);
                int subGridID = (randRow / subSize) * subSize + (randCol / subSize);
                cover[3 * arraySize * arraySize + subGridID * arraySize + value - 1] = 1;
                xBoard.add(cover);

                arr[randRow][randCol] = value;
                if (algorithmX(new DancingLinks(xBoard).header)) {
                    possibleNums++;
                }
                xBoard.remove(cover);
                if (possibleNums > 1) {
                    arr[randRow][randCol] = tempNumber;
                    break;
                }
            }
            if (possibleNums == 1) {
                System.out.println("Removed " + tempNumber + " from row " + randRow + " and column " + randCol);
                    numRemoved++;
                }
            }






        }
        printBoard(arr);
    }

    public static boolean algorithmX(ColumnNode header) {
        if (header.right == header) {
            return true;  // Return true indicating the solution was found
        }

        ColumnNode c = chooseHeuristicColumn(header);

        c.cover();

        // Optionally, display the state of the linked list or affected columns here

        for (Node r = c.down; r != c; r = r.down) {
            selectRow(r);
            for (Node j = r.right; j != r; j = j.right) {
                j.column.cover();
            }

            if (algorithmX(header)) {
                return true;  // Return immediately if solution was found
            }

            for (Node j = r.left; j != r; j = j.left) {
                j.column.uncover();
            }
            deselectRow(r);
        }

        c.uncover();

        return false;  // Return false as no solution was found in this path
    }

    public static ColumnNode chooseHeuristicColumn(ColumnNode header) {
        Random rand = new Random();
        ColumnNode c = (ColumnNode) header.right;
        List<ColumnNode> columns = new ArrayList<>();
        int minSize = c.size;
        for (ColumnNode temp = (ColumnNode) header.right; temp != header; temp = (ColumnNode) temp.right) {
            if (temp.size < minSize) {
                minSize = temp.size;
                columns.clear();
                columns.add(temp);
            } else if (temp.size == minSize) {
                columns.add(temp);
            }
        }
        return columns.get(rand.nextInt(columns.size()));
    }

    private static void selectRow(Node row) {
        solution.add(row);
    }

    private static void deselectRow(Node row) {
        solution.remove(row);
    }

    public static void printMatrix(DancingLinks dl) {
        ColumnNode header = dl.header;
        ColumnNode columnNode = (ColumnNode) header.right;

        // Traverse all columns from the header
        while (columnNode != header) {
            System.out.println("Column " + columnNode.name + " size: " + columnNode.size);
            Node rowNode = columnNode.down;

            // Traverse all rows in the current column
            while (rowNode != columnNode) {
                // Print details about each node in the row for the current column
                System.out.print("Row " + getRowIndex(rowNode) + " -> ");
                Node rightNode = rowNode.right;

                // Traverse all nodes in this row (right direction from the current column's node)
                while (rightNode != rowNode) {
                    System.out.print(rightNode.column.name + " ");
                    rightNode = rightNode.right;
                }

                System.out.println(); // New line for each row
                rowNode = rowNode.down;
            }
            columnNode = (ColumnNode) columnNode.right;
        }
    }

    // Utility function to check if a column is correctly covered
    public static boolean isColumnCovered(ColumnNode column) {
        // Check that the column's left and right links bypass this column
        if (column.left.right != column.right || column.right.left != column.left) {
            return false;
        }

        // Ensure no node in this column is accessible from other parts of the matrix
        for (Node node = column.down; node != column; node = node.down) {
            for (Node rightNode = node.right; rightNode != node; rightNode = rightNode.right) {
                if (rightNode.column == column) {
                    // If any node still points back to this column, it's not fully covered
                    return false;
                }
            }
        }

        return true;
    }

    // Utility function to check if a column is correctly uncovered
    public static boolean isColumnUncovered(ColumnNode column, int[][] matrix) {
        // Verify column links are restored
        if (column.left.right != column || column.right.left != column) {
            return false;
        }

        // Check if all nodes are restored in the column as per the matrix definition
        int currentRow = 0;
        for (Node node = column.down; node != column; node = node.down, currentRow++) {
            if (matrix[getRowIndex(node)][Integer.parseInt(column.name)] != 1) {
                return false;  // The node exists where matrix indicates there should be no node
            }
        }

        return true;
    }

    // Utility function to get row index if not inherently stored in the Node
    private static int getRowIndex(Node node) {
        return node.rowIndex;  // Directly return the stored row index
    }

    public static int[][] convertSolutionToBoard(List<Node> solution, List<Placement> placements) {
        int[][] board = new int[arraySize][arraySize];

        for (Node node : solution) {
            Placement placement = placements.get(node.rowIndex);
            board[placement.row][placement.col] = placement.value;
        }

        return board;
    }

    public static void printList(List<int[]> list) {
        for (int[] arr : list) {
            for (int i : arr) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    public static void printBoard(int[][] matrix) {
        for (int[] row : matrix) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }
}

