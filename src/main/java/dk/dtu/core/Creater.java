package dk.dtu.core;

import java.util.*;
import java.util.stream.IntStream;

public class Creater {
    public static void fillBoard(Board board) {
        ArrayList<Integer> numbers = new ArrayList<>(IntStream.rangeClosed(1, board.getDimensions()).boxed().toList());
        // Shuffle the numbers
        Collections.shuffle(numbers);
        fillBoardRecursive(board, numbers);
    }

    private static boolean fillBoardRecursive(Board board, ArrayList<Integer> numbers) {
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

    public static void createSudoku(Board board) {
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

    private static boolean hasUniqueSolution(Board board) {
        // Implement a method that attempts to solve the board and stops
        // if it finds more than one solution, indicating it's not unique.

        return false;
    }
}
