package dk.dtu.game.core;

class SudokuSolverApp {
    public static void main(String[] args) throws Exception {
        // Set up the window manager and game parameters.
        WindowManager windowManager = new WindowManager(800, 800);
        int n = 5;
        int k = 5;
        int cellSize = 50;
        // Initialize the GameEngine with the window manager.
        GameEngine gameEngine = new GameEngine(windowManager, n, k, cellSize);

        // Display the window.
        windowManager.display();

        // Start the game loop in the GameEngine.
        gameEngine.start();
    }
}