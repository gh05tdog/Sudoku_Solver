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
public class GameEngine implements Runnable {
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

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1000000000D / 60D; // 60 ticks per second

        long lastTimer = System.currentTimeMillis();
        double delta = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            boolean shouldRender = false;
            while (delta >= 1) {
                sudokuGame.update(); // Update game logic
                delta -= 1;
                shouldRender = true;
            }

            if (shouldRender) {
                sudokuGame.render(); // Render the game
            }

            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
                // One second has passed - could be used to update an FPS counter, etc.
            }
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
        Thread gameThread = new Thread(this);
        initialize();
        gameThread.start();
    }

    // Stop the game loop for the custom board
    public void startCustom(int[][] customBoard) throws Board.BoardNotCreatable {
        if (running) return;
        running = true;
        sudokuGame =
                new SudokuGame(windowManager, Config.getN(), Config.getK(), Config.getCellSize());
        sudokuGame.initializeCustom(customBoard);
        windowManager.display();
        Thread gameThread = new Thread(this);
        gameThread.start();
    }
}
