package dk.dtu.game.core;

class SudokuSolverApp {
<<<<<<< Updated upstream:src/main/java/dk/dtu/SudokuSolverApp.java
    public static void main(String[] args) {
        System.out.println("test");
=======
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
>>>>>>> Stashed changes:src/main/java/dk/dtu/game/core/SudokuSolverApp.java
    }
}