package dk.dtu.game.core;

import dk.dtu.engine.core.GameEngine;
import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;

import javax.swing.*;

public class StartMenu {

    private StartMenuWindowManager startMenu;
    public JButton startButton = new JButton("Start Game");


    public StartMenu(StartMenuWindowManager startMenu) {
        this.startMenu = startMenu;
        startMenu.display();
    }


    public void startGame() throws Exception {
        config config = new config(3, 3, 550/(3*3), "extreme");

        WindowManager windowManager = new WindowManager(900, 900);

        int n = config.getN();
        int k = config.getK();
        int cellSize = config.getCellSize();
        // Initialize the GameEngine with the window manager.
        GameEngine gameEngine = new GameEngine(windowManager, n, k, cellSize);

        // Display the window.
        windowManager.display();

        // Start the game loop in the GameEngine.
        gameEngine.start();
        startMenu.close();
    }


    public void addButton() {
        startButton.setBounds(200, 200, 200, 50);
        startButton.addActionListener(e -> {
            try {
                startGame();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        startMenu.addComponentToButtonPanel(startButton);
    }


}
