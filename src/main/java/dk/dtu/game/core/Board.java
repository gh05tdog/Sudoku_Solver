    package dk.dtu.game.core;

    import java.util.ArrayList;

    public class Board {

        private final int n;
        private final int k;

        private final int size;

        private int [][] board;

        public Board(int n, int k) throws Exception {
            this.n = n;
            this.k = k;
            this.size = n*k;

            if (!boardIsPossible(k, n)) {
                throw new Exception("This board is not possible to create");
            }
            this.board = new int [n*k][n*k];
            int numRows = n * k;
            // Initialize the board with zeros
            fillZeros(board);

        }

        private boolean boardIsPossible(int k, int n) {
            return (k * n) <= (n * n);
        }

        public void setNumber(int row, int col, int num) {
            board[row][col] = num;
        }

        public boolean validPlace(int x, int y, int num) {
            // Check rows and check square
            return !contains(getRow(x),num) && !contains(getRow(y),num) && !squareContains(getSquare(x,y),num);
        }

        public int[] getRow(int numRow) {
            int [] row = new int[size];
            for (int j = 0; j < size; j++) {
                row[j] = board[numRow][j];

            }
            return row;
        }

        public boolean contains (int [] arr, int num) {
            for (int j : arr) {
                if (j == num) {
                    return true;
                }
            }
            return false;
        }

        public boolean squareContains(int [][] square, int num) {
            for (int[] ints : square) {
                for (int anInt : ints) {
                    if (anInt == num) {
                        return true;
                    }
                }
            }
            return false;
        }

        public int[] getColumn(int numCol) {
            int [] column = new int[size];
            for (int j = 0; j < size; j++) {
                column[j] = board[j][numCol];

            }
            return column;
        }

        public void fillZeros(int[][] board) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    board[i][j] = 0;
                }
            }
        }

        public int[][] getSquare(int x, int y) {
            int [][] square = new int [n][n];

            for(int row = 0; row < n; row++) {
                for(int col = 0; col < n; col++) {
                    square[row][col] = board[(x / n) * n + row][(y / n) * n + col];
                }
            }
            return square;
        }

        public void printBoard() {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    System.out.print(board[i][j] + " ");
                }
                System.out.println();
            }
        }

        public int getDimensions(){
            return n * k;
        }

        public void clear() {
            int numRows = n * k;
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numRows; j++) {
                    board[i][j] = 0;
                }
            }
        }

        public int getNumber(int row, int col) {
            return board[row][col];
        }

        public int[][] getBoard() {
            return board;
        }

        public void setBoard(int[][] tempBoard) {
            this.board = tempBoard;
        }
    }
