/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import dk.dtu.game.core.StartMenu;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StartMenuTest {
    private StartMenu startMenu;

    @BeforeEach
    void setUp() {
        JFrame mockedFrame = mock(JFrame.class);
        StartMenuWindowManager startMenuWindowManager =
                new StartMenuWindowManager(mockedFrame, 800, 600);
        startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();
    }

    private void simulateTextFieldInput(JTextField textField, String input) {
        // helper function to test input fields N and K
        Document doc = textField.getDocument();
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, input, null);
        } catch (BadLocationException e) {
            fail("Bad location: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Start Game Testing")
    void testStartGame()
            throws Board.BoardNotCreatable, InterruptedException, InvocationTargetException {
        // Testing the start game button, and if the start menu closes and the difficulties + size
        // is
        // the correct
        SwingUtilities.invokeAndWait(() -> startMenu.getStartButton().doClick());
        startMenu.startGame();
        assertEquals(3, Config.getK());
        assertEquals(3, Config.getN());
        assertEquals(550 / (3 * 3), Config.getCellSize());
    }

    @Test
    @DisplayName("Custom Board Panel Testing")
    void testCustomBoardPanelUpdate() {
        // Tests if the custom board actually updates the n and k values
        startMenu.updateCustomBoardPanel(3, 3);
        assertEquals(3, startMenu.getCustomBoardPanel().getN());
        assertEquals(3, startMenu.getCustomBoardPanel().getK());
        assertNotNull(startMenu.getCustomBoardPanel());
    }

    @Test
    @DisplayName("Input Field Testing")
    void testInputFieldFunctionality() throws Exception {
        // Tests if the input fields are working correctly
        SwingUtilities.invokeAndWait(
                () -> {
                    simulateTextFieldInput(startMenu.getInputNField(), "4");
                    simulateTextFieldInput(startMenu.getInputKField(), "4");
                });
        assertEquals("4", startMenu.getInputNField().getText());
        assertEquals("4", startMenu.getInputKField().getText());
    }

    @Test
    @DisplayName("Button Panel Testing")
    void testButtonPanelFunctionality() throws Exception {
        // Testing the start button
        SwingUtilities.invokeAndWait(() -> startMenu.getStartButton().doClick());
        // Assert the start button is working correctly
        assertEquals(3, Config.getN());
        assertEquals(3, Config.getK());
        assertEquals("medium", Config.getDifficulty());
    }

    @Test
    @DisplayName("Size Panel Testing")
    void testSizePanelFunctionality() {
        // Tests if the size panel is working correctly
        assertEquals(3, Config.getN());
        assertEquals(3, Config.getK());
    }

    @Test
    @DisplayName("Difficulty Panel Testing")
    void testDifficultyPanelFunctionality() throws Exception {
        // Tests if the difficulty panel is working correctly
        SwingUtilities.invokeAndWait(
                () -> {
                    JComboBox<String> difficultyDropdown = startMenu.getDifficultyDropdown();

                    difficultyDropdown.setSelectedItem("Easy");
                    assertEquals("easy", Config.getDifficulty());

                    difficultyDropdown.setSelectedItem("Medium");
                    assertEquals("medium", Config.getDifficulty());

                    difficultyDropdown.setSelectedItem("Hard");
                    assertEquals("hard", Config.getDifficulty());

                    difficultyDropdown.setSelectedItem("Extreme");
                    assertEquals("extreme", Config.getDifficulty());
                });
    }

    @Test
    @DisplayName("Initialization Configuration Test")
    void testInitializationConfiguration() {
        // Tests if the configuration is initialized correctly
        assertEquals(3, Config.getN());
        assertEquals(3, Config.getK());
        assertEquals("medium", Config.getDifficulty());
        assertEquals(550 / (3 * 3), Config.getCellSize());
    }

    @Test
    @DisplayName("Size Panel Button Click Simulation for 4x4 Button")
    void testSizePanelButtonClicks() throws Exception {
        // Assertions to check if the size is set to 3x3
        assertEquals(3, Config.getN(), "N should be set to 3, at the beginning");
        assertEquals(3, Config.getK(), "K should be set to 3, at the beginning");

        // Simulate mouse click on the 4x4 CustomBoardPanel
        SwingUtilities.invokeAndWait(
                () -> {
                    CustomBoardPanel fourByFourPanel = startMenu.getFourByFour();
                    fourByFourPanel.getMouseListeners()[0].mouseClicked(
                            new MouseEvent(
                                    fourByFourPanel,
                                    MouseEvent.MOUSE_CLICKED,
                                    System.currentTimeMillis(),
                                    0,
                                    10,
                                    10,
                                    1,
                                    false));
                });

        // Assertions to check if the size is set to 4x4
        assertEquals(4, Config.getN(), "N should be set to 4");
        assertEquals(4, Config.getK(), "K should be set to 4");
    }

    @Test
    @DisplayName("Input Field Invalid Data Handling")
    void testInputFieldInvalidData() throws Exception {
        // Tests if the input fields are working correctly
        SwingUtilities.invokeAndWait(
                () -> {
                    simulateTextFieldInput(startMenu.getInputNField(), "invalid");
                    simulateTextFieldInput(startMenu.getInputKField(), "");
                });
        assertInstanceOf(Integer.class, Config.getN());
        assertInstanceOf(Integer.class, Config.getK());
    }
}
