package dk.dtu.game.core;

import dk.dtu.engine.core.GameEngine;
import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.utility.*;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class StartMenu {

    private StartMenuWindowManager startMenu;
    private final JToggleButton startButton = new JToggleButton("Start Game");
    private final JToggleButton easyButton = new JToggleButton("Easy");
    private final JToggleButton mediumButton = new JToggleButton("Medium",true);
    private final JToggleButton hardButton = new JToggleButton("Hard");
    private final JToggleButton extremeButton = new JToggleButton("Extreme");

    private final CustomBoardPanel twoBytwo = new CustomBoardPanel();
    private final CustomBoardPanel threeByThree = new CustomBoardPanel();
    private final CustomBoardPanel fourByFour = new CustomBoardPanel();
    private final CustomBoardPanel customBoardPanel = new CustomBoardPanel();

    private final JTextField inputNField = new JTextField("N",1);
    private final JTextField inputKField = new JTextField("K",1);


    private final ButtonGroup difficultyGroup = new ButtonGroup();
    private final CustomComponentGroup sizeGroup = new CustomComponentGroup();

;



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



    public void updateCustomBoardPanel(int n, int k) {
        if((k * n) <= (n * n)){
            customBoardPanel.updateBoard(n, k); // Update the board based on n and k
        }
        else{
            customBoardPanel.setBackground(Color.WHITE);
        }
    }


    public void initialize(){
        addSizePanelButtons();
        addDifficultyPanelButtons();
        addButtonPanelButtons();
        addInputPanelButtons();
        updateCustomBoardPanel(2,2);
    }


    private void addInputPanelButtons() {
        Font fieldFont = new Font("SansSerif", Font.BOLD, 20);

        // Set properties common to both fields
        JTextField[] fields = {inputNField, inputKField};
        String[] initialTexts = {"N", "K"};
        for (int i = 0; i < fields.length; i++) {
            JTextField field = fields[i];
            field.setFont(fieldFont);
            field.setHorizontalAlignment(JTextField.CENTER);
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumberDocumentFilter());

            int finalI1 = i;
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (field.getText().equals(initialTexts[finalI1])) {
                        field.setText("");
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(initialTexts[finalI1]);
                    }
                }
            });

            field.setBounds(i == 0 ? 5 : 60, 5, 50, 40);
            startMenu.addComponent(field, startMenu.getInputPanel());
        }
    }



    private void addButtonPanelButtons(){
        //TODO: implement
    }

    private void addSizePanelButtons() {
        // Set up an array of the buttons for convenience.
        CustomBoardPanel[] boardPanels = {twoBytwo, threeByThree, fourByFour, customBoardPanel};
        int[][] boardConfigs = {{2, 2}, {3, 3}, {4, 4}, {3, 3}}; // Replace the last pair with the user's input for customBoardPanel
        //boardConfigs[3] = new int[]{Integer.parseInt(inputNField.getText()), Integer.parseInt(inputKField.getText())};

        // Use a single mouse listener for all the panels.
        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                CustomBoardPanel source = (CustomBoardPanel) evt.getSource();
                for (int i = 0; i < boardPanels.length; i++) {
                    if (source == boardPanels[i]) {
                        int n = boardConfigs[i][0];
                        int k = boardConfigs[i][1];
                        config.setN(n);
                        config.setK(k);
                        break;
                    }
                }
            }
        };

        int xPosition = 5;
        for (int i = 0; i < boardPanels.length; i++) {
            int n = boardConfigs[i][0];
            int k = boardConfigs[i][1];

            CustomBoardPanel panel = boardPanels[i];
            panel.updateBoard(n, k); // Initialize with the correct board configuration

            panel.addMouseListener(mouseAdapter); // Add the mouse listener

            panel.setBounds(xPosition, 5, 150, 150);
            xPosition += 150 + 5; // Increment for the next panel

            startMenu.addComponent(panel, startMenu.getSizePanel()); // Add the panel to the size panel
            sizeGroup.addComponent(panel); // Add the panel to the custom component group
        }
    }

    private void addDifficultyPanelButtons(){
        difficultyGroup.add(easyButton);
        difficultyGroup.add(mediumButton);
        difficultyGroup.add(hardButton);
        difficultyGroup.add(extremeButton);

        easyButton.setBackground(Color.WHITE);
        easyButton.setFocusPainted(false);
        mediumButton.setFocusPainted(false);
        hardButton.setFocusPainted(false);
        extremeButton.setFocusPainted(false);

        easyButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                easyButton.setBackground(Color.GRAY);
                config.setDifficulty("easy");
            }
            else{
                easyButton.setBackground(Color.WHITE);
            }
        });
        mediumButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                mediumButton.setBackground(Color.GRAY);
                config.setDifficulty("medium");
            }
            else{
                mediumButton.setBackground(Color.WHITE);
            }
        });
        hardButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                hardButton.setBackground(Color.GRAY);
                config.setDifficulty("hard");
            }
            else{
                hardButton.setBackground(Color.WHITE);
            }
        });
        extremeButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                extremeButton.setBackground(Color.GRAY);
                config.setDifficulty("extreme");
            }
            else{
                extremeButton.setBackground(Color.WHITE);
            }
        });

        easyButton.setBounds(5,5, twoBytwo.getWidth(), 40);
        mediumButton.setBounds(10+easyButton.getWidth(),5,  twoBytwo.getWidth(), 40);
        hardButton.setBounds(15+easyButton.getWidth()+mediumButton.getWidth(),5,  twoBytwo.getWidth(), 40);
        extremeButton.setBounds(20+easyButton.getWidth()+mediumButton.getWidth()+hardButton.getWidth(),5,  twoBytwo.getWidth(), 40);


        startMenu.addComponent(easyButton,startMenu.getDifficultyPanel());
        startMenu.addComponent(mediumButton,startMenu.getDifficultyPanel());
        startMenu.addComponent(hardButton,startMenu.getDifficultyPanel());
        startMenu.addComponent(extremeButton,startMenu.getDifficultyPanel());
    }








}
