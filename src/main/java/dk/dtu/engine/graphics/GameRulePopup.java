package dk.dtu.engine.graphics;

import dk.dtu.engine.utility.JSwitchBox;
import dk.dtu.engine.utility.NumberDocumentFilter;
import dk.dtu.game.core.Config;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

public class GameRulePopup extends JFrame {

    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private static final Color backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background color
    private static final Color accentColor = new Color(237, 224, 186);

    public GameRulePopup() {
        super("Game Rules");
        if (!GraphicsEnvironment.isHeadless()) {
            initialize();
        }

    }

    private void initialize() {
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(backgroundColor);
    }

    public void addJSwitchBox(String description, boolean initialState, Consumer<Boolean> toggleAction) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add label
        JLabel label = new JLabel(description);
        if(Config.getDarkMode()){
            label.setForeground(accentColor);
        }

        gbc.gridy = getContentPane().getComponentCount();
        add(label, gbc);

        // Add switch box
        JSwitchBox switchBox = new JSwitchBox(initialState, toggleAction);
        gbc.gridy = getContentPane().getComponentCount();
        gbc.insets = new Insets(0, 5, 10, 5); // Adjust spacing for better visual separation
        add(switchBox, gbc);

        // If the description is "Enable lives", add a JTextField next to the switch box
        if (description.equalsIgnoreCase("Enable lives")) {
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 5, 10, 5); // Adjust spacing for better visual separation
            JTextField livesField = createLivesField();
            add(livesField, gbc);
        }

        revalidate();
        repaint();
    }

    private JTextField createLivesField() {
        JTextField livesField = new JTextField(1);
        livesField.setBackground(backgroundColor);
        livesField.setText(String.valueOf(Config.getNumberOfLives()));
        livesField.setPreferredSize(new Dimension(30, 30)); // Make the box a square

        if(Config.getDarkMode()){
            livesField.setForeground(accentColor);
        }

        // Apply the NumberDocumentFilter to restrict input to one digit only
        ((AbstractDocument) livesField.getDocument()).setDocumentFilter(new NumberDocumentFilter());

        // Add a DocumentListener to update the number of lives in the Config
        livesField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateNumberOfLives();
            }

            public void removeUpdate(DocumentEvent e) {
                updateNumberOfLives();
            }

            public void changedUpdate(DocumentEvent e) {
                updateNumberOfLives();
            }

            private void updateNumberOfLives() {
                String text = livesField.getText();
                if (!text.isEmpty() && text.matches("[1-9]")) {
                    Config.setNumberOfLives(Integer.parseInt(text));
                }
            }
        });

        return livesField;
    }
}
