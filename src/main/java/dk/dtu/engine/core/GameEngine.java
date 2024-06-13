/* (C)2024 */
package dk.dtu.engine.core;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.SudokuGame;


/**
 * The GameEngine class is responsible for running the game loop. It updates the game logic and
 * renders the game at a fixed rate. The game loop is run in a separate thread to avoid blocking the
 * main thread.
 */
public class GameEngine {
    private final WindowManager windowManager;
    private SudokuGame sudokuGame;
    private boolean running = false;
    private final int n;
    private final int k;
    private final int cellSize;

    public GameEngine(WindowManager windowManager, int n, int k, int cellSize)
            throws Board.BoardNotCreatable {
        this.n = n;
        this.k = k;
        this.cellSize = cellSize;
        this.windowManager = windowManager;
        try {
            this.sudokuGame = new SudokuGame(this.windowManager, n, k, cellSize);
        } catch (Board.BoardNotCreatable boardNotCreatable) {
            throw new Board.BoardNotCreatable("This board is not possible to create");
        }
    }


    private void initialize() {
        windowManager.display(); // Show the game window
        sudokuGame.initialize(this.n, this.k, this.cellSize); // Initialize game-specific components
    }


    // Start the game loop
    public void start() {
        if (running) return;
        running = true;

        initialize();
    }

    // Stop the game loop for the custom board
    public void startCustom(int[][] customBoard) throws Board.BoardNotCreatable {
        if (running) return;
        running = true;
        sudokuGame =
                new SudokuGame(windowManager, Config.getN(), Config.getK(), Config.getCellSize());
        sudokuGame.initializeCustom(customBoard);
        windowManager.display();

    }

    public void startCustomSaved(int[][] initialBoard, int[][] currentBoard, int time, int usedLifeLines, int n, int k, int[][] cages, boolean isKillerSudoku, String notes) throws Board.BoardNotCreatable {
        if (running) return;
        running = true;
        sudokuGame =
                new SudokuGame(windowManager, n, k, Config.getCellSize());
        sudokuGame.initializeCustomSaved(initialBoard, currentBoard, time, usedLifeLines, cages, isKillerSudoku, notes);
        windowManager.display();

    }
}