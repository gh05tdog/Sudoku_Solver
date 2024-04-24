package dk.dtu.engine.core;

import java.awt.*;
import javax.swing.*;

public class WindowManager {
    private final JFrame frame;
    private final JPanel buttonPanel = new JPanel(); // Panel for buttons
    private final JPanel whitePanel =
            new JPanel(new GridBagLayout()); // Create a new JPanel for the Sudoku board

    public WindowManager(JFrame frame) {
        this.frame = frame;
        // frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.WHITE);
        whitePanel.setOpaque(true);
        buttonPanel.setOpaque(true);
        // Use GridBagLayout for more control
        JPanel mainPanel = new JPanel(new GridBagLayout());
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

    public void drawNumbers(Component numbers) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; // Align component at the grid's center
        gbc.gridy = 0; // Align component at the grid's center
        gbc.weightx = 0.5; // Give column some weight so component will be centered
        gbc.weighty = 1; // Give row some weight so component will be centered
        gbc.fill = GridBagConstraints.REMAINDER; // Let component fill its display area
        gbc.insets = new Insets(0, 20, 0, 20); // Add padding

        whitePanel.add(numbers, gbc);
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void updateBoard() {
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void display() {
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }
}
