package dk.dtu.core;

import dk.dtu.gui.SudokuBoardCanvas;
import dk.dtu.gui.WindowManager;
import dk.dtu.listener.MouseActionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class Game implements Runnable {
    private WindowManager windowManager;
    private boolean running = true;
    int gridSize = 9; // or 9 for a standard Sudoku
    int cellSize = 50; // Adjust based on your window size and desired grid size


    public Game(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public void run() {
        MouseActionListener mouseActionListener = new MouseActionListener(this);
        windowManager.addMouseListener(mouseActionListener);

        Button button = new Button("Click me");
        button.setBounds(600, 100, 100, 30); // Set the size of the button
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Button clicked");
            }
        });
        button.addMouseListener(mouseActionListener);
        windowManager.drawComponent(button);


        SudokuBoardCanvas board = new SudokuBoardCanvas(gridSize, cellSize);
        board.setLocation(50, 50); // Position the board within the white panel as needed
        board.addMouseListener(mouseActionListener);
        windowManager.drawComponent(board);

        while (running) {
            // Game loop logic:
            // - Process input
            // - Update game state
            // - Render updates through WindowManager



            // Sleep for a short duration to control the loop timing
            try {
                Thread.sleep(16); // For a roughly 60Hz game loop
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void onSudokuBoardClicked(int x, int y) {
        // Assume SudokuBoardCanvas is positioned at (boardX, boardY) within its parent container
        int boardX = 50; // X offset of the Sudoku board within the window
        int boardY = 50; // Y offset of the Sudoku board within the window

        // Adjust click coordinates by the position of the SudokuBoardCanvas
        int adjustedX = x - boardX;
        int adjustedY = y - boardY;

        // Calculate the column and row based on the adjusted click location
        int column = adjustedX / cellSize;
        int row = adjustedY / cellSize;

        // Check if the adjusted click is within the bounds of the Sudoku board
        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            int cellIndex = row * gridSize + column + 1; // Calculate the cell index
            System.out.println("Cell " + cellIndex + " clicked. Row: " + (row + 1) + ", Column: " + (column + 1));
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

    // ... other methods for game logic ...
}
