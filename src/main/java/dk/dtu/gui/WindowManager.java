package dk.dtu.gui;

import dk.dtu.listener.MouseActionListener;
import dk.dtu.listener.WindowListener;

import java.awt.*;

public class WindowManager{

    private static final String TITLE = "Sudoku Game";
    private final Frame frame = new Frame(TITLE);

    WindowListener windowListener = new WindowListener();
    private final Panel whitePanel = new Panel(); // Create a white panel to act as the white container

    public WindowManager(int width, int height) {
        frame.setSize(width, height);
        frame.setLayout(null); // We're managing the layout manually
        frame.setResizable(false);
        frame.setBackground(Color.BLUE); // Set the background color of the frame to blue

        whitePanel.setBackground(Color.WHITE); // Set the background color of the white panel to white
        whitePanel.setLayout(null); // Manually managing the layout of the white panel
        whitePanel.setBounds(0, 0, 800, 800); // Set bounds within the frame

        frame.add(whitePanel);

        frame.addWindowListener(windowListener); // Add window listener for closing operation

        frame.setVisible(true);
    }


    public void drawComponent(Component obj) {
        whitePanel.add(obj);
        whitePanel.validate();
        whitePanel.repaint();
    }

    public void updateBoard(){
        //TODO implement
    }
    public void display(){
        frame.setVisible(true);
    }

    public void addMouseListener(MouseActionListener listener){
        whitePanel.addMouseListener(listener);
    }

    public Component getFrame() {
        return frame;
    }
}