package dk.dtu.engine.core;

import javax.swing.*;
import java.awt.*;

public class StartMenuWindowManager {

    private static final String TITLE = "Sudoku Game";

    private final JFrame frame = new JFrame(TITLE);
    private final JPanel mainPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for more control

    private final JPanel whitePanel = new JPanel(new GridBagLayout()); // Create a new JPanel for the Sudoku board
    private final JPanel buttonPanel = new JPanel(); // Panel for buttons

    public StartMenuWindowManager(int width, int height) {
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


        whitePanel.setBackground(Color.WHITE);
        whitePanel.setLayout(new GridBagLayout()); // GridBagLayout to center the board


        // Add the white panel to the main panel
        mainPanel.add(whitePanel, new GridBagConstraints());

        mainPanel.add(buttonPanel);

        frame.setContentPane(mainPanel); // Add the main panel to the frame
    }


    public void addComponentToButtonPanel(Component component) {
        // Adds a component (like a button) to the button panel
        buttonPanel.add(component);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    public void display() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
    }


}
