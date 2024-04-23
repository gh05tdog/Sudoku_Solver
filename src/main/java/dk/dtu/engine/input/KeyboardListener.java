package dk.dtu.engine.input;

import dk.dtu.game.core.SudokuGame;

import java.awt.event.*;

public class KeyboardListener implements KeyListener {
    private final SudokuGame sudokuGame; // Reference to the Game class

    public KeyboardListener(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("Key Typed: "+e.getKeyChar());
        sudokuGame.typeNumberWithKeyboard(e);

    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key Typed: "+e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("Key Typed: "+e.getKeyChar());
    }
}
