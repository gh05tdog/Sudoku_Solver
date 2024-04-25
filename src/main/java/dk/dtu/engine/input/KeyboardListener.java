package dk.dtu.engine.input;

import dk.dtu.game.core.SudokuGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.*;

public class KeyboardListener implements KeyListener {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardListener.class);
    private final SudokuGame sudokuGame; // Reference to the Game class

    public KeyboardListener(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        logger.info("Key Typed: {} ",e.getKeyChar());
        sudokuGame.typeNumberWithKeyboard(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        logger.info("Key Pressed: {} ",e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        logger.info("Key Released: {} ",e.getKeyChar());
    }
}
