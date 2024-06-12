package dk.dtu.engine.graphics;

import dk.dtu.engine.utility.JSwitchBox;
import dk.dtu.engine.utility.NumberDocumentFilter;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.StartMenu;

import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

public class GameRulePopup extends JFrame {

    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private static Color backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background color
    private static final Color accentColor = new Color(237, 224, 186);
    private final StartMenu startMenu;

    public GameRulePopup(StartMenu startMenu) {
        super("Game Rules");
        this.startMenu = startMenu;
        if (!GraphicsEnvironment.isHeadless()) {
            backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE;
            initialize();
        }
    }

    private void initialize() {
        setSize(500, 500); // Increased window size
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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add label
        JLabel label = new JLabel(description);
        if (Config.getDarkMode()) {
            label.setForeground(accentColor);
        }

        gbc.gridy = getContentPane().getComponentCount();
        gbc.gridx = 0;
        gbc.gridwidth = 3; // Span across multiple columns
        add(label, gbc);

        // Add switch box
        JSwitchBox switchBox = new JSwitchBox(initialState, state -> {
            toggleAction.accept(state);
            if (description.equalsIgnoreCase("Dark Mode")) {
                startMenu.updateColors();
            }
        });
        gbc.gridy = getContentPane().getComponentCount();
        gbc.gridx = 1;
        gbc.gridwidth = 1; // Reset gridwidth

        // Check if the description is "Dark Mode"
        if (description.equalsIgnoreCase("Dark Mode")) {
            // Load sun and moon images
            ImageIcon sunIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/sun_symbol.png")));
            ImageIcon moonIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/moon_symbol.png")));

            // Scale the images
            Image sunImage = sunIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            Image moonImage = moonIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);

            // Create new ImageIcon objects
            sunIcon = new ImageIcon(sunImage);
            moonIcon = new ImageIcon(moonImage);

            // Add sun icon to the left
            JLabel sunLabel = new JLabel(sunIcon);
            gbc.gridx = 0;
            add(sunLabel, gbc);

            // Add switch box in the middle
            gbc.gridx = 1;
            add(switchBox, gbc);

            // Add moon icon to the right
            JLabel moonLabel = new JLabel(moonIcon);
            gbc.gridx = 2;
            add(moonLabel, gbc);
        } else {
            // Add switch box normally if not "Dark Mode"
            gbc.gridx = 1;
            add(switchBox, gbc);
        }

        // If the description is "Enable lives", add a JTextField next to the switch box
        if (description.equalsIgnoreCase("Enable lives")) {
            gbc.gridx = 2; // Align JTextField properly
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

        if (Config.getDarkMode()) {
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
