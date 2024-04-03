package dk.dtu.game.core;

import dk.dtu.engine.core.GameEngine;
import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;

import javax.swing.*;
import java.awt.*;

public class StartMenu {

    private StartMenuWindowManager startMenu;
    public JButton startButton = new JButton("Start Game");
    public JButton easyButton = new JButton("Easy");
    public JButton mediumButton = new JButton("Medium");
    public JButton hardButton = new JButton("Hard");
    public JButton extremeButton = new JButton("Extreme");


    public StartMenu(StartMenuWindowManager startMenu) {
        this.startMenu = startMenu;
        startMenu.display();
    }


    public void startGame() throws Exception {
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
        startButton.setBounds(startMenu.getCenterX(startMenu.getButtonPanel()),startMenu.getButtonPanel().getHeight()-40,
                startMenu.getPrefferedWidth(startMenu.getButtonPanel()),30);
        addDifficultyButtons();

        startButton.addActionListener(e -> {
            setButtonsToVisible();
        });
        startMenu.addComponentToButtonPanel(startButton);
    }

    public void addDifficultyButtons() {
        easyButton.setVisible(false);
        mediumButton.setVisible(false);
        hardButton.setVisible(false);
        extremeButton.setVisible(false);

        easyButton.setBounds(startMenu.getCenterX(startMenu.getDifficultyPanel()),0,
                startMenu.getPrefferedWidth(startMenu.getDifficultyPanel()),30);
        mediumButton.setBounds(startMenu.getCenterX(startMenu.getDifficultyPanel()),35,
                startMenu.getPrefferedWidth(startMenu.getDifficultyPanel()),30);
        hardButton.setBounds(startMenu.getCenterX(startMenu.getDifficultyPanel()),70,
                startMenu.getPrefferedWidth(startMenu.getDifficultyPanel()),30);
        extremeButton.setBounds(startMenu.getCenterX(startMenu.getDifficultyPanel()),105,
                startMenu.getPrefferedWidth(startMenu.getDifficultyPanel()),30);


        easyButton.addActionListener(e -> {
            new config(3, 3, 550/(3*3), "easy");
            try {
                startGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        mediumButton.addActionListener(e -> {
            new config(3, 3, 550/(3*3), "medium");
            try {
                startGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        hardButton.addActionListener(e -> {
            new config(3, 3, 550/(3*3), "hard");
            try {
                startGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        extremeButton.addActionListener(e -> {
            new config(3, 3, 550/(3*3), "extreme");
            try {
                startGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        startMenu.addDifficultyButtons(easyButton);
        startMenu.addDifficultyButtons(mediumButton);
        startMenu.addDifficultyButtons(hardButton);
        startMenu.addDifficultyButtons(extremeButton);
    }

    public void setButtonsToVisible() {
        startButton.setVisible(true);
        easyButton.setVisible(true);
        mediumButton.setVisible(true);
        hardButton.setVisible(true);
        extremeButton.setVisible(true);
    }



}
