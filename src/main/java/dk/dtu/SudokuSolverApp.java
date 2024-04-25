package dk.dtu;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.game.core.StartMenu;

import javax.swing.*;

class SudokuSolverApp {
    public static void main(String[] args)  {

        StartMenuWindowManager startMenu = new StartMenuWindowManager(new JFrame(), 1000, 700);
        StartMenu startMenu1 = new StartMenu(startMenu);
        startMenu1.initialize();



    }
}
