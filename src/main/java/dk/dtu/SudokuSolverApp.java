package dk.dtu;

import dk.dtu.engine.core.GameEngine;
import dk.dtu.engine.core.WindowManager;

class SudokuSolverApp {
    public static void main(String[] args) throws Exception {
        // Set up the window manager and game parameters.
        WindowManager windowManager = new WindowManager(900, 900);
        int n = 3;
        int k = 3;
        int cellSize = 550/(n*k);
        // Initialize the GameEngine with the window manager.
        GameEngine gameEngine = new GameEngine(windowManager, n, k, cellSize);

        // Display the window.
        windowManager.display();

        // Start the game loop in the GameEngine.
        gameEngine.start();
    }
}
