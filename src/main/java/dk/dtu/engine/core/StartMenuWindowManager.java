package dk.dtu.engine.core;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;

public class StartMenuWindowManager {

    private static final String TITLE = "Sudoku Game";
    private final JFrame frame;
    private final JPanel buttonPanel = new JPanel(null); // Panel for buttons
    private final JPanel difficultyPanel = new JPanel(null); // Panel for difficulty buttons
    private final JPanel sizePanel = new JPanel(null); // Panel for size buttons
    private final JPanel inputPanel = new JPanel(null); // Panel for input buttons

    public StartMenuWindowManager(JFrame Frame, int width, int height) {

        this.frame = Objects.requireNonNullElseGet(Frame, () -> new JFrame(TITLE));
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setResizable(false);
        this.frame.getContentPane().setBackground(Color.WHITE);

        buttonPanel.setOpaque(true);
        sizePanel.setOpaque(true);
        // Use GridBagLayout for more control
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(true);
        difficultyPanel.setOpaque(true);
        inputPanel.setOpaque(true);
        // Panel for fullscreen and settings buttons
        JPanel fullscreenSettingsPanel = new JPanel(null);
        fullscreenSettingsPanel.setOpaque(true);

        mainPanel.setBackground(Color.WHITE);

        sizePanel.setBounds(50, (frame.getHeight() / 2) - 150, 650, 160);
        sizePanel.setBackground(Color.WHITE);

        difficultyPanel.setBounds(50, (frame.getHeight() / 2) + 50, 650, 50);
        difficultyPanel.setBackground(Color.WHITE);

        fullscreenSettingsPanel.setBackground(Color.WHITE);
        fullscreenSettingsPanel.setBounds(10, 10, 150, 150);
        mainPanel.add(fullscreenSettingsPanel);
        buttonPanel.setBounds(
                (frame.getWidth()) - 250,
                (frame.getHeight() / 2) - 150,
                200,
                difficultyPanel.getHeight() + sizePanel.getHeight() + 50);
        buttonPanel.setBackground(Color.WHITE);

        inputPanel.setBounds(525, (frame.getHeight() / 2) - 205, 140, 50);
        inputPanel.setBackground(Color.WHITE);

        mainPanel.add(buttonPanel);
        mainPanel.add(sizePanel);
        mainPanel.add(difficultyPanel);
        mainPanel.add(inputPanel);

        JButton settingsButton = new JButton("Settings");
        settingsButton.setBounds(10, 5, 120, 30);
        JButton fullscreenButton = new JButton("Toggle Fullscreen");
        fullscreenButton.setBounds(10, 40, 150, 30);
        fullscreenSettingsPanel.add(settingsButton);
        fullscreenSettingsPanel.add(fullscreenButton);

        settingsButton.addActionListener(e -> showFrameSizeDialog());

        fullscreenButton.addActionListener(e -> toggleFullscreen());

        frame.setContentPane(mainPanel); // Add the main panel to the frame
    }

    private void showFrameSizeDialog() {
        String input = JOptionPane.showInputDialog(frame, "Enter new frame size (width,height):");
        if (input != null && input.matches("\\d+,\\d+")) {
            String[] parts = input.split(",");
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            frame.setSize(width, height);
        } else {
            JOptionPane.showMessageDialog(
                    frame, "Invalid input. Please enter two numbers separated by a comma.");
        }
    }

    public void toggleFullscreen() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        if (device.isFullScreenSupported()) {
            if (frame.isUndecorated()) {
                device.setFullScreenWindow(null);
                frame.dispose();
                frame.setUndecorated(false);
                frame.setSize(1000, 700);
                frame.setResizable(false);
                frame.setVisible(true);
            } else {
                frame.dispose();
                frame.setUndecorated(true);
                device.setFullScreenWindow(frame);
                frame.setResizable(false);
            }
            repositionComponents();
        }
    }

    private void repositionComponents() {
        int width = frame.getWidth();
        int height = frame.getHeight();
        sizePanel.setLocation((width - sizePanel.getWidth()) / 2, (height / 2) - 150);
        difficultyPanel.setLocation((width - difficultyPanel.getWidth()) / 2, (height / 2) + 50);
        inputPanel.setLocation((width - inputPanel.getWidth()) / 2, (height / 2) - 205);
        buttonPanel.setLocation(width - 210, 10); // Always at the top-left
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

    public JPanel getSizePanel() {
        return sizePanel;
    }

    public JPanel getInputPanel() {
        return inputPanel;
    }
}
