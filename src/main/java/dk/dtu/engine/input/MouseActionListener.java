package dk.dtu.engine.input;

import dk.dtu.game.core.SudokuGame;
import dk.dtu.engine.graphics.SudokuBoardCanvas;

import java.awt.event.*;

public class MouseActionListener implements MouseListener {
    private boolean isInsideSudokuBoard = false;
    private SudokuGame sudokuGame; // Reference to the Game class

    public MouseActionListener(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
    }



    @Override
    public void mouseClicked(MouseEvent e) {
        if (isInsideSudokuBoard) {
            sudokuGame.onSudokuBoardClicked(e.getX(), e.getY());
        }
        else {
            sudokuGame.onMouseClicked(e.getX(), e.getY());
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