/* (C)2024 */
package dk.dtu;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.StartMenu;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SudokuSolverApp {
    private static final Logger logger = LoggerFactory.getLogger(SudokuSolverApp.class);

    public static void main(String[] args) throws Board.BoardNotCreatable {

        StartMenuWindowManager startMenu = new StartMenuWindowManager(new JFrame(), 1000, 1000);
        StartMenu startMenu1 = new StartMenu(startMenu);
        startMenu1.initialize();
    }
}
