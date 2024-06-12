package dk.dtu.engine.input;

import dk.dtu.game.core.SudokuGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.*;


/**
 * The KeyboardListener class is responsible for listening to keyboard events.
 * It listens for key typed events and calls the typeNumberWithKeyboard method in the Game class.
 */
public class KeyboardListener implements KeyListener {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardListener.class);
    private final SudokuGame sudokuGame; // Reference to the Game class

    public KeyboardListener(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        logger.debug("Key Typed: {} ",e.getKeyChar());
        sudokuGame.typeNumberWithKeyboard(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        logger.debug("Key Pressed: {} ",e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        logger.debug("Key Released: {} ",e.getKeyChar());
    }
}
