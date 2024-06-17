/* (C)2024 */
package dk.dtu;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.DatabaseSetup;
import dk.dtu.game.core.StartMenu;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class SudokuSolverApp {

    private static final Logger logger = Logger.getLogger(SudokuSolverApp.class.getName());

    public static void main(String[] args) {
        logger.info("Starting Sudoku Solver Application...");

        try {
            // Generate the database
            logger.info("Setting up database...");
            DatabaseSetup.setup("jdbc:sqlite:sudoku.db");
            logger.info("Database setup complete.");

            // Ensure GUI code runs on the EDT
            SwingUtilities.invokeLater(
                    () -> {
                        try {
                            logger.info("Initializing GUI...");
                            StartMenuWindowManager startMenu =
                                    new StartMenuWindowManager(new JFrame(), 1000, 1000);
                            StartMenu startMenu1 = new StartMenu(startMenu);
                            startMenu1.initialize();
                            logger.info("GUI initialized successfully.");
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error initializing GUI", e);
                        }
                    });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting application", e);
        }
    }
}
