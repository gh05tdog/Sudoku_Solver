package dk.dtu;

import dk.dtu.core.Board;
import dk.dtu.core.Creater;

class SudokuSolverApp {
    public static void main(String[] args) throws Exception {
        Board board = new Board(3,3);
        Creater.createSudoku(board);
        board.printBoard();
    }
}