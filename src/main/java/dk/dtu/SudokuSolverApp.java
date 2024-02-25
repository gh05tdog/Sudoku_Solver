package dk.dtu;

import dk.dtu.core.Board;
import dk.dtu.core.Game;
import dk.dtu.gui.SudokuBoardCanvas;
import dk.dtu.gui.WindowManager;
import dk.dtu.listener.KeyboardListener;
import dk.dtu.listener.MouseActionListener;

import java.awt.event.*;
import java.awt.*;

class SudokuSolverApp {
    public static void main(String[] args) throws Exception {
        WindowManager windowManager = new WindowManager(800, 800);
        int n = 3;
        int k = 3;
        int cellSize = 50;
        Game game = new Game(windowManager, n, k, cellSize);


        game.createBoard(n,k,cellSize);


        windowManager.display(); // Show the window



        // Create the Sudoku board component



//        Panel container = new Panel();
//        container.setBackground(Color.RED);
//        container.setBounds(50,50,300, 300); // Set the size of the red panel
//        container.addMouseListener(mouseActionListener);
//        windowManager.drawComponent(container);




        game.start(); // Start the game loop
    }
}