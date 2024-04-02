package dk.dtu;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.game.core.StartMenu;

class SudokuSolverApp {
    public static void main(String[] args) throws Exception {


        StartMenuWindowManager startMenu = new StartMenuWindowManager(600, 600);
        StartMenu startMenu1 = new StartMenu(startMenu);
        startMenu1.addButton();


    }
}
