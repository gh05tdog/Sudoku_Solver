/* (C)2024 */
package dk.dtu.engine.graphics;

import dk.dtu.engine.utility.JSwitchBox;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

public class GameRulePopup extends JFrame {

    public GameRulePopup() {
        super("Game Rules");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
    }

    public void addJSwitchBox(
            String description, boolean initialState, Consumer<Boolean> toggleAction) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add label
        JLabel label = new JLabel(description);
        gbc.gridy = getContentPane().getComponentCount();
        add(label, gbc);

        // Add switch box
        JSwitchBox switchBox = new JSwitchBox(initialState, toggleAction);
        gbc.gridy = getContentPane().getComponentCount();
        gbc.insets = new Insets(0, 5, 10, 5); // Adjust spacing for better visual separation
        add(switchBox, gbc);

        revalidate();
        repaint();
    }
}
