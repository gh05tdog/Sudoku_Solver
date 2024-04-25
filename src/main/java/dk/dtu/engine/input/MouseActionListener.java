package dk.dtu.engine.input;

import dk.dtu.engine.graphics.numberHub;
import dk.dtu.game.core.SudokuGame;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.*;

public class MouseActionListener implements MouseListener {
    private static final Logger logger = LoggerFactory.getLogger(MouseActionListener.class);
    private boolean isInsideSudokuBoard = false;
    private boolean isInsideNumbersBoard = false;
    private final SudokuGame sudokuGame; // Reference to the Game class

    public MouseActionListener(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (isInsideSudokuBoard) {
            sudokuGame.onSudokuBoardClicked(e.getX(), e.getY());
        } else if (isInsideNumbersBoard) {
            sudokuGame.onNumbersBoardClicked(e.getX(), e.getY());
        } else {
            logger.info("Clicked outside of the board");
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        logger.info("Mouse Pressed: {} ", e.getButton());


    }

    @Override
    public void mouseReleased(MouseEvent e) {
        logger.info("Mouse Released: {} ", e.getButton());

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Detect if the source of the event is the SudokuBoard
        if (e.getSource() instanceof SudokuBoardCanvas) {
            isInsideSudokuBoard = true;
        }
        if (e.getSource() instanceof numberHub) {
            isInsideNumbersBoard = true;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() instanceof SudokuBoardCanvas) {
            isInsideSudokuBoard = false;
        }
        if (e.getSource() instanceof numberHub) {
            isInsideNumbersBoard = false;
        }
    }


    public boolean getIsInsideSudokuBoard() {
        return isInsideSudokuBoard;
    }
}
