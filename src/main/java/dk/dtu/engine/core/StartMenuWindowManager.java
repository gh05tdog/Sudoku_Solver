/* (C)2024 */
package dk.dtu.engine.core;

import java.awt.*;
import javax.swing.*;

public class StartMenuWindowManager {
    private final JFrame frame;
    private final JPanel buttonPanel = new JPanel(null); // Panel for buttons
    private final JPanel difficultyPanel = new JPanel(null); // Panel for difficulty buttons
    private final JPanel sizePanel = new JPanel(null); // Panel for size buttons
    private final JPanel inputPanel = new JPanel(null); // Panel for input buttons
    private final JPanel gameRulePanel = new JPanel(null); // Panel for game rules

    public StartMenuWindowManager(JFrame frame, int width, int height) {
        this.frame = frame;
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setResizable(false);

        buttonPanel.setOpaque(true);
        sizePanel.setOpaque(true);
        // Use GridBagLayout for more control
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(true);
        difficultyPanel.setOpaque(true);
        inputPanel.setOpaque(true);

        mainPanel.setBackground(Color.WHITE);

        sizePanel.setBounds(50, (frame.getHeight() / 2) - 150, 650, 160);
        sizePanel.setBackground(Color.WHITE);

        difficultyPanel.setBounds(50, (frame.getHeight() / 2) + 50, 650, 50);
        difficultyPanel.setBackground(Color.WHITE);

        buttonPanel.setBounds(
                (frame.getWidth()) - 250,
                (frame.getHeight() / 2) - 150,
                200,
                difficultyPanel.getHeight() + sizePanel.getHeight() + 50);
        buttonPanel.setBackground(Color.WHITE);

        inputPanel.setBounds(525, (frame.getHeight() / 2) - 205, 140, 50);
        inputPanel.setBackground(Color.WHITE);

        gameRulePanel.setBounds(50, 50, 200, 50);
        gameRulePanel.setBackground(Color.WHITE);

        mainPanel.add(buttonPanel);
        mainPanel.add(sizePanel);
        mainPanel.add(difficultyPanel);
        mainPanel.add(inputPanel);
        mainPanel.add(gameRulePanel);

        frame.setContentPane(mainPanel); // Add the main panel to the frame
    }

    public void addComponent(Component component, JPanel panel) {
        panel.add(component);
        panel.revalidate();
        panel.repaint();
    }

    public void display() {
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }

    public JPanel getDifficultyPanel() {
        return difficultyPanel;
    }
    public JPanel getGameRulePanel() {
        return gameRulePanel;
    }

    public JPanel getSizePanel() {
        return sizePanel;
    }

    public JPanel getInputPanel() {
        return inputPanel;
    }
}
