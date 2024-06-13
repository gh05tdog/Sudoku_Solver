/* (C)2024 */
package dk.dtu.engine.core;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.SudokuGame;

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

    public void start() {
        if (running) return;
        running = true;

        initialize();
    }

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