/* (C)2024 */
package dk.dtu;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.utility.DatabaseSetup;
import dk.dtu.game.core.StartMenu;

import javax.swing.*;
import java.util.Objects;

class SudokuSolverApp {

    public static void main(String[] args) {

        // Generate the database
        DatabaseSetup.setup("jdbc:sqlite:sudoku.db");
        JFrame frame = new JFrame();
        ImageIcon img = new ImageIcon(Objects.requireNonNull(SudokuSolverApp.class.getResource("/logo.png")));
        frame.setIconImage(img.getImage());
        StartMenuWindowManager startMenu = new StartMenuWindowManager(frame, 1000, 1000);
        StartMenu startMenu1 = new StartMenu(startMenu);
        startMenu1.initialize();
    }
}
