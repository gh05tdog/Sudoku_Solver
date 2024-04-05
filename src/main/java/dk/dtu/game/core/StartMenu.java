package dk.dtu.game.core;

import dk.dtu.engine.core.GameEngine;
import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.utility.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private int[][] boardConfigs = {{2, 2}, {3, 3}, {4, 4}, {3, 3}};
;

    public StartMenu(StartMenuWindowManager startMenu) {
        this.startMenu = startMenu;
        startMenu.display();
    }


    public void startGame() throws Exception {
        System.out.println("startGame:" + config.getK() + " " + config.getN() + " " + config.getDifficulty()+ " " + config.getCellSize());
        int n = config.getN();
        int k = config.getK();
        int cellSize = config.getCellSize();
        WindowManager windowManager = new WindowManager(900, 900);

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

        threeByThree.updateBackgroundColor(Color.GRAY);
        config.setK(3);
        config.setN(3);
        config.setCellSize(550/(config.getK()*config.getN()));

        addChangeListenerToField(inputNField);
        addChangeListenerToField(inputKField);

    }


    private void addChangeListenerToField(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateBoard();
            }
            public void removeUpdate(DocumentEvent e) {
                updateBoard();
            }
            public void insertUpdate(DocumentEvent e) {
                updateBoard();
            }

            // Method to parse the n and k values and update the custom board panel
            private void updateBoard() {
                try {
                    int n = Integer.parseInt(inputNField.getText().trim());
                    int k = Integer.parseInt(inputKField.getText().trim());
                    if(n*k <= n*n){
                        updateCustomBoardPanel(n, k);
                        boardConfigs[3] = new int[]{n, k};
                    }
                } catch (NumberFormatException ex) {
                    // Handle the case where one of the fields is empty or does not contain a valid integer
                    System.out.println("Invalid input: " + ex.getMessage());
                }
            }
        });
    }
    private void addInputPanelButtons() {
        Font fieldFont = new Font("SansSerif", Font.BOLD, 20);

        JTextField[] fields = {inputNField, inputKField};
        String[] initialTexts = {"N", "K"};
        for (int i = 0; i < fields.length; i++) {
            JTextField field = fields[i];
            field.setFont(fieldFont);
            field.setHorizontalAlignment(JTextField.CENTER);
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumberDocumentFilter());

            String initialText = initialTexts[i];
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (field.getText().trim().equalsIgnoreCase(initialText)) {
                        field.setText("");
                    }
                }

            });

            field.setBounds(i == 0 ? 5 : 85, 5, 50, 40);
            startMenu.addComponent(field, startMenu.getInputPanel());
        }
    }



    private void addButtonPanelButtons(){
        startButton.setBounds(5,5, 190, 40);
        startButton.setBackground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                startButton.setBackground(Color.GRAY);
                try {
                    startGame();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            else{
                startButton.setBackground(Color.WHITE);
            }
        });
        startMenu.addComponent(startButton,startMenu.getButtonPanel());
    }

    private void addSizePanelButtons() {
        CustomBoardPanel[] boardPanels = {twoBytwo, threeByThree, fourByFour, customBoardPanel};

        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                CustomBoardPanel source = (CustomBoardPanel) evt.getSource();
                for (int i = 0; i < boardPanels.length; i++) {
                    if (source == boardPanels[i]) {
                        int n = boardConfigs[i][0];
                        int k = boardConfigs[i][1];
                        config.setN(n);
                        config.setK(k);
                        config.setCellSize(550/(n*k));
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
        config.setDifficulty("medium");
        difficultyGroup.add(easyButton);
        difficultyGroup.add(mediumButton);
        difficultyGroup.add(hardButton);
        difficultyGroup.add(extremeButton);

        easyButton.setBackground(Color.WHITE);
        mediumButton.setBackground(Color.WHITE);
        hardButton.setBackground(Color.WHITE);
        extremeButton.setBackground(Color.WHITE);
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
