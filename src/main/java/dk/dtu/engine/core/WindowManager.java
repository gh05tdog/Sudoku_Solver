package dk.dtu.engine.core;


import dk.dtu.engine.utility.Timer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class WindowManager {
    private static final String TITLE = "Sudoku Game";
    private final JFrame frame = new JFrame(TITLE);
    private final JPanel mainPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for more control
    private final JPanel buttonPanel = new JPanel(); // Panel for buttons
    private final JPanel whitePanel = new JPanel(new GridBagLayout()); // Create a new JPanel for the Sudoku board

    private final JPanel hubPanel = new JPanel(new GridBagLayout()); // Create a new JPanel for the number hub
    public Timer timer;



    public WindowManager(int width, int height) {
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.WHITE);
        whitePanel.setOpaque(true);
        buttonPanel.setOpaque(true);
        mainPanel.setOpaque(true);
        whitePanel.setBackground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);
        mainPanel.setBackground(Color.WHITE);

        // Configure the button panel
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center buttons horizontally
        buttonPanel.setBackground(Color.WHITE);

        GridBagConstraints buttonContraints = new GridBagConstraints();
        buttonContraints.insets = new Insets(10, 0, 10, 0); // Add padding

        whitePanel.setBackground(Color.WHITE);
        whitePanel.setLayout(new GridBagLayout()); // GridBagLayout to center the board



        // Add the white panel to the main panel
        mainPanel.add(whitePanel, new GridBagConstraints());

        // Add the button panel below the board
        buttonContraints.gridy = 1; // Place buttonPanel below the board
        buttonContraints.weighty = 0; // Don't allow vertical stretching
        mainPanel.add(buttonPanel, buttonContraints);

        frame.setContentPane(mainPanel); // Add the main panel to the frame

    }

    public void addComponentToButtonPanel(Component component) {
        // Adds a component (like a button) to the button panel
        buttonPanel.add(component);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    public void drawBoard(Component board) {
        // This method is used to add the Sudoku board itself, centered in the boardPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Align component at the grid's center
        gbc.gridy = 0; // Align component at the grid's center
        gbc.weightx = 0.5; // Give column some weight so component will be centered
        gbc.weighty = 1; // Give row some weight so component will be centered
        gbc.fill = GridBagConstraints.BOTH; // Let component fill its display area

        whitePanel.add(board, gbc);
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void layoutComponents( Timer timer, Component numberHub) {
        JPanel combinedPanel = setupNumberAndTimerPanel(timer, numberHub);

        // Layout the combined panel with the number hub and timer
        GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.gridx = 1;
        gbcPanel.gridy = 0;
        gbcPanel.fill = GridBagConstraints.NORTH; // Align to the top of the space
        gbcPanel.insets = new Insets(60, 20, 10, 10); // Adds padding around the combined panel
        mainPanel.add(combinedPanel, gbcPanel);


        frame.setVisible(true);
    }

    public JPanel setupNumberAndTimerPanel(Timer timer, Component numberHub) {
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setOpaque(false);

        timer.setAlignmentX(Component.CENTER_ALIGNMENT);
        combinedPanel.add(timer);
        combinedPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between timer and number hub

        combinedPanel.add(numberHub);

        return combinedPanel;
    }



    public void updateBoard() {
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void display() {
        frame.setVisible(true);
    }

}

