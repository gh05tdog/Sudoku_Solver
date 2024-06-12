package dk.dtu.engine.utility;

import dk.dtu.game.core.Config;

import javax.swing.*;
import java.awt.*;

public class CustomBoardPanel extends JPanel {
    private int n;
    private int k;
    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private Color backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background color
    private static final Color accentColor = new Color(237, 224, 186);
    private Color strokeColor = Config.getDarkMode() ? accentColor : Color.BLACK;

    public CustomBoardPanel() {
        this.setPreferredSize(new Dimension(120, 120));
        setBackground(backgroundColor);
    }

    public void updateBoard(int n, int k) {
        this.n = n;
        this.k = k;
        this.repaint(); // Call repaint to redraw the panel with new values
    }

    // Method to update the background color
    public void updateBackgroundColor(Color newColor) {
        backgroundColor = newColor;
        this.repaint(); // Repaint to show the new background color
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clears the panel

        int boardSize = n * k; // Total grid size
        int cellSize = 150 / boardSize; // Calculate cell size based on the smaller panel dimension
        int boardPixelSize = cellSize * boardSize; // Size of the full board in pixels
        int offset = (150 - boardPixelSize)/2;

        if(n == 2 && k == 2){
            cellSize = 145/boardSize;
            boardPixelSize = cellSize * boardSize;
            offset = (150 - boardPixelSize)/2;
        }

        // Only fill the area where the board will be drawn
        g.setColor(backgroundColor);
        g.fillRect(offset, offset, boardPixelSize, boardPixelSize); // Fill just the board area

        if (n > 0 && k > 0) { // Ensure n and k are initialized
            drawBoard(g, offset, offset, cellSize, boardPixelSize); // Pass in the new parameters
        }
    }

    private void drawBoard(Graphics g, int xOffset, int yOffset, int cellSize, int boardPixelSize) {
        int boardSize = n*k;

        // Set up the graphics object for drawing
        Graphics2D g2d = (Graphics2D) g;
        Stroke defaultStroke = g2d.getStroke();

        // Set the color for drawing the grid lines
        g2d.setColor(strokeColor);

        // Draw the subgrid lines (thicker lines)
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i <= k; i++) {
            int pos = yOffset + i * cellSize * n;
            g2d.drawLine(xOffset, pos, xOffset + boardPixelSize, pos); // Horizontal subgrid lines
            pos = xOffset + i * cellSize * n;
            g2d.drawLine(pos, yOffset, pos, yOffset + boardPixelSize); // Vertical subgrid lines
        }

        // Draw the cell lines (thinner lines)
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                int x = xOffset + i * cellSize;
                int y = yOffset + j * cellSize;
                g2d.drawRect(x, y, cellSize, cellSize); // Draw the cell
            }
        }

        // Restore the original stroke
        g2d.setStroke(defaultStroke);
    }

    public int getN (){
        return n;
    }
    public int getK (){
        return k;
    }


}
