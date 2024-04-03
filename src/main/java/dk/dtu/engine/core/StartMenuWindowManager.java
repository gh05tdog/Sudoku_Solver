package dk.dtu.engine.core;

import javax.swing.*;
import java.awt.*;

public class StartMenuWindowManager {

    private static final String TITLE = "Sudoku Game";

    private final JFrame frame = new JFrame(TITLE);
    private final JPanel mainPanel = new JPanel(null); // Use GridBagLayout for more control
    private final JPanel buttonPanel = new JPanel(null); // Panel for buttons

    private final JPanel difficultyPanel = new JPanel(null);

    public StartMenuWindowManager(int width, int height) {
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.WHITE);

        buttonPanel.setOpaque(true);
        mainPanel.setOpaque(true);

        buttonPanel.setBackground(Color.WHITE);
        mainPanel.setBackground(Color.WHITE);



        buttonPanel.setBounds((frame.getWidth()/2)-(150/2),(frame.getHeight()/2)-150, 150, 200);
        buttonPanel.setBackground(Color.RED);

        difficultyPanel.setBounds(buttonPanel.getX(), buttonPanel.getY()+205, 150, 200);
        difficultyPanel.setBackground(Color.BLUE);


        mainPanel.add(buttonPanel);
        mainPanel.add(difficultyPanel);

        frame.setContentPane(mainPanel); // Add the main panel to the frame
    }


    public void addComponentToButtonPanel(Component component) {
        buttonPanel.add(component);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }
    public void addDifficultyButtons(JButton button) {
        difficultyPanel.add(button);
        difficultyPanel.revalidate();
        difficultyPanel.repaint();
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


    public int getCenterX(JPanel panel) {
        return (panel.getWidth()/2)-((panel.getWidth()-10)/2);
    }
    public int getPrefferedWidth(JPanel panel) {
        return panel.getWidth()-10;
    }



}
