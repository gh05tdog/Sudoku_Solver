/* (C)2024 */
package dk.dtu;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.DatabaseSetup;
import dk.dtu.game.core.StartMenu;
import javax.swing.*;

class SudokuSolverApp {

    public static void main(String[] args) {

        // Generate the database
        DatabaseSetup.setup("jdbc:sqlite:sudoku.db");

        StartMenuWindowManager startMenu = new StartMenuWindowManager(new JFrame(), 1000, 900);
        StartMenu startMenu1 = new StartMenu(startMenu);
        startMenu1.initialize();
    }
}
