    package dk.dtu.game.core;

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
            // Initialize the board with zeros
            fillZeros(board);

        }

        private boolean boardIsPossible(int k, int n) {
            return (k * n) <= (n * n);
        }

        public void setNumber(int x, int y, int num) {
            board[x][y] = num;
        }

        public boolean validPlace(int x, int y, int num) {
            // Check rows and check square
            System.out.println("Checking if " + num + " can be placed at " + x + ", " + y);
            return contains(getRow(x), num) && contains(getColumn(y), num) && !squareContains(getSquare(x,y),num);
        }

        public int[] getRow(int rowIndex) {
            int[] row = new int[size];
                for (int colIndex = 0; colIndex < size; colIndex++) {
                row[colIndex] = board[rowIndex][colIndex];
            }
            return row;
        }
        public int[] getColumn(int colIndex) {
            int[] column = new int[size];
            for (int rowIndex = 0; rowIndex < size; rowIndex++) {
                column[rowIndex] = board[rowIndex][colIndex];
            }
            return column;
        }

        public boolean contains (int [] arr, int num) {
            for (int j : arr) {
                if (j == num) {
                    return false;
                }
            }
            return true;
        }

        public boolean squareContains(int [][] square, int num) {
            for (int[] ints : square) {
                for (int anInt : ints) {
                    if (anInt == num) {
                        System.out.println("Found " + num + " in the array");
                        return true;
                    }
                }
            }
            return false;
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
            for(int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++) {
                    square[i][j] = board[(x / n) * n + i][(y / n) * n + j];
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
            this.board = new int[size][size];
            int numRows = n * k;
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numRows; j++) {
                    board[i][j] = 0;
                }

            }
        }

        public int getNumber(int x, int y) {
            return board[x][y];
        }

        public int[][] getBoard() {
            return board;
        }

        public void setBoard(int[][] tempBoard) {
            this.board = tempBoard;
        }

    }
