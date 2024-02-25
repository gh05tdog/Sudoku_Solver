package dk.dtu.core;

import dk.dtu.gui.SudokuBoardCanvas;
import dk.dtu.gui.WindowManager;
import dk.dtu.listener.KeyboardListener;
import dk.dtu.listener.MouseActionListener;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class Game implements Runnable {
    private WindowManager windowManager;
    private boolean running = true;
    int gridSize; // or 9 for a standard Sudoku
    int cellSize; // Adjust based on your window size and desired grid size
    private final Board gameboard;
    MouseActionListener mouseActionListener = new MouseActionListener(this);
    KeyboardListener keyboardListener = new KeyboardListener(this);
    Creater creater = new Creater();
    private SudokuBoardCanvas board;
    private boolean gameIsStarted = false;
    private boolean isHighlighted = false;



    public Game(WindowManager windowManager,int n, int k,int cellSize) throws Exception {
        this.windowManager = windowManager;
        gameboard = new Board(n, k);
        this.gridSize = n * k;
        this.cellSize = cellSize;
    }

    @Override
    public void run() {

        EventQueue.invokeLater(() -> {
            windowManager.addMouseListener(mouseActionListener);

            Button button = new Button("Click me");
            button.setBounds(600, 100, 100, 30); // Set the size of the button
            button.addActionListener(e -> {
                System.out.println("Start game!");
                displayNumbersVisually();
                gameIsStarted = true;
                board.requestFocusInWindow();
            });
            Button restartButton = new Button("Restart");
            restartButton.setBounds(600, 150, 100, 30); // Set the size of the button
            restartButton.addActionListener(e -> {
                for (int i = 0; i < gameboard.getDimensions(); i++) {
                    for (int j = 0; j < gameboard.getDimensions(); j++) {
                        board.removeNumber(i,j);
                    }
                }
                board.requestFocusInWindow();
            });

            Button solveButton = new Button("Solve");
            solveButton.setBounds(600, 200, 100, 30); // Set the size of the button
            solveButton.addActionListener(e -> {
                board.requestFocusInWindow();
            });

            Button newGame = new Button("New Game");
            newGame.setBounds(600,250,100,30);
            newGame.addActionListener(e ->{
                board.requestFocusInWindow();
                for (int i = 0; i < gameboard.getDimensions(); i++) {
                    for (int j = 0; j < gameboard.getDimensions(); j++) {
                        gameboard.setNumber(i,j,0);
                        board.removeNumber(i,j);
                    }
                }
                creater.createSudoku(gameboard);

            });

            restartButton.addMouseListener(mouseActionListener);
            solveButton.addMouseListener(mouseActionListener);
            button.addMouseListener(mouseActionListener);
            windowManager.drawComponent(button);
            windowManager.drawComponent(restartButton);
            windowManager.drawComponent(solveButton);
            windowManager.drawComponent(newGame);
        });

        while (running) {
            if (gameIsStarted) {
                displayNumbersVisually();
            }





            // Sleep for a short duration to control the loop timing
            try {
                Thread.sleep(16); // For a roughly 60Hz game loop
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void onSudokuBoardClicked(int x, int y) {
        // Calculate the column and row based on the adjusted click location
        int column = x / cellSize;
        int row = y / cellSize;

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


    public void start() {
        new Thread(this).start();
    }

    public void onMouseClicked(int x, int y) {
        System.out.println("Mouse clicked at: " + x + ", " + y);
    }

    public void createBoard(int n, int k, int cellSize) {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50, 50); // Position the board within the white panel as needed
        board.setFocusable(true);
       // windowManager.getFrame().addKeyListener(keyboardListener);

        board.addMouseListener(mouseActionListener);
        windowManager.drawComponent(board);
        board.addKeyListener(keyboardListener);
        creater.createSudoku(gameboard);

    }

    public void displayNumbersVisually(){
        for (int i = 0; i < gameboard.getDimensions(); i++) {
            for (int j = 0; j < gameboard.getDimensions(); j++) {
                int number = gameboard.getRow(i).get(j);
                board.drawNumber(j, i, number, board.getGraphics());
            }
        }
    }

    public void typeNumber(KeyEvent e) {
        char keyChar = e.getKeyChar();

        if (Character.isDigit(keyChar)) { // Check if the key character is a digit
            int number = keyChar - '0'; // Convert key character to its integer value

            if (board.isACellHighligthed()) {
                int[] cell = board.getHightligtedCell();
                int row = cell[0];
                int col = cell[1];
                if(gameboard.validPlace(row, col, number)){
                    board.removeNumber(row, col); // Assuming (col, row) are the correct order for your logic
                    gameboard.setNumber(row, col, number); // Update the cell with the new number
                }



                // After updating the gameboard, you may want to visually update the board to reflect the change
                //board.drawNumber(col, row, number, board.getGraphics()); // Redraw only the changed cell
               // board.repaint(); // You might need to call repaint on a specific cell area or the whole board, depending on your implementation
            }
        }
    }


    // ... other methods for game logic ...
}
