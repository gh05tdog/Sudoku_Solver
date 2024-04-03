package dk.dtu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DropdownButtonExample {
    public static void main(String[] args) {
        // Create a new frame
        JFrame frame = new JFrame("Dropdown Button Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Create a panel
        JPanel panel = new JPanel();
        frame.add(panel);

        // Create a button that will show the dropdown options
        JButton dropdownButton = new JButton("Select Difficulty");

        // Create a popup menu to show when the button is clicked
        JPopupMenu popupMenu = new JPopupMenu();

        // List of dropdown items
        String[] difficulties = {"Easy", "Medium", "Hard", "Extreme"};

        // Create and add menu items to the popup menu
        for (String difficulty : difficulties) {
            JMenuItem item = new JMenuItem(difficulty);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Selected Difficulty: " + difficulty);
                    // Perform action here based on the selected difficulty
                }
            });
            popupMenu.add(item);
        }

        // Add listener to the button to show the popup menu
        dropdownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupMenu.show(dropdownButton, 0, dropdownButton.getHeight());
            }
        });

        // Add button to panel
        panel.add(dropdownButton);

        // Display the frame
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}