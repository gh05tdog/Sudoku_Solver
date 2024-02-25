package dk.dtu.listener;

import dk.dtu.core.Game;

import java.awt.event.*;

public class KeyboardListener implements KeyListener {
    private Game game; // Reference to the Game class

    public KeyboardListener(Game game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("Key Typed: "+e.getKeyChar());
        game.typeNumber(e);

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
