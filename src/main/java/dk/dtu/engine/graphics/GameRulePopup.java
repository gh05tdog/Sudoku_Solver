/* (C)2024 */
package dk.dtu.engine.graphics;

import dk.dtu.engine.utility.JSwitchBox;
import dk.dtu.engine.utility.NumberDocumentFilter;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.StartMenu;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

/**
 * This class is responsible for creating a popup window that displays the game rules.
 */
public class GameRulePopup extends JFrame {
    private final Color darkModebackgroundColor = new Color(64, 64, 64);
    private Color backgroundColor =
            Config.getDarkMode()
                    ? darkModebackgroundColor
                    : Color.WHITE; // Default background color
    private static final Color lightAccentColor = new Color(237, 224, 186);
    private Color accentColor = Config.getDarkMode() ? lightAccentColor : Color.BLACK;
    private final transient StartMenu startMenu;
    private final List<JLabel> labels = new ArrayList<>();
    JTextField livesField = new JTextField(1);

    public GameRulePopup(StartMenu startMenu) {
        super("Game Rules");
        this.startMenu = startMenu;
        if (!GraphicsEnvironment.isHeadless()) {
            initialize();
        }
    }

    private void initialize() {
        backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE;
        accentColor = Config.getDarkMode() ? lightAccentColor : Color.BLACK;

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(backgroundColor);
    }

    public void addJSwitchBox(
            String description, boolean initialState, Consumer<Boolean> toggleAction) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel label = new JLabel(description);
        labels.add(label);
        label.setForeground(accentColor);

        gbc.gridy = getContentPane().getComponentCount();
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        add(label, gbc);

        JSwitchBox switchBox =
                new JSwitchBox(
                        initialState,
                        state -> {
                            toggleAction.accept(state);
                            if (description.equalsIgnoreCase("Dark Mode")) {
                                startMenu.updateColors();
                                update();
                            }
                        });
        gbc.gridy = getContentPane().getComponentCount();
        gbc.gridx = 1;
        gbc.gridwidth = 1;

        if (description.equalsIgnoreCase("Dark Mode")) {
            ImageIcon sunIcon =
                    new ImageIcon(
                            Objects.requireNonNull(getClass().getResource("/sun_symbol.png")));
            ImageIcon moonIcon =
                    new ImageIcon(
                            Objects.requireNonNull(getClass().getResource("/moon_symbol.png")));

            Image sunImage = sunIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            Image moonImage = moonIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);

            sunIcon = new ImageIcon(sunImage);
            moonIcon = new ImageIcon(moonImage);

            JLabel sunLabel = new JLabel(sunIcon);
            gbc.gridx = 0;
            add(sunLabel, gbc);

            gbc.gridx = 1;
            add(switchBox, gbc);

            JLabel moonLabel = new JLabel(moonIcon);
            gbc.gridx = 2;
            add(moonLabel, gbc);
        } else {
            add(switchBox, gbc);
        }

        if (description.equalsIgnoreCase("Enable lives")) {
            gbc.gridx = 2; // Align JTextField properly
            gbc.insets = new Insets(0, 5, 10, 5); // Adjust spacing for better visual separation
            JTextField lives = createLivesField();
            add(lives, gbc);
        }

        revalidate();
        repaint();
    }

    private JTextField createLivesField() {
        livesField.setBackground(backgroundColor);
        livesField.setText(String.valueOf(Config.getNumberOfLives()));
        livesField.setPreferredSize(new Dimension(30, 30));

        livesField.setForeground(accentColor);

        ((AbstractDocument) livesField.getDocument()).setDocumentFilter(new NumberDocumentFilter());

        livesField
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {
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

    public void update() {
        backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE;
        accentColor = Config.getDarkMode() ? lightAccentColor : Color.BLACK;
        getContentPane().setBackground(backgroundColor);
        livesField.setBackground(backgroundColor);
        livesField.setForeground(accentColor);

        for (JLabel label : labels) {
            label.setForeground(accentColor);
            label.setBackground(backgroundColor);
        }

        revalidate();
        repaint();
    }
}
