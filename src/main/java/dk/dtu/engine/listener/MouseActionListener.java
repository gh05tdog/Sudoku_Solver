package dk.dtu.engine.listener;

import dk.dtu.game.core.Game;
import dk.dtu.engine.gui.SudokuBoardCanvas;

import java.awt.event.*;

public class MouseActionListener implements MouseListener {
    private boolean isInsideSudokuBoard = false;
    private Game game; // Reference to the Game class

    public MouseActionListener(Game game) {
        this.game = game;
    }



    @Override
    public void mouseClicked(MouseEvent e) {
        if (isInsideSudokuBoard) {
            game.onSudokuBoardClicked(e.getX(), e.getY());
        }
        else {
            game.onMouseClicked(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Detect if the source of the event is the SudokuBoard
        if (e.getSource() instanceof SudokuBoardCanvas) {
            isInsideSudokuBoard = true;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() instanceof SudokuBoardCanvas) {
            isInsideSudokuBoard = false;
        }
    }

    // ... other overrides ...
}
