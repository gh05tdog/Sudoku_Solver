/* (C)2024 */
package dk.dtu.game.core.solver.AlgorithmX;

import static java.lang.Math.sqrt;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.solver.SolverAlgorithm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class algorithmX {
    public static List<Node> solution = new ArrayList<>();
    private static int arraySize;

    static int[][] solvedBoard;

    public static void createXSudoku(Board board) {
        int[][] arr = board.getBoard();
        arraySize = board.getDimensions();
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(arr, placements);
        DancingLinks dl = new DancingLinks(xBoard);
        ColumnNode header = dl.header;
        if (algorithmXSolver(header)) {
            int[][] sudokuBoard = convertSolutionToBoard(solution, placements);
            solvedBoard = new int[arraySize][arraySize];
            deepSetSolutionBoard(sudokuBoard);
            removeXRecursive(sudokuBoard, arraySize * arraySize / 2);
            board.setInitialBoard(sudokuBoard);
            board.setBoard(sudokuBoard);
        } else {
            System.out.println("No solution found");
        }
    }

    public static List<Integer> getPossiblePlacements(int[][] board, int row, int col) {
        List<Integer> possiblePlacements = new ArrayList<>();
        int subSize = (int) sqrt(board.length);
        for (int i = 1; i <= board.length; i++) {
            if (SolverAlgorithm.checkBoard(board, row, col, i, subSize)) {
                possiblePlacements.add(i);
            }
        }
        return possiblePlacements;
    }

    public static List<int[]> createExactCoverFromBoard(int[][] board, List<Placement> placements) {

        List<int[]> coverList = new ArrayList<>();
        int constraints = board.length * board.length * 4;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                int num = board[i][j];
                List<Integer> nums;
                if (num == 0) {
                    nums = getPossiblePlacements(board, i, j);
                } else {
                    nums = Collections.singletonList(num);
                }
                for (int n : nums) {
                    int[] cover = new int[constraints];
                    setCoverRow(board, coverList, i, j, n, cover);
                    placements.add(new Placement(i, j, n));
                }
            }
        }

        return coverList;
    }

    private static void setCoverRow(
            int[][] board, List<int[]> coverList, int i, int j, int n, int[] cover) {
        cover[i * board.length + j] = 1;
        cover[board.length * board.length + i * board.length + n - 1] = 1;
        cover[2 * board.length * board.length + j * board.length + n - 1] = 1;
        int subSize = (int) Math.sqrt(board.length);
        int subGridID = (i / subSize) * subSize + (j / subSize);
        cover[3 * board.length * board.length + subGridID * board.length + n - 1] = 1;
        coverList.add(cover);
    }

    public static void removeXRecursive(int[][] arr, int maxRemoved) {

        int numRemoved = 0;

        Random rand = new Random();
        List<Placement> placements = new ArrayList<>();
        List<int[]> xBoard = createExactCoverFromBoard(arr, placements);

        while (numRemoved < maxRemoved) {
            int randRow = rand.nextInt(arr.length);
            int randCol = rand.nextInt(arr.length);

            if (arr[randRow][randCol] == 0) {
                continue; // Skip already removed numbers.
            }

            int tempNumber = arr[randRow][randCol];
            arr[randRow][randCol] = 0; // Temporarily remove the number.

            // Manage exact cover changes.
            removeNumberFromXBoard(arr, randRow, randCol, xBoard);

            List<Integer> possiblePlacements = getPossiblePlacements(arr, randRow, randCol);

            if (canRemoveNumber(arr, randRow, randCol, xBoard, possiblePlacements)) {
                arr[randRow][randCol] = 0; // Remove the number.
                numRemoved++;
            } else {
                arr[randRow][randCol] = tempNumber; // Restore the number.
            }
        }
    }

    private static boolean canRemoveNumber(
            int[][] arr, int row, int col, List<int[]> xBoard, List<Integer> placements) {

        int possibleNums = 0;
        int chosenNum = 0;

        for (int num : placements) {
            addNumberToXBoard(arr, row, col, num, xBoard);
            arr[row][col] = num;

            DancingLinks dl = new DancingLinks(xBoard); // Create only once per board check.
            ColumnNode header = dl.header;

            if (algorithmXSolver(header)) {
                possibleNums++;
                chosenNum = num;
            }
            removeNumberFromXBoard(arr, row, col, xBoard);
        }
        if (possibleNums == 1) {
            addNumberToXBoard(arr, row, col, chosenNum, xBoard);
            arr[row][col] = chosenNum; // Set the only possible number.
            return true;
        }
        return false;
    }

    public static void addNumberToXBoard(
            int[][] arr, int row, int col, int num, List<int[]> xBoard) {
        int[] list = new int[arr.length * arr.length * 4];

        setCoverRow(arr, xBoard, row, col, num, list);
    }

    public static void removeNumberFromXBoard(int[][] arr, int row, int col, List<int[]> xBoard) {
        // Remove the number from the xBoard
        for (int i = 0; i < xBoard.size(); i++) {

            if (xBoard.get(i)[row * arr.length + col] == 1) {
                xBoard.remove(i);
                break;
            }
        }
    }

    public static boolean algorithmXSolver(ColumnNode header) {
        if (header.right == header) {
            return true; // Return true indicating the solution was found
        }

        ColumnNode c = chooseHeuristicColumn(header);

        c.cover();

        // Optionally, display the state of the linked list or affected columns here

        for (Node r = c.down; r != c; r = r.down) {
            selectRow(r);
            for (Node j = r.right; j != r; j = j.right) {
                j.column.cover();
            }

            if (algorithmXSolver(header)) {
                return true; // Return immediately if solution was found
            }

            for (Node j = r.left; j != r; j = j.left) {
                j.column.uncover();
            }
            deselectRow(r);
        }

        c.uncover();

        return false; // Return false as no solution was found in this path
    }

    public static ColumnNode chooseHeuristicColumn(ColumnNode header) {
        Random rand = new Random();
        ColumnNode c = (ColumnNode) header.right;
        List<ColumnNode> columns = new ArrayList<>();
        int minSize = c.size;
        for (ColumnNode temp = (ColumnNode) header.right;
                temp != header;
                temp = (ColumnNode) temp.right) {
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

    public static int[][] convertSolutionToBoard(List<Node> solution, List<Placement> placements) {
        int[][] board = new int[arraySize][arraySize];

        for (Node node : solution) {
            Placement placement = placements.get(node.rowIndex);
            board[placement.row][placement.col] = placement.value;
        }

        return board;
    }

    public static void deepSetSolutionBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, solvedBoard[i], 0, board.length);
        }
    }

    public static int[][] getSolutionBoard() {
        return solvedBoard;
    }
}
