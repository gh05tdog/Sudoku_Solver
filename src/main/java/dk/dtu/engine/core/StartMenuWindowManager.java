/* (C)2024 */
package dk.dtu.engine.core;

import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.game.core.Config;
import java.awt.*;
import javax.swing.*;

public class StartMenuWindowManager {
    private final JFrame frame;
    private final JPanel buttonPanel = new JPanel(null); // Panel for buttons
    private final JPanel sizePanel = new JPanel(null); // Panel for size buttons
    private final JPanel inputPanel = new JPanel(null); // Panel for input buttons
    private final JPanel welcomePanel = new JPanel(null); // Panel for welcome message
    private final JPanel mainPanel = new JPanel(null);
    private static Color backgroundColor =
            Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;

    // Add references to custom board panels
    private CustomBoardPanel[] customBoardPanels;

    public StartMenuWindowManager(JFrame frame, int width, int height) {
        this.frame = frame;
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setResizable(false);
        backgroundColor = Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;

        buttonPanel.setOpaque(true);
        sizePanel.setOpaque(true);
        mainPanel.setOpaque(true);
        inputPanel.setOpaque(true);
        welcomePanel.setOpaque(true);

        mainPanel.setBackground(backgroundColor);

        welcomePanel.setBounds(60, 50, 610, 50 + sizePanel.getHeight() + 430);
        welcomePanel.setBackground(Color.RED);

        sizePanel.setBounds((frame.getWidth()/2)-325, (frame.getHeight() / 2) + 200, 650, 160);
        sizePanel.setBackground(backgroundColor);


        buttonPanel.setBounds(
                (frame.getWidth()) - 250,
                50,
                200,
                50 + sizePanel.getHeight() + 300);
        buttonPanel.setBackground(backgroundColor);

        inputPanel.setBounds(650, (frame.getHeight() / 2) + 150, 140, 50);
        inputPanel.setBackground(backgroundColor);


        mainPanel.add(buttonPanel);
        mainPanel.add(sizePanel);
        mainPanel.add(inputPanel);
        mainPanel.add(welcomePanel);

        frame.setContentPane(mainPanel); // Add the main panel to the frame
    }

    public void setCustomBoardPanels(CustomBoardPanel[] panels) {
        this.customBoardPanels = panels;
    }

    public void addComponent(Component component, JPanel panel) {
        panel.add(component);
        panel.revalidate();
        panel.repaint();
    }

    public void update() {
        backgroundColor = Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;

        mainPanel.setBackground(backgroundColor);
        mainPanel.revalidate();
        mainPanel.repaint();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.revalidate();
        buttonPanel.repaint();
        sizePanel.setBackground(backgroundColor);
        sizePanel.revalidate();
        sizePanel.repaint();
        inputPanel.setBackground(backgroundColor);
        inputPanel.revalidate();
        inputPanel.repaint();

        // Update custom board panels
        if (customBoardPanels != null) {
            for (CustomBoardPanel panel : customBoardPanels) {
                panel.updateBackgroundColor(backgroundColor);
                panel.updateAccentColor();
            }
        }
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


    public JPanel getSizePanel() {
        return sizePanel;
    }

    public JPanel getInputPanel() {
        return inputPanel;
    }
}
