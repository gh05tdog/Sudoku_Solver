/* (C)2024 */
package dk.dtu.engine.core;

import dk.dtu.engine.utility.TimerFunction;
import dk.dtu.game.core.Config;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;


/**
 * The WindowManager class is responsible for managing the game window.
 * It sets up the window and adds components to it.
 * It keeps track visually of the hearts when lives are enabled and makes sure one can add components to panels from the sudoku game.
 */
public class WindowManager {
    private final JFrame frame;
    private final JPanel mainPanel =
            new JPanel(new GridBagLayout()); // Use GridBagLayout for more control
    private final JPanel buttonPanel = new JPanel(); // Panel for buttons
    private final JPanel whitePanel =
            new JPanel(new GridBagLayout()); // Create a new JPanel for the Sudoku board
    JPanel heartsPanel = new JPanel();
    BufferedImage emptyHeartImage;
    ImageIcon emptyHeartIcon;
    BufferedImage heartImage;
    ImageIcon heartIcon;
    JPanel combinedPanel = new JPanel();
    private boolean[] heartStates; // true if the heart is full, false if empty

    private static Color backgroundColor =
            Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;

    public WindowManager(JFrame frame, int width, int height) {
        this.frame = frame;
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setResizable(false);
        whitePanel.setOpaque(true);
        buttonPanel.setOpaque(true);
        mainPanel.setOpaque(true);
        whitePanel.setBackground(backgroundColor);
        buttonPanel.setBackground(backgroundColor);
        mainPanel.setBackground(backgroundColor);

        // Configure the button panel
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center buttons horizontally
        buttonPanel.setBackground(backgroundColor);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.insets = new Insets(10, 70, 10, 0); // Add padding

        whitePanel.setBackground(backgroundColor);
        whitePanel.setLayout(new GridBagLayout()); // GridBagLayout to center the board

        // Add the white panel to the main panel
        mainPanel.add(whitePanel, new GridBagConstraints());

        // Add the button panel below the board
        buttonConstraints.gridy = 1; // Place buttonPanel below the board
        buttonConstraints.weighty = 0; // Don't allow vertical stretching
        mainPanel.add(buttonPanel, buttonConstraints);

        this.frame.setContentPane(mainPanel); // Add the main panel to the frame
        addHeartLabels();
    }

    private void addHeartLabels() {
        heartStates = new boolean[Config.getNumberOfLives()]; // Assuming 5 hearts as maximum
        try {
            heartsPanel.setBackground(backgroundColor);
            heartsPanel.setLayout(
                    new FlowLayout(FlowLayout.LEFT, 5, 0)); // Horizontal layout with small gaps

            if (Config.getEnableLives()) {
                for (int i = 0; i < Config.getNumberOfLives(); i++) {
                    JLabel heartLabel = new JLabel();
                    heartsPanel.add(heartLabel);
                    heartStates[i] = true; // Mark the heart as full
                }
            }

            // Set constraints and add the panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(10, 70, 10, 10);
            whitePanel.add(heartsPanel, gbc);
        } catch (NullPointerException ignored) {
        }
    }

    public void removeHeart() {
        try {
            if (emptyHeartImage == null) {
                emptyHeartImage =
                        ImageIO.read(
                                Objects.requireNonNull(
                                        getClass().getResource("/pixel-frame-0.png")));
                Image scaledEmptyHeartImage =
                        emptyHeartImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                emptyHeartIcon = new ImageIcon(scaledEmptyHeartImage);
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Image not found, check the path.");
        }

        // Find the last "full" heart label and update its icon directly
        int lastIndex = -1;
        for (int i = 0; i < heartStates.length; i++) {
            if (heartStates[i]) { // Check if the heart is marked as full
                lastIndex = i;
            }
        }

        if (lastIndex != -1) {
            Component comp = heartsPanel.getComponent(lastIndex);
            if (comp instanceof JLabel label) {
                label.setIcon(emptyHeartIcon);
                heartStates[lastIndex] = false; // Update state to empty
                System.out.println("Heart emptied at index: " + lastIndex);
            }
        } else {
            System.out.println("No full heart found to replace");
        }

        heartsPanel.revalidate();
        heartsPanel.repaint();
    }

    public void setHeart() {
        try {
            if (heartImage == null) {
                heartImage =
                        ImageIO.read(
                                Objects.requireNonNull(getClass().getResource("/redHeart.png")));
                Image scaledHeartImage = heartImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                heartIcon = new ImageIcon(scaledHeartImage);
            }

        } catch (IOException | NullPointerException e) {
            System.out.println("Image not found, check the path.");
        }

        for (int i = 0; i < heartsPanel.getComponentCount(); i++) {
            Component comp = heartsPanel.getComponent(i);
            if (comp instanceof JLabel label) {
                label.setIcon(heartIcon); // Set each heart to full
                heartStates[i] = true; // Mark the heart as full
            }
        }
        heartsPanel.revalidate();
        heartsPanel.repaint();
    }

    public boolean checkGameOver() {
        if (!Config.getEnableLives()) {
            return false;
        }
        for (boolean heartState : heartStates) {
            if (heartState) {
                return false;
            }
        }
        return true;
    }

    public void addComponentToButtonPanel(Component component) {
        buttonPanel.add(component);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    //add a button to go back to the start menu
    public void addGoBackButton(JButton goBackButton) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx =
                1; // You might need to adjust this depending on how many columns your layout has
        gbc.gridy = 0; // Top row
        gbc.weightx = 1.0; // Take up space horizontally
        gbc.weighty = 0; // No vertical expansion
        gbc.anchor = GridBagConstraints.NORTHEAST; // Anchor the button to the northeast corner
        gbc.insets = new Insets(10, 10, 10, 10); // Add some padding

        mainPanel.add(goBackButton, gbc);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void drawBoard(Component board) {
        // This method is used to add the Sudoku board itself, centered in the boardPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Align component at the grid's center
        gbc.gridy = 1; // Align component at the grid's center (below the heart label)
        gbc.weightx = 0.5; // Give column some weight so the component will be centered
        gbc.weighty = 1; // Give row some weight so the component will be centered
        gbc.fill = GridBagConstraints.BOTH; // Let component fill its display area
        gbc.insets = new Insets(0, 70, 0, 30); // Add padding from the left (100 pixels)

        whitePanel.add(board, gbc);
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void layoutComponents(TimerFunction timer, Component numberHub, JButton goBackButton) {
        JPanel combinedPanel = setupNumberAndTimerPanel(timer, numberHub, goBackButton);

        // Layout the combined panel with the number hub, timer, and go back button
        GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.gridx = 1;
        gbcPanel.gridy = 0;
        gbcPanel.fill = GridBagConstraints.NORTH; // Align to the top of the space
        gbcPanel.insets = new Insets(60, 20, 10, 10); // Adds padding around the combined panel

        mainPanel.add(combinedPanel, gbcPanel);

        frame.setVisible(true);
    }


    // This method is used to set up the panel that contains the number hub and timer
    public JPanel setupNumberAndTimerPanel(
            TimerFunction timer, Component numberHub, JButton goBackButton) {
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setBackground(backgroundColor);
        combinedPanel.setOpaque(false);

        // Add vertical glue to push the components towards the center vertically
        combinedPanel.add(Box.createVerticalGlue());

        // Create a wrapper panel to center the numberHub with padding
        JPanel numberHubWrapper = new JPanel();
        numberHubWrapper.setLayout(new BoxLayout(numberHubWrapper, BoxLayout.X_AXIS));
        numberHubWrapper.setBackground(backgroundColor);
        numberHubWrapper.setOpaque(false);
        numberHubWrapper.setBorder(
                BorderFactory.createEmptyBorder(0, 7, 0, 0)); // Add 10 pixels padding to the left
        numberHubWrapper.add(Box.createHorizontalGlue());
        numberHubWrapper.add(numberHub);
        numberHubWrapper.add(Box.createHorizontalGlue());

        timer.setAlignmentX(Component.CENTER_ALIGNMENT);
        timer.setVisibility(Config.getEnableTimer());
        combinedPanel.add(timer);
        combinedPanel.add(
                Box.createRigidArea(new Dimension(0, 10))); // Space between timer and number hub

        combinedPanel.add(numberHubWrapper);

        combinedPanel.add(
                Box.createRigidArea(
                        new Dimension(0, 10))); // Space between number hub and go back button

        goBackButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        combinedPanel.add(goBackButton);

        // Add vertical glue to push the components towards the center vertically
        combinedPanel.add(Box.createVerticalGlue());

        return combinedPanel;
    }

    public void updateBoard() {
        System.out.println(Config.getDarkMode());
        backgroundColor = Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;
        frame.setBackground(backgroundColor);
        frame.revalidate();
        frame.repaint();
        mainPanel.setBackground(backgroundColor);
        mainPanel.revalidate();
        mainPanel.repaint();
        whitePanel.setBackground(backgroundColor);
        whitePanel.revalidate();
        whitePanel.repaint();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.revalidate();
        buttonPanel.repaint();
        heartsPanel.setBackground(backgroundColor);
        heartsPanel.revalidate();
        heartsPanel.repaint();
    }

    public void display() {
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public int getHearts() {
        int hearts = 0;
        for (boolean heartState : heartStates) {
            if (heartState) {
                hearts++;
            }
        }
        return hearts;
    }

    public void addProgressBar(JProgressBar progressBar, int yPos) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = yPos; // Position set based on yPos
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0); // Add some space around the progress bar

        whitePanel.add(progressBar, gbc); // Add to whitePanel to ensure proper layout
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void setHearts(int usedLifeLines) {
        for (int i = 1; i < usedLifeLines; i++) {
            removeHeart();
        }
    }
}
