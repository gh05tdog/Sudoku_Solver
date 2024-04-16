package dk.dtu.game.solver;

import dk.dtu.game.core.Board;

import java.util.*;

import static java.lang.Math.sqrt;

public class solverAlgorithm {

    private static List <Node> solution = new ArrayList<>();

    public static boolean solutionFound = false;

    public static void main(String[] args) {

        int [][] tempBoard = createExactCoverMatrix(2,2);
        dancingLinks dl = new dancingLinks(tempBoard);
        ColumnNode header = dl.header;
        algorithmX(header);
    }

    public static void createSudoku(Board board) throws Exception {
        long startTime = System.nanoTime();
        fillBoard(board);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time taken to generate board: " + duration/1000 + "ns");
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
        while (numRemoved < 50) {
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

    // use exact cover problem to solve sudoku
    // size of matrix:
      // column size: (n*k)^3
      // row size: (n*k)^2*4

    // exact_cover_matrix [(n*k)^3][(n*k)^2*4]

    // 1. create exact cover matrix
    // 2. solve exact cover problem
    // 3. convert solution to sudoku board

    public static int[][] createExactCoverMatrix(int n, int k) {
        int size = k*n;
        int matrix_size = size*size*size;
        int constraint = size*size*4;

        int [][]exactCoverMatrix = new int[matrix_size][constraint];

        // exactCovermatrix is set up. Now we need to fill it with the constraints
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
                    int x = row*size*size + col*size + num;
                    // constraint 1 - each cell must contain exactly one number
                    exactCoverMatrix[x][row*size + col] = 1;

                    // constraint 2 - each number must appear exactly once in each row
                    exactCoverMatrix[x][size*size + row*size + num] = 1;

                    // constraint 3 - each number must appear exactly once in each column
                    exactCoverMatrix[x][2*size*size + col*size + num] = 1;

                    // constraint 4 - each number must appear exactly once in each subgrid
                    int subGridID = (row/subSize)*subSize + (col/subSize);
                    exactCoverMatrix[x][3*size*size + subGridID*size + num] = 1;
                }
            }
        }
        return exactCoverMatrix;
    }
    public static void algorithmX(ColumnNode header) {
        if (solutionFound) {
            System.out.println("Exiting as solution is found.");
            return;
        }

        if (header.right == header) {
            System.out.println("Solution found");
            solutionFound = true;
            printSolution(); // A method to print or process the solution
            convertSolutionToBoard();
            return;
        }

        ColumnNode c = chooseHeuristicColumn(header);
        c.cover();

        for (Node r = c.down; r != c; r = r.down) {
            if (solutionFound) {
                return;
            }
            selectRow(r);
            for (Node j = r.right; j != r; j = j.right) {
                j.column.cover();
            }
            algorithmX(header);
            c = r.column; // ensure the correct column is reestablished after recursion

            for (Node j = r.left; j != r; j = j.left) {
                j.column.uncover();
            }
            deselectRow(r);
        }
        c.uncover();
    }

    private static void printSolution() {
        System.out.println("Current solution set:");
        for (Node node : solution) {
            System.out.println(node); // Assuming Node.toString() gives meaningful output
        }
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

    public static void convertSolutionToBoard() {

        int[][] board = new int[4][4];

        for (Node node : solution) {
            Node temp = node;
            int min = Integer.parseInt(temp.column.name);
            temp = temp.right;
            while (temp != node) {
                int val = Integer.parseInt(temp.column.name);
                if (val < min) {
                    min = val;
                }
                temp = temp.right;
            }
            int num = min % board.length + 1;
            min /= board.length;
            int col = min % board.length;
            min /= board.length;
            int row = min;
            board[row][col] = num;
        }

        printBoard(board);
    }

    public static void printBoard(int[][] board) {
        for (int[] row : board) {
            System.out.println(Arrays.toString(row));
        }
    }

}
