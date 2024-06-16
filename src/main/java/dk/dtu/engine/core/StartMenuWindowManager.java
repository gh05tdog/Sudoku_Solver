/* (C)2024 */
package dk.dtu.engine.core;

import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.game.core.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The StartMenuWindowManager class is responsible for managing the start menu window.
 * It sets up the window and adds components to it.
 */
public class StartMenuWindowManager {
    private final JFrame frame;
    private final JPanel buttonPanel = new JPanel(null); // Panel for buttons
    private final JPanel sizePanel = new JPanel(null); // Panel for size buttons
    private final JPanel inputPanel = new JPanel(null); // Panel for input buttons
    private final JPanel welcomePanel = new JPanel(null); // Panel for welcome message
    private final JPanel mainPanel = new JPanel(null);
    private static Color backgroundColor =
            Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;
    private static final Logger logger = LoggerFactory.getLogger(StartMenuWindowManager.class);

    // Add references to custom board panels
    private CustomBoardPanel[] customBoardPanels;

    public StartMenuWindowManager(JFrame frame, int width, int height) {
        this.frame = frame;
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setResizable(false);
        setBackgroundColor();

        buttonPanel.setOpaque(true);
        sizePanel.setOpaque(true);
        mainPanel.setOpaque(true);
        inputPanel.setOpaque(true);
        welcomePanel.setOpaque(true);

        mainPanel.setBackground(backgroundColor);

        welcomePanel.setBounds(60, 50, 610, 50 + sizePanel.getHeight() + 430);
        welcomePanel.setBackground(backgroundColor);
        setStartMenuLogo();

        sizePanel.setBounds((frame.getWidth() / 2) - 325, (frame.getHeight() / 2) + 200, 650, 160);
        sizePanel.setBackground(backgroundColor);

        buttonPanel.setBounds((frame.getWidth()) - 400, 80, 200, 50 + sizePanel.getHeight() + 300);
        buttonPanel.setBackground(backgroundColor);

        inputPanel.setBounds(650, (frame.getHeight() / 2) + 150, 140, 50);
        inputPanel.setBackground(backgroundColor);

        // Add the panels to the main panel
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

    public void setStartMenuLogo() {
        try {
            BufferedImage originalImage =
                    ImageIO.read(Objects.requireNonNull(getClass().getResource("/logo.png")));
            int newWidth = 700; // Change this to desired width
            int newHeight = 700; // Change this to desired height

            Image scaledImage =
                    originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            JLabel logo = new JLabel(scaledIcon);

            logo.setBounds(-30, -100, newWidth, newHeight);
            welcomePanel.add(logo);
            welcomePanel.revalidate();
            welcomePanel.repaint();
        } catch (IOException e) {
            logger.error("Failed to load logo image");
        }
    }

    private static void setBackgroundColor() {
        backgroundColor = Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;
    }

    public void update() {
        setBackgroundColor();

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
        welcomePanel.setBackground(backgroundColor);
        welcomePanel.revalidate();
        welcomePanel.repaint();

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
