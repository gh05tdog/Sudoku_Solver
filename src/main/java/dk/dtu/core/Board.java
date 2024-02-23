    package dk.dtu.core;

    import java.util.ArrayList;

    public class Board {

        private final int n;
        private final int k;

        private final ArrayList<ArrayList<Integer>> board;

        public Board(int n, int k) throws Exception {
            this.n = n;
            this.k = k;

            if (!boardIsPossible(k, n)) {
                throw new Exception("This board is not possible to create");
            }
            this.board = new ArrayList<>();
            int numRows = n * k;
            // Initialize the board with zeros
            for (int i = 0; i < numRows; i++) {
                ArrayList<Integer> row = new ArrayList<>(numRows);
                for (int j = 0; j < numRows; j++) {
                    row.add(0);
                }
                board.add(row);
            }
        }

        private boolean boardIsPossible(int k, int n) {
            return (k * n) <= (n * n);
        }

        public void setNumber(int x, int y, int num) {
            this.board.get(x).set(y, num);
        }

        public boolean validPlace(int x, int y, int num) {
            // Check rows and check square
            return !getRow(x).contains(num) && !getColumn(y).contains(num) && !getSquare(x, y).contains(num);
        }

        private ArrayList<Integer> getRow(int i) {
            return this.board.get(i);
        }

        private ArrayList<Integer> getColumn(int i) {
            ArrayList<Integer> column = new ArrayList<>(this.board.size()); //
            for (ArrayList<Integer> row : board) {
                column.add(row.get(i));
            }
            return column;
        }

        private ArrayList<Integer> getSquare(int x, int y) {
            ArrayList<Integer> square = new ArrayList<>();
            int squareRowStart = (x / n) * n;
            int squareColStart = (y / n) * n;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int currentX = squareRowStart + i;
                    int currentY = squareColStart + j;
                    square.add(this.board.get(currentX).get(currentY));
                }
            }
            return square;
        }

        public void printBoard() {
            for (ArrayList<Integer> row : board) {
                for (int cell : row) {
                    System.out.print(cell + " ");
                }
                System.out.println();
            }
        }

        public int getDimensions(){
            return n * k;
        }

        public int getNumber(int x, int y) {
            return board.get(x).get(y);
        }
    }
