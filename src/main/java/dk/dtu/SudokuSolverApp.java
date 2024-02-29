package dk.dtu;

import dk.dtu.game.core.Game;
import dk.dtu.engine.gui.WindowManager;

class SudokuSolverApp {
    public static void main(String[] args) throws Exception {
        WindowManager windowManager = new WindowManager(800, 800);
        int n = 3;
        int k = 3;
        int cellSize = 50;
        Game game = new Game(windowManager, n, k, cellSize);


        game.createBoard(n,k,cellSize);

        windowManager.display(); // Show the window

        game.start(); // Start the game loop
    }
}