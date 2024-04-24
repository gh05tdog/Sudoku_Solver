package dk.dtu.game.core.solver;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.config;
import dk.dtu.game.core.solver.AlgorithmX.ColumnNode;
import dk.dtu.game.core.solver.AlgorithmX.DancingLinks;
import dk.dtu.game.core.solver.AlgorithmX.Node;
import dk.dtu.game.core.solver.AlgorithmX.Placement;

import java.util.*;

import static java.lang.Math.sqrt;

public class solverAlgorithm {

    public static List<Node> solution = new ArrayList<>();
    private static int arraySize;

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


    public static void createXSudoku(Board board) throws Exception {
        int [][] arr = board.getBoard();
        arraySize = board.getDimensions();
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverfromBoard(arr, placements);
        DancingLinks dl = new DancingLinks(xBoard);
        ColumnNode header = dl.header;
        if (algorithmX(header)) {
            int[][] solutionBoard = convertSolutionToBoard(solution, placements);
            removeXRecursive(solutionBoard, arraySize*arraySize/2);
            board.setBoard(solutionBoard);
        } else {
            System.out.println("No solution found");
        }

    }

    public static List<int[]> createExactCoverMatrix(int n, int k, List<Placement> placements) {
        int size = k * n;
        int matrix_size = size * size * size;
        int constraint = size * size * 4;

        List<int[]> exactCoverMatrix = new ArrayList<>();

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
                    int[] rowArray = new int[constraint];
                    // constraint 1 - each cell must contain exactly one number
                    rowArray[row * size + col] = 1;

                    // constraint 2 - each number must appear exactly once in each row
                    rowArray[size * size + row * size + num] = 1;

                    // constraint 3 - each number must appear exactly once in each column
                    rowArray[2 * size * size + col * size + num] = 1;

                    // constraint 4 - each number must appear exactly once in each subgrid
                    int subGridID = (row / subSize) * subSize + (col / subSize);
                    rowArray[3 * size * size + subGridID * size + num] = 1;
                    exactCoverMatrix.add(rowArray);
                    placements.add(new Placement(row, col, num+1));

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

    public static void removeXRecursive(int [][] arr, int maxRemoved) {

        int numRemoved = 0;

        Random rand = new Random();
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverfromBoard(arr, placements);

        while (numRemoved < maxRemoved) {
            int randRow = rand.nextInt(arr.length);
            int randCol = rand.nextInt(arr.length);

            if (arr[randRow][randCol] == 0) {
                continue; // Skip already removed numbers.
            }

            int tempNumber = arr[randRow][randCol];
            arr[randRow][randCol] = 0; // Temporarily remove the number.

            // Manage exact cover changes.
            removeNumberFromXBoard(arr, randRow, randCol, tempNumber, xBoard);

            List<Integer> possiblePlacements = getPossiblePlacements(arr, randRow, randCol);

            if (canRemoveNumber(arr, randRow, randCol, xBoard, possiblePlacements)) {
                arr[randRow][randCol] = 0; // Remove the number.
                numRemoved++;
            } else {
                arr[randRow][randCol] = tempNumber; // Restore the number.
            }
        }
    }

    private static boolean canRemoveNumber(int[][] arr, int row, int col, List<int[]> xBoard, List<Integer> placements) {

        int possibleNums = 0;
        int chosenNum = 0;

        for (int num : placements) {
            addNumberToXBoard(arr, row, col, num, xBoard);
            arr[row][col] = num;

            DancingLinks dl = new DancingLinks(xBoard); // Create only once per board check.
            ColumnNode header = dl.header;

            if (algorithmX(header)) {
                possibleNums++;
                chosenNum = num;
            }
            removeNumberFromXBoard(arr, row, col, num, xBoard);
            arr[row][col] = 0; // Reset the cell to empty.
        }
        if (possibleNums == 1) {
            addNumberToXBoard(arr, row, col, chosenNum, xBoard);
            arr[row][col] = chosenNum; // Set the only possible number.
            return true;
        }
        return false;
    }

    public static void addNumberToXBoard(int[][] arr, int row, int col, int num, List<int[]> xBoard) {
        int [] list = new int[arr.length * arr.length * 4];

        list[row * arr.length + col] = 1;
        list[arr.length * arr.length + row * arr.length + num - 1] = 1;
        list[2 * arr.length * arr.length + col * arr.length + num - 1] = 1;
        int subSize = (int) Math.sqrt(arr.length);
        int subGridID = (row / subSize) * subSize + (col / subSize);
        list[3 * arr.length * arr.length + subGridID * arr.length + num - 1] = 1;

        xBoard.add(list);
    }

    public static void removeNumberFromXBoard(int[][] arr, int row, int col, int num, List<int[]> xBoard) {
        // Remove the number from the xBoard
        for (int i = 0; i < xBoard.size(); i++) {

            if (xBoard.get(i)[row * arr.length + col] == 1) {
                xBoard.remove(i);
                break;
            }
        }
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
                if (cell<10) {
                    System.out.print(cell + "  ");
                } else {
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
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
}

