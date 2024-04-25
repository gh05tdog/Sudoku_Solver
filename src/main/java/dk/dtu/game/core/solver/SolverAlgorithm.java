package dk.dtu.game.core.solver;

public class SolverAlgorithm {

    private SolverAlgorithm() {
        throw new IllegalStateException("Utility class");
    }



    public static boolean checkBoard(int[][] board, int row, int col, int c, int constant) {
        boolean legalRowCol = true;
        boolean legalSquare = true;
        for (int p = 0; p < board.length; p++) {
            if (board[row][p] == c || board[p][col] == c) {
                legalRowCol = false;
                break;
            }
        }
        for (int p = (row / constant) * constant; p < (row / constant) * constant + constant; p++) {
            for (int q = (col / constant) * constant; q < (col / constant) * constant + constant; q++) {
                if (board[p][q] == c) {
                    legalSquare = false;
                    break;
                }
            }
            if (!legalSquare) break;
        }

        return legalRowCol && legalSquare;
    }
}

