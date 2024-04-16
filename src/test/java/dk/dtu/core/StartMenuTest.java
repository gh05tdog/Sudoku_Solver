package dk.dtu.core;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

public class StartMenuTest {
    private StartMenu startMenu;
    private StartMenuWindowManager startMenuWindowManager;

    @BeforeEach
    void setUp() {
        startMenuWindowManager = new StartMenuWindowManager(800, 600);
        startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();  // Assuming this properly sets up the environment
    }

    private void simulateTextFieldInput(JTextField textField, String input) {
        Document doc = textField.getDocument();
        try {
            doc.remove(0, doc.getLength());  // Clear existing content
            doc.insertString(0, input, null);  // Insert new text which triggers DocumentListener
        } catch (BadLocationException e) {
            fail("Bad location: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Start Game Testing")
    void testStartGame() throws Exception {
        SwingUtilities.invokeAndWait(() -> startMenu.getStartButton().doClick());
        startMenu.startGame();  // This should probably be triggered by the button click in a real scenario
        assertEquals(3, config.getK());
        assertEquals(3, config.getN());
        assertEquals(550 / (3 * 3), config.getCellSize());
        assertFalse(startMenuWindowManager.getFrame().isVisible());
    }

    @Test
    @DisplayName("Custom Board Panel Testing")
    void testCustomBoardPanelUpdate() {
        startMenu.updateCustomBoardPanel(3, 3);
        assertEquals(3, startMenu.getCustomBoardPanel().getN());
        assertEquals(3, startMenu.getCustomBoardPanel().getK());
        assertNotNull(startMenu.getCustomBoardPanel());
    }

    @Test
    @DisplayName("Input Field Testing")
    void testInputFieldFunctionality() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            simulateTextFieldInput(startMenu.getInputNField(), "4");
            simulateTextFieldInput(startMenu.getInputKField(), "4");
        });
        assertEquals("4", startMenu.getInputNField().getText());
        assertEquals("4", startMenu.getInputKField().getText());
    }

    @Test
    @DisplayName("Button Panel Testing")
    void testButtonPanelFunctionality() throws Exception {
        SwingUtilities.invokeAndWait(() -> startMenu.getStartButton().doClick());
        assertFalse(startMenuWindowManager.getFrame().isVisible());
    }

    @Test
    @DisplayName("Size Panel Testing")
    void testSizePanelFunctionality() {
        assertEquals(3, config.getN());
        assertEquals(3, config.getK());
    }

    @Test
    @DisplayName("Difficulty Panel Testing")
    void testDifficultyPanelFunctionality() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            startMenu.getEasyButton().doClick();
            assertEquals("easy", config.getDifficulty());
            startMenu.getMediumButton().doClick();
            assertEquals("medium", config.getDifficulty());
            startMenu.getHardButton().doClick();
            assertEquals("hard", config.getDifficulty());
            startMenu.getExtremeButton().doClick();
            assertEquals("extreme", config.getDifficulty());
        });
    }

    @Test
    @DisplayName("Initialization Configuration Test")
    void testInitializationConfiguration() {
        assertEquals(3, config.getN());
        assertEquals(3, config.getK());
        assertEquals("medium", config.getDifficulty());
        assertEquals(550 / (3 * 3), config.getCellSize());
    }

    @Test
    @DisplayName("Size Panel Button Click Simulation for 4x4 Button")
    void testSizePanelButtonClicks() throws Exception {
        StartMenuWindowManager startMenuManager = new StartMenuWindowManager(800, 800);
        StartMenu startMenu = new StartMenu(startMenuManager);
        startMenu.initialize(); // Make sure to initialize the components
        // Assertions to check if the size is set to 4x4
        assertEquals(3, config.getN(), "N should be set to 3, at the beginning");
        assertEquals(3, config.getK(), "K should be set to 3, at the beginning");

        // Simulate mouse click on the 4x4 CustomBoardPanel
        SwingUtilities.invokeAndWait(() -> {
            CustomBoardPanel fourByFourPanel = startMenu.getFourByFour();
            fourByFourPanel.getMouseListeners()[0].mouseClicked(new MouseEvent(fourByFourPanel, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 1, false));
        });

        // Assertions to check if the size is set to 4x4
        assertEquals(4, config.getN(), "N should be set to 4");
        assertEquals(4, config.getK(), "K should be set to 4");
    }

    @Test
    @DisplayName("Difficulty Button Toggle Functionality")
    void testDifficultyButtonToggles() throws Exception {
        SwingUtilities.invokeAndWait(() -> startMenu.getMediumButton().doClick());
        assertEquals("medium", config.getDifficulty());
        SwingUtilities.invokeAndWait(() -> startMenu.getHardButton().doClick());
        assertEquals("hard", config.getDifficulty());
    }

    @Test
    @DisplayName("Input Field Invalid Data Handling")
    void testInputFieldInvalidData() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            simulateTextFieldInput(startMenu.getInputNField(), "invalid");
            simulateTextFieldInput(startMenu.getInputKField(), "");
        });
        assertNotEquals("invalid", config.getN());
        assertNotEquals("", config.getK());
    }

}
