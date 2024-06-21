/* (C)2024 */
package dk.dtu;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.DatabaseSetup;
import dk.dtu.game.core.StartMenu;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import javax.swing.*;

public class SudokuSolverApp {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SudokuSolverApp.class);

    public static void main(String[] args) {
        boolean isDebug = false;

        // Parse command-line arguments
        for (String arg : args) {
            if ("--debug".equals(arg)) {
                isDebug = true;
                break;
            }
        }

        // Configure Logback programmatically
        configureLogging(isDebug);

        logger.info("Starting Sudoku Solver Application...");
        //Get icon from resources
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(SudokuSolverApp.class.getResource("/logo.png")));

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
                            JFrame frame = new JFrame("Sudoku");
                            frame.setIconImage(icon.getImage());
                            StartMenuWindowManager startMenu =
                                    new StartMenuWindowManager(frame, 1000, 1000);
                            StartMenu startMenu1 = new StartMenu(startMenu);
                            startMenu1.initialize();
                            logger.info("GUI initialized successfully.");
                        } catch (Exception e) {
                            logger.error("Error initializing GUI", e);
                        }
                    });
        } catch (Exception e) {
            logger.error("Error starting application", e);
        }
    }

    private static void configureLogging(boolean isDebug) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();  // Reset the default configuration

        try {
            // Load the configuration from the string
            configurator.doConfigure(SudokuSolverApp.class.getResourceAsStream("/logback.xml"));
        } catch (JoranException je) {
            // Handle the exception as needed, without using StatusPrinter
            System.err.println("Error configuring Logback: " + je.getMessage());
        }

        // Set the root logger level to DEBUG if the --debug flag is present
        if (isDebug) {
            context.getLogger("ROOT").setLevel(Level.DEBUG);
        } else {
            context.getLogger("ROOT").setLevel(Level.INFO);
        }
    }
}
