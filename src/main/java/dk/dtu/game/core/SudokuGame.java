package dk.dtu.game.core;

import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
import dk.dtu.game.solver.solverAlgorithm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class SudokuGame {
    private final WindowManager windowManager;
    private boolean running = true;
    int gridSize; // or 9 for a standard Sudoku
    int cellSize; // Adjust based on your window size and desired grid size
    private final Board gameboard;
    MouseActionListener mouseActionListener = new MouseActionListener(this);
    KeyboardListener keyboardListener = new KeyboardListener(this);
    solverAlgorithm solverAlgorithm = new solverAlgorithm();
    private final Stack<Move> moveList = new Stack<>();
    private ArrayList<Integer> arrayMovelist = new ArrayList<>();

    private ArrayList<Move> hintList = new ArrayList<>();

    private SudokuBoardCanvas board;
    private boolean gameIsStarted = false;

    private int [][] initialBoard;

    public SudokuGame(WindowManager windowManager, int n, int k, int cellSize) throws Exception {
        this.windowManager = windowManager;
        gameboard = new Board(n, k);
        this.gridSize = n * k;
        this.cellSize = cellSize;
    }

    public void onSudokuBoardClicked(int x, int y) {
        // Calculate the column and row based on the adjusted click location
        int row = y / (board.getWidth() / gridSize); // Adjust for variable cell size
        int column = x / (board.getHeight() / gridSize); // Adjust for variable cell size
        board.setMarkedCell(row,column);

        System.out.println("Row: " + row + ", Column: " + column);

        // Check if the adjusted click is within the bounds of the Sudoku board
        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            int cellIndex = row * gridSize + column + 1; // Calculate the cell index
            System.out.println("Cell " + cellIndex + " clicked. Row: " + (row + 1) + ", Column: " + (column + 1));
            board.removeNumber(row, column);
            board.highlightCell(row, column,true);
            System.out.println("highlighted cell: " + Arrays.toString(board.getMarkedCell()));

        } else {
            System.out.println("Click outside the Sudoku board or on another component");
        }
    }

    public void typeNumberWithKeyboard(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (Character.isDigit(keyChar)) {
            int number = keyChar - '0'; // Convert character to integer

            int[] markedCell = board.getMarkedCell();
            int row = markedCell[0];
            int col = markedCell[1];
            if (row >= 0 && col >= 0) { // Validate that a cell is indeed highlighted
                if(gameboard.validPlace(row, col, number) && initialBoard[row][col] == 0){
                    int previousNumber = gameboard.getNumber(row, col);
                    board.setCellNumber(row, col, number);
                    gameboard.setNumber(row, col, number);
                    gameboard.printBoard();
                    Move move = moveList.push(new Move(row, col, number, previousNumber));
                    arrayMovelist.add(move.getNumber());
                    System.out.println(Arrays.toString(arrayMovelist.toArray()));
                }
            }
        }
    }

    private void eraseNumber(){
        if (board.isACellHighligthed()) {
            int[] cell = board.getMarkedCell();
            int row = cell[0];
            int col = cell[1];
            if(initialBoard[row][col] == 0){
                board.removeNumber(row, col);
                gameboard.setNumber(row, col, 0);
            }
        }
    }

    public void displayNumbersVisually(){
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                int number = gameboard.getNumber(row, col);
                board.setCellNumber(row, col, number);
            }
        }
    }

    private void undoMove() {
        if (!moveList.isEmpty()) {
            Move move = moveList.pop();
            int row = move.getRow();
            int col = move.getColumn();
            int prevNumber = move.getPreviousNumber();
            gameboard.setNumber(row, col, prevNumber);
            board.setCellNumber(row, col, prevNumber);
            arrayMovelist.remove(arrayMovelist.size()-1);
            System.out.println(Arrays.toString(arrayMovelist.toArray()));
        }
    }



    public void createBoard(int n, int k, int cellSize) throws Exception {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50,50);
        board.setFocusable(true);

        board.addMouseListener(mouseActionListener);

        board.addKeyListener(keyboardListener);

        initialBoard = deepCopyBoard(gameboard.getBoard());
    }

    public void initialize(int n, int k, int cellSize) throws Exception {
        createBoard(n, k, cellSize);
        displayButtons();
        windowManager.drawBoard(board);
    }

    private void newGame() throws Exception {
        gameboard.clear();
        hintList.clear();
        dk.dtu.game.solver.solverAlgorithm.createSudoku(gameboard);
        initialBoard = deepCopyBoard(gameboard.getBoard());
        gameIsStarted = true;
        gameboard.printBoard();
        fillHintList();
        System.out.println(hintList.size());

    }

    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(width, height));
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Align buttons for BoxLayout

        // Create a margin around the button
        int topBottomMargin = 5; // Space above and below the button
        int leftRightMargin = 10; // Space to the left and right of the button
        button.setBorder(BorderFactory.createEmptyBorder(topBottomMargin, leftRightMargin, topBottomMargin, leftRightMargin));

        return button;
    }


    private void fillHintList() {
        int[][] solutionBoard = solverAlgorithm.getSolutionBoard(gameboard.getBoard());

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (initialBoard[row][col] == 0) { // Assuming initialBoard is the puzzle with empty spaces
                    hintList.add(new Move(row, col, solutionBoard[row][col], 0));
                }
            }
        }
    }

    public void provideHint() {
        if (!hintList.isEmpty()) {
            Random random = new Random();
            int hintIndex = random.nextInt(hintList.size());
            Move hintMove = hintList.get(hintIndex);
            hintList.remove(hintIndex);

            int row = hintMove.getRow();
            int col = hintMove.getColumn();
            int number = hintMove.getNumber();


            gameboard.setNumber(row, col, number);
            board.setCellNumber(row, col, number);
            initialBoard[row][col] = number;

        } else {
            System.out.println("No more hints available.");
        }
    }



    private void displayButtons(){
        JButton startButton = createButton("Start",  100, 30);
        JButton restartButton = createButton("Restart",  100, 30);
        JButton solveButton = createButton("Solve",  100, 30);
        JButton newGameButton = createButton("New Game",  100, 30);
        JButton eraseButton = createButton("Erase", 100, 30);
        JButton undoButton = createButton("Undo", 100, 300);
        JButton hintButton = createButton("Hint", 100, 30);

        startButton.addActionListener(e -> {
            System.out.println("Start game!");
            try {
                newGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            displayNumbersVisually();
            gameIsStarted = true;
            board.requestFocusInWindow();
        });
        restartButton.addActionListener(e -> {
            //set the numbers to the initial board
            gameIsStarted = false;
            gameboard.setBoard(deepCopyBoard(initialBoard));
            board.requestFocusInWindow();
            gameIsStarted = true;
            windowManager.updateBoard();
        });


        solveButton.addActionListener(e -> {
            int [][] boardArray = deepCopyBoard(gameboard.getBoard());
            solverAlgorithm solver = new solverAlgorithm();
            solver.sudoku(boardArray);
            gameboard.setBoard(boardArray);

            board.requestFocusInWindow();
        });

        newGameButton.addActionListener(e -> {
            gameIsStarted = false;
            try {
                newGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            board.requestFocusInWindow();

            windowManager.updateBoard();
        });

        eraseButton.addActionListener(e -> {
            board.requestFocusInWindow();
            eraseNumber();
        });

        undoButton.addActionListener(e -> {
            board.requestFocusInWindow();
            undoMove();
        });

        hintButton.addActionListener(e -> {
            board.requestFocusInWindow();
            provideHint();
        });

        windowManager.addComponentToButtonPanel(startButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10))); // 10-pixel vertical spacing
        windowManager.addComponentToButtonPanel(restartButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        windowManager.addComponentToButtonPanel(solveButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        windowManager.addComponentToButtonPanel(newGameButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        windowManager.addComponentToButtonPanel(eraseButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea((new Dimension(10,10))));
        windowManager.addComponentToButtonPanel(undoButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea((new Dimension(10,10))));
        windowManager.addComponentToButtonPanel(hintButton);
    }



    private int [][] deepCopyBoard(int [][] original) {
        int [][] copy = new int [original.length][original.length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original.length);
        }
        return copy;
    }

    public void onMouseClicked(int x, int y) {
        System.out.println("Mouse clicked at: " + x + ", " + y);
    }

    public void render() {
        if(gameIsStarted){
            displayNumbersVisually();
        }

    }

    public void update() {
        if (running) {
            render();
        }
    }
}
