package dk.dtu.engine.core;

import dk.dtu.engine.input.MouseActionListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class WindowManager {

    private static final String TITLE = "Sudoku Game";
    private final JFrame frame = new JFrame(TITLE);

    private final JPanel whitePanel = new JPanel(); // Create a white panel to act as the white container
    private final JPanel buttonPanel = new JPanel();

    public WindowManager(int width, int height) {
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        whitePanel.setBackground(Color.WHITE);
        whitePanel.setLayout(new FlowLayout()); // Switched to FlowLayout for demonstration

        frame.add(whitePanel, BorderLayout.CENTER);
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
