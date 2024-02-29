package dk.dtu.core;

import java.util.*;
import java.util.stream.IntStream;

public class Creater {
    public void fillBoard(Board board) {
        //Clear the board
        board.clear();
        ArrayList<Integer> numbers = new ArrayList<>(IntStream.rangeClosed(1, board.getDimensions()).boxed().toList());
        // Shuffle the numbers
        Collections.shuffle(numbers);
        fillBoardRecursive(board, numbers);
    }

    private boolean fillBoardRecursive(Board board, ArrayList<Integer> numbers) {
        for (int x = 0; x < board.getDimensions(); x++) {
            for (int y = 0; y < board.getDimensions(); y++) {
                // Find the next empty cell
                if (board.getNumber(x, y) == 0) {
                    ArrayList<Integer> shuffledNumbers = new ArrayList<>(numbers);
                    // Shuffle numbers to try for each cell
                    Collections.shuffle(shuffledNumbers);
                    for (int number : shuffledNumbers) {
                        if (board.validPlace(x, y, number)) {
                            // Attempt to place a number
                            board.setNumber(x, y, number);
                            if (fillBoardRecursive(board, numbers)) {
                                // Success, the number leads to a solution
                                return true;
                            }
                            // Backtrack, remove the number
                            board.setNumber(x, y, 0);
                        }
                    }
                    // No valid number found for this square, need to backtrack
                    return false;
                }
            }
        }
        // Done filling board
        return true;
    }

    public void createSudoku(Board board) throws Exception {
        fillBoard(board);

        //Create list of possible places
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < board.getDimensions(); i++) {
            for (int j = 0; j < board.getDimensions(); j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions);

        for (int[] pos : positions) {
            // Remember the number in case we need to undo the removal.
            int tempNumber = board.getNumber(pos[0], pos[1]);

            // Remove the number.
            board.setNumber(pos[0], pos[1], 0);

            // Check if the board still has a unique solution.
            if (!hasUniqueSolution(board)) {
                // If not unique, undo the removal.
                board.setNumber(pos[0], pos[1], tempNumber);
            }
        }
    }

    public boolean hasUniqueSolution(Board board) {
        //Check if it only has one Solution
        return backtraceSolve(board, 0) == 1;
    }

    private int backtraceSolve(Board board, int count) {
        if (count > 1) {
            // If we've found more than one solution, stop searching
            return count;
        }
        for (int x = 0; x < board.getDimensions(); x++) {
            for (int y = 0; y < board.getDimensions(); y++) {
                // Find the next empty cell
                if (board.getNumber(x, y) == 0) {
                    for (int number = 1; number <= board.getDimensions(); number++) {
                        if (board.validPlace(x, y, number)) {
                            // Attempt to place a number
                            board.setNumber(x, y, number);
                            count = backtraceSolve(board, count);
                            // Backtrack, remove the number
                            board.setNumber(x, y, 0);
                        }
                    }
                    // If no number is valid, return count as is
                    return count;
                }
            }
        }
        // Found a solution, increment count and return it
        return count + 1;
    }
}
