package dk.dtu.engine.core;

import dk.dtu.engine.utility.TimerFunction;
import dk.dtu.game.core.Config;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

public class WindowManager {
    private final JFrame frame;
    private final JPanel mainPanel =
            new JPanel(new GridBagLayout()); // Use GridBagLayout for more control
    private final JPanel buttonPanel = new JPanel(); // Panel for buttons
    private final JPanel whitePanel =
            new JPanel(new GridBagLayout()); // Create a new JPanel for the Sudoku board
    JPanel heartsPanel = new JPanel();
    BufferedImage emptyHeartImage = null;
    ImageIcon emptyHeartIcon = null;
    BufferedImage heartImage = null;
    ImageIcon heartIcon = null;

    public WindowManager(JFrame frame, int width, int height) {
        this.frame = frame;
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setResizable(false);
        whitePanel.setOpaque(true);
        buttonPanel.setOpaque(true);
        mainPanel.setOpaque(true);
        whitePanel.setBackground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);
        mainPanel.setBackground(Color.WHITE);

        // Configure the button panel
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center buttons horizontally
        buttonPanel.setBackground(Color.WHITE);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.insets = new Insets(10, 0, 10, 0); // Add padding

        whitePanel.setBackground(Color.WHITE);
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

    private boolean[] heartStates; // true if the heart is full, false if empty

    private void addHeartLabels() {
        heartStates = new boolean[5]; // Assuming 5 hearts as maximum
        try {
            heartsPanel.setBackground(Color.WHITE);
            heartsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Horizontal layout with small gaps

            if (Config.getEnableLives()) {
                for (int i = 0; i < 5; i++) {
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
            gbc.insets = new Insets(10, 10, 10, 10);
            whitePanel.add(heartsPanel, gbc);
            System.out.println("Heart labels added to whitePanel"); // Debug message
        }
        catch (NullPointerException e) {
            System.out.println("Image not found, check the path."); // Debug message
        }
    }


    public void removeHeart(){
        try {
            if (emptyHeartImage == null) {
                emptyHeartImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/pixil-frame-0.png")));
                Image scaledEmptyHeartImage = emptyHeartImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                emptyHeartIcon = new ImageIcon(scaledEmptyHeartImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Image not found, check the path."); // Debug message
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

    public void setHeart(){
        try {
            if (heartImage == null){
                heartImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/redHeart.png")));
                Image scaledHeartImage = heartImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                heartIcon = new ImageIcon(scaledHeartImage);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Image not found, check the path."); // Debug message
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
        for (boolean heartState : heartStates) {
            if (heartState) {
                return false;
            }
        }
        return true;
    }





    public void addComponentToButtonPanel(Component component) {
        // Adds a component (like a button) to the button panel
        buttonPanel.add(component);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

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

        whitePanel.add(board, gbc);
        whitePanel.revalidate();
        whitePanel.repaint();
    }

    public void layoutComponents(TimerFunction timer, Component numberHub) {
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

    public JPanel setupNumberAndTimerPanel(TimerFunction timer, Component numberHub) {
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setOpaque(false);

        timer.setAlignmentX(Component.CENTER_ALIGNMENT);
        if(Config.getEnableTimer()){
            combinedPanel.add(timer);
        }
        combinedPanel.add(
                Box.createRigidArea(new Dimension(0, 10))); // Space between timer and number hub

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

    public JFrame getFrame() {
        return frame;
    }
}
