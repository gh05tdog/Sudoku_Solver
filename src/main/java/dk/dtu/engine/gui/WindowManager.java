package dk.dtu.engine.gui;

import dk.dtu.engine.listener.MouseActionListener;

import javax.swing.*;
import java.awt.*;

public class WindowManager {

    private static final String TITLE = "Sudoku Game";
    private final JFrame frame = new JFrame(TITLE);

    private final JPanel whitePanel = new JPanel(); // Create a white panel to act as the white container
    private final JPanel buttonPanel = new JPanel();

    public WindowManager(int width, int height) {
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure the application closes properly
        frame.setResizable(false);
        frame.setLayout(new BorderLayout()); // Use BorderLayout for JFrame

        whitePanel.setBackground(Color.WHITE); // Set the background color of the white panel to white
        whitePanel.setLayout(null); // Use BorderLayout for JPanel


        frame.add(whitePanel, BorderLayout.CENTER); // Add the white panel to the frame's center

        // Add window listener using the default window closing operation set above
    }

    public void drawComponent(Component obj) {
        whitePanel.add(obj);
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

    public void addMouseListener(MouseActionListener listener) {
        whitePanel.addMouseListener(listener);
    }

    public JFrame getFrame() {
        return frame;
    }
}
