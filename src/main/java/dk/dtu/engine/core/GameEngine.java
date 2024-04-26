package dk.dtu.engine.core;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.SudokuGame;

public class GameEngine implements Runnable {
    private final WindowManager windowManager;
    private final SudokuGame sudokuGame;
    private boolean running = false;
    private final int n;
    private final int k;
    private final int cellSize;

    public GameEngine(WindowManager windowManager, int n, int k, int cellSize) throws Board.BoardNotCreatable {
        this.n = n;
        this.k = k;
        this.cellSize = cellSize;
        this.windowManager = windowManager; // Set your desired window size
        try {
            this.sudokuGame = new SudokuGame(this.windowManager,n,k,cellSize); // Set your desired game parameters
        } catch (Board.BoardNotCreatable boardNotCreatable) {
           throw new Board.BoardNotCreatable("This board is not possible to create");
        }
    }

    public void start() {
        if (running) return;
        running = true;
        Thread gameThread = new Thread(this);
        gameThread.start();
    }


    @Override
    public void run() {
        initialize();
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
                // One second has passed - could be used to update a FPS counter, etc.
            }
        }
    }

    private void initialize() {
        windowManager.display(); // Show the game window
        sudokuGame.initialize(this.n, this.k, this.cellSize); // Initialize game-specific components
    }

}
