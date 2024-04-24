package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.graphics.numberHub;
import dk.dtu.engine.utility.CustomBoardPanel;
import dk.dtu.engine.utility.CustomComponentGroup;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.SudokuGame;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SudokuGameTest {

    private SudokuGame game;
    private CustomComponentGroup componentGroup;
    private CustomBoardPanel panel2;

    @BeforeEach
    void setUp() throws Exception {

        JFrame testFrame = new JFrame();
        StartMenuWindowManager startMenuWindowManager = new StartMenuWindowManager(testFrame, 1000, 700);
        StartMenu startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();
        startMenu.getStartButton().doClick(); // Simulates starting the game through the UI

        WindowManager windowManager = new WindowManager(startMenuWindowManager.getFrame());
        try {
            game = new SudokuGame(windowManager, 3, 3, 550 / 9);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeAndWait(
                () -> {
                    game.initialize(3, 3, 550 / 9); // Ensures the game is ready to be tested
                });
        game.setTestMode(true);
        CustomBoardPanel panel1 = new CustomBoardPanel();
        panel2 = new CustomBoardPanel();
        CustomBoardPanel panel3 = new CustomBoardPanel();
        componentGroup = new CustomComponentGroup();

        componentGroup.addComponent(panel1);
        componentGroup.addComponent(panel2);
        componentGroup.addComponent(panel3);

        // Simulating adding mouse listener that sets the selected component
        panel2.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        componentGroup.selectedComponent = panel2;
                    }
                });
    }

    @Test
    void testGameInitialization() {
        assertNotNull(game.gameboard, "Game board must not be null after initialization.");
        assertEquals(9, game.gameboard.getDimensions(), "Game board should be initialized as 9x9.");
        assertTrue(game.moveList.isEmpty(), "Move list should be empty initially.");
        assertFalse(game.gameIsStarted, "Game should not start automatically.");
    }

    @Test
    @DisplayName("Test number placement on board click within valid conditions")
    void testOnSudokuBoardClickedWithNumberPlacement() {
        game.board.setSize(550, 550);
        // Setting a valid number to place
        game.placeableNumber = 5;

        // Ensuring the target cell is valid for placement
        game.gameboard.setInitialNumber(4, 4, 0); // Ensure the cell is initially empty
        game.gameboard.setNumber(4, 4, 0); // No number currently placed

        // Click at the center cell which should be empty and valid
        game.onSudokuBoardClicked(275, 275);
        int[] markedCell = game.board.getMarkedCell();

        // Verify the number placement
        assertEquals(4, markedCell[0], "Clicked cell row should be 4.");
        assertEquals(4, markedCell[1], "Clicked cell column should be 4.");
        assertEquals(5, game.gameboard.getNumber(4, 4), "Number 5 should be placed at (4,4).");
        assertFalse(
                game.moveList.isEmpty(), "Move list should not be empty after a valid placement.");
        assertEquals(
                5,
                game.moveList.peek().getNumber(),
                "Top of move list should have the placed number 5.");
    }

    @Test
    @DisplayName("Test click handling when clicking on a cell with initial number")
    void testOnSudokuBoardClickedInitialNumber() {
        game.board.setSize(550, 550);
        // Set an initial number that should not be overwritten
        game.gameboard.setInitialNumber(4, 4, 1);
        game.gameboard.setNumber(4, 4, 1);
        game.placeableNumber = 5;

        // Attempt to place a number in a cell that already has an initial number
        game.onSudokuBoardClicked(275, 275);

        // Check that the initial number is unchanged
        assertEquals(
                1,
                game.gameboard.getInitialNumber(4, 4),
                "Cell (4,4) should still have the initial number 1.");
        assertEquals(
                1,
                game.gameboard.getNumber(4, 4),
                "Cell (4,4) should not update the number due to initial number constraint.");
        assertTrue(
                game.moveList.isEmpty(),
                "No moves should be recorded when clicking on a cell with an initial number.");
    }

    @Test
    void testTypeNumberWithKeyboard() {
        game.board.setMarkedCell(0, 0); // Set the top-left cell as marked
        KeyEvent keyEvent =
                new KeyEvent(
                        new JButton(),
                        KeyEvent.KEY_TYPED,
                        System.currentTimeMillis(),
                        0,
                        KeyEvent.VK_UNDEFINED,
                        '5');
        game.typeNumberWithKeyboard(keyEvent);
        assertEquals(5, game.gameboard.getNumber(0, 0), "Number 5 should be placed at (0,0).");
    }

    @Test
    void testUndoMove() {
        game.initialize(3, 3, 550 / 9); // Initialize game
        game.board.setMarkedCell(0, 0); // Mark the top-left cell
        game.makeMoveTest(0, 0, 5); // Simulate placing number 5 at (0,0)

        JButton undoButton = game.getUndoButton();
        assertNotNull(undoButton, "Undo button should not be null");
        undoButton.doClick(); // Simulate undo click

        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after undo.");
    }

    @Test
    void testIsSudokuCompleted() {
        // Fill the board with non-zero numbers
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                game.gameboard.setNumber(
                        i, j, 1); // Assuming valid placement isn't required for this test
            }
        }
        assertTrue(game.isSudokuCompleted(), "Game should be marked as completed.");
    }

    @Test
    void testProvideHint() throws Exception {
        game.initialize(3, 3, 550 / 9); // Ensure game initialization
        game.newGame(); // Start a new game to generate hints

        // Initially check that there are hints available
        assertFalse(
                game.getHintList().isEmpty(),
                "Hint list should not be empty after starting a new game.");

        // Click the hint button
        JButton hintButton = game.getHintButton();
        assertNotNull(hintButton, "Hint button should not be null");
        hintButton.doClick(); // Simulate clicking the hint button

        // Check if a hint was actually used
        assertFalse(
                game.getHintList().isEmpty(),
                "Hint list should still contain hints after one use.");
    }

    @Test
    void testEraseNumber() {
        // Ensure the game is initialized and ready for interaction
        game.initialize(
                3, 3, 550 / 9); // Call initialize to setup the game completely if not already done

        // Set the cell number and mark it
        game.board.setCellNumber(0, 0, 5); // Set number 5 at (0,0)
        game.board.setMarkedCell(0, 0); // Mark the cell

        // Now click the erase button
        JButton eraseButton =
                game.getEraseButton(); // This should no longer be null if initialization is correct
        assertNotNull(eraseButton, "Erase button should not be null");
        eraseButton.doClick(); // Simulate button click

        // Check the result
        assertEquals(0, game.gameboard.getNumber(0, 0), "Cell should be empty after erasing.");
    }

    @Test
    void testDisplayNumbersVisually() {
        game.displayNumbersVisually();
        game.board.setMarkedCell(0, 0); // Set the top-left cell as marked
        assertEquals(game.board.getMarkedNumber(), game.gameboard.getNumber(0, 0));
    }

    @Test
    void testRestartGame() throws Exception {
        game.initialize(3, 3, 550 / 9); // Initialize game
        game.board.setCellNumber(0, 0, 5); // Set number 5 at (0,0)
        game.newGame(); // Ensure a game state exists to restart from

        JButton restartButton = game.getRestartButton();
        assertNotNull(restartButton, "Restart button should not be null");
        restartButton.doClick(); // Simulate restart click

        assertTrue(game.moveList.isEmpty(), "Move list should be empty after restarting the game.");
    }

    @Test
    void testStartGame() throws Exception {

        JButton startButton = game.getStartButton();
        assertNotNull(startButton, "Start button should not be null");
        System.out.println("Game is started: " + game.gameIsStarted);
        // Simulate starting the game
        SwingUtilities.invokeAndWait(startButton::doClick);

        assertTrue(game.gameIsStarted, "Game should be started after clicking the start button.");
    }

    @Test
    void testSolver() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(
                () -> {
                    game.initialize(3, 3, 550 / 9); // Initialize game

                    try {
                        game.newGame(); // Ensure a valid game is loaded
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        SwingUtilities.invokeAndWait(
                () -> {
                    JButton startButton = game.getStartButton();
                    assertNotNull(startButton, "Start button should not be null");
                    JButton solveButton = game.getSolveButton();
                    assertFalse(
                            solveButton.isEnabled(), "Solve button should be disabled initially.");
                    startButton.doClick(); // Simulate starting the game
                    solveButton = game.getSolveButton();
                    assertTrue(
                            solveButton.isEnabled(),
                            "Solve button should be enabled after starting the game.");

                    assertNotNull(solveButton, "Solve button should not be null");
                    solveButton.doClick(); // Simulate solving the game
                });

        assertTrue(game.isSudokuCompleted(), "Game should be completed after solving.");
    }

    @Test
    @DisplayName("Test handling of number board clicks")
    void testOnNumbersBoardClicked() {
        // Simulate clicking at a position corresponding to number 5 in the numberHub
        final int x = 50;
        final int y = 50;

        game.numbers =
                new numberHub(9, 550 / 9) {
                    @Override
                    public int getNumber(int x, int y) {
                        return 5; // Mock behavior
                    }

                    @Override
                    public void highlightNumber(int x, int y) {
                        // Mock behavior: Test could verify this call
                        System.out.println("Number highlighted: " + getNumber(x, y));
                    }
                };

        game.board =
                new SudokuBoardCanvas(3, 3, 550 / 9) {
                    @Override
                    public void setChosenNumber(int number) {
                        super.setChosenNumber(number);
                        assertEquals(5, number, "Chosen number should be set to 5 on the board.");
                    }
                };

        game.onNumbersBoardClicked(x, y);

        assertEquals(5, game.placeableNumber, "Placeable number should be updated to 5.");
    }

    @Test
    void testAddComponentAndClick() {
        // Ensure panel2 is correctly initialized and not null
        assertNotNull(panel2, "Panel 2 should not be null");

        // Simulate a mouse click on panel2
        MouseEvent click =
                new MouseEvent(
                        panel2, // Use panel2 as the source
                        MouseEvent.MOUSE_CLICKED, // Event type
                        System.currentTimeMillis(), // Current time as the timestamp
                        0, // No modifiers
                        10, // x coordinate of the click
                        10, // y coordinate of the click
                        1, // Number of clicks
                        false // Not a popup trigger
                        );

        // Iterate over all MouseListeners attached to panel2 and trigger the mouseClicked event
        for (MouseListener ml : panel2.getMouseListeners()) {
            ml.mouseClicked(click);
        }

        // Assert that the correct component was selected after the click
        assertEquals(
                panel2,
                componentGroup.selectedComponent,
                "Panel 2 should be the selected component after click.");
    }
}
