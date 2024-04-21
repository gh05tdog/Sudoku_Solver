package dk.dtu.engine.core;

import javax.swing.*;
import java.awt.*;

public class StartMenuWindowManager {

    private static final String TITLE = "Sudoku Game";

    private final JFrame frame = new JFrame(TITLE);
    private final JPanel mainPanel = new JPanel(null); // Use GridBagLayout for more control
    private final JPanel buttonPanel = new JPanel(null); // Panel for buttons
    private final JPanel difficultyPanel = new JPanel(null); // Panel for difficulty buttons
    private final JPanel sizePanel = new JPanel(null); // Panel for size buttons
    private final JPanel inputPanel = new JPanel(null); // Panel for input buttons


    public StartMenuWindowManager(int width, int height) {
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.WHITE);

        buttonPanel.setOpaque(true);
        sizePanel.setOpaque(true);
        mainPanel.setOpaque(true);
        difficultyPanel.setOpaque(true);
        inputPanel.setOpaque(true);

        mainPanel.setBackground(Color.WHITE);


        sizePanel.setBounds(50,(frame.getHeight()/2)-150, 650, 160);
        sizePanel.setBackground(Color.WHITE);

        difficultyPanel.setBounds(50,(frame.getHeight()/2)+50, 650, 50);
        difficultyPanel.setBackground(Color.WHITE);

        buttonPanel.setBounds((frame.getWidth())-250,(frame.getHeight()/2)-150, 200, difficultyPanel.getHeight()+sizePanel.getHeight()+50 ) ;
        buttonPanel.setBackground(Color.WHITE);

        inputPanel.setBounds(525,(frame.getHeight()/2)-205, 140, 50);
        inputPanel.setBackground(Color.WHITE);

        mainPanel.add(buttonPanel);
        mainPanel.add(sizePanel);
        mainPanel.add(difficultyPanel);
        mainPanel.add(inputPanel);


        frame.setContentPane(mainPanel); // Add the main panel to the frame
    }


    public void addComponent(Component component,JPanel panel) {
        panel.add(component);
        panel.revalidate();
        panel.repaint();
    }

    public void display() {
        frame.setVisible(true);
    }

    public void close() {
        frame.setVisible(false);
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
