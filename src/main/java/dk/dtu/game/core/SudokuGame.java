package dk.dtu.game.core;

import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
import dk.dtu.game.solver.solverAlgorithm;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class SudokuGame {
    private final WindowManager windowManager;
    private boolean running = true;
    int gridSize; // or 9 for a standard Sudoku
    int cellSize; // Adjust based on your window size and desired grid size
    private final Board gameboard;
    MouseActionListener mouseActionListener = new MouseActionListener(this);
    KeyboardListener keyboardListener = new KeyboardListener(this);
    Creater creater = new Creater();
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

        System.out.println("Row: " + row + ", Column: " + column);

        // Check if the adjusted click is within the bounds of the Sudoku board
        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            int cellIndex = row * gridSize + column + 1; // Calculate the cell index
            System.out.println("Cell " + cellIndex + " clicked. Row: " + (row + 1) + ", Column: " + (column + 1));
            board.removeNumber(row, column);
            board.highlightCell(row, column);
            System.out.println("highlighted cell: " + Arrays.toString(board.getHightligtedCell()));

        } else {
            System.out.println("Click outside the Sudoku board or on another component");
        }
    }


    public void typeNumberWithKeyboard(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (Character.isDigit(keyChar)) { // Check if the key character is a digit
            int number = keyChar - '0'; // Convert key character to its integer value

            if (board.isACellHighligthed()) {
                int[] cell = board.getHightligtedCell();
                int row = cell[0];
                int col = cell[1];
                if(gameboard.validPlace(row, col, number) && initialBoard[row][col] == 0){
                    board.removeNumber(row, col); // Assuming (col, row) are the correct order for your logic
                    gameboard.setNumber(row, col, number); // Update the cell with the new number
                    gameboard.printBoard();
                }
            }
        }
    }

    private void eraseNumber(){
        if (board.isACellHighligthed()) {
            int[] cell = board.getHightligtedCell();
            int row = cell[0];
            int col = cell[1];
            if(initialBoard[row][col] == 0){
                board.removeNumber(row, col); // Assuming (col, row) are the correct order for your logic
                gameboard.setNumber(row, col, 0); // Update the cell with the new number
            }
        }
    }

    public void displayNumbersVisually(){
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                int number = gameboard.getNumber(row, col);
                board.drawNumber(row, col, number, board.getGraphics());
            }
        }
    }

    public void createBoard(int n, int k, int cellSize) throws Exception {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50,50);
        board.setFocusable(true);

        board.addMouseListener(mouseActionListener);

        board.addKeyListener(keyboardListener);
        creater.createSudoku2(gameboard);
        solverAlgorithm.printBoard(gameboard.getBoard());
        initialBoard = deepCopyBoard(gameboard.getBoard());

    }

    public void initialize(int n, int k, int cellSize) throws Exception {
        createBoard(n, k, cellSize);
        displayButtons();
        windowManager.drawComponent(board);
    }

    private void newGame() throws Exception {
        gameboard.clear();
        creater.createSudoku2(gameboard);
        initialBoard = deepCopyBoard(gameboard.getBoard());
        gameIsStarted = true;
    }

    private JButton createButton(String text, int x, int y, int width, int height){
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void displayButtons(){
        JButton startButton = createButton("Start", 600, 100, 100, 30);
        JButton restartButton = createButton("Restart", 600, 150, 100, 30);
        JButton solveButton = createButton("Solve", 600, 200, 100, 30);
        JButton newGameButton = createButton("New Game", 600, 250, 100, 30);
        JButton eraseButton = createButton("Erase", (board.getWidth()/2 + board.getX()) - 50 , board.getY()-35, 100, 30);

        startButton.addActionListener(e -> {
            System.out.println("Start game!");
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
            boardArray = solver.solve(boardArray);
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

        windowManager.drawComponent(startButton);
        windowManager.drawComponent(restartButton);
        windowManager.drawComponent(solveButton);
        windowManager.drawComponent(newGameButton);
        windowManager.drawComponent(eraseButton);
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
