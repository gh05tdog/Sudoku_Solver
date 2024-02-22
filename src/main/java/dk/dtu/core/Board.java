package dk.dtu.core;

public class Board {

    private int n;
    private int k;

    private int[][] board;

    public Board(int k, int n) throws Exception {
        this.n = n;
        this.k = k;
        // Create empty board array
        this.board = createEmptyBoard(n, k);
    }

    public int[][] createEmptyBoard(int k, int n) throws Exception {
        //Check if board is possible to create

        if (!boardIsPossible(k, n)) {
            throw new Exception("This board is not possible to create");
        }
        int num = n * n;
        return new int[num][num];
    }

    public int[][] getBoard(){
        return this.board;
    }

    public void setBoard (int[][] board){
        this.board = board;
    }
    private boolean boardIsPossible(int k, int n) {
        return Math.pow(k, 2) == n * n;
    }
}
