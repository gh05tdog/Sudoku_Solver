/* (C)2024 */
package dk.dtu.engine.graphics;

import dk.dtu.game.core.Config;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * The NumberHub class is responsible for displaying the numbers that can be placed on the Sudoku board.
 * It displays the numbers 1 to n*k.
 */
public abstract class NumberHub extends JPanel {
    private static int cellSize;
    private final int subGrid;
    private final int[][] numberArray;
    private static final float STROKE_WIDTH = 3.0f;
    private static final int MIN_CELL_SIZE = 50;

    private static final Color CROSS_OUT_COLOR = Color.GRAY; // Color to cross out the numbers

    // Map to keep track of which numbers are available to place
    private final Map<Integer, Boolean> numberAvailability;

    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private Color backgroundColor =
            Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background
    private Color accentColor =
            Config.getDarkMode() ? new Color(237, 224, 186) : Color.BLACK;

    private Color nonhighlightColor = backgroundColor;

    protected NumberHub(int n, int cellSize) {
        this.subGrid = n;
        this.numberArray = new int[subGrid][subGrid];
        this.numberAvailability = new HashMap<>();

        for (int i = 1; i <= subGrid * subGrid; i++) {
            numberAvailability.put(i, true); // All numbers available initially
        }

        NumberHub.setCellSize(Math.max(cellSize, MIN_CELL_SIZE));
        // Calculate the size of the NumberHub panel
        setPreferredSize(
                new Dimension(subGrid * NumberHub.cellSize, (subGrid + 2) * NumberHub.cellSize));
        setBackground(backgroundColor);
        for (int i = 0; i < subGrid; i++) {
            for (int j = 0; j < subGrid; j++) {
                numberArray[i][j] = j + i * subGrid + 1;
            }
        }
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private static void setCellSize(int size) {
        cellSize = size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        setBackground(backgroundColor); // Ensure background color is set
        for (int i = 0; i < subGrid; i++) {
            for (int j = 0; j < subGrid; j++) {
                int x = i * cellSize;
                int y = j * cellSize;
                int number = j * subGrid + i + 1;

                if (Boolean.TRUE.equals(numberAvailability.getOrDefault(number, true))) {
                    g.setColor(nonhighlightColor);
                } else {
                    g.setColor(CROSS_OUT_COLOR);
                }
                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(accentColor);
                g2d.setStroke(new BasicStroke(STROKE_WIDTH));
                g.drawRect(x, y, cellSize, cellSize);

                String num = Integer.toString(number);
                Font font = new Font("Arial", Font.BOLD, cellSize / 2);
                g.setFont(font);
                g.setColor(accentColor);
                g.drawString(
                        num,
                        x + cellSize / 2 - g.getFontMetrics().stringWidth(num) / 2,
                        y + cellSize / 2 + g.getFontMetrics().getAscent() / 2);
            }
        }

        int clearBoxY = subGrid * cellSize;
        g.setColor(nonhighlightColor);
        g.fillRect(0, clearBoxY, subGrid * cellSize, 2 * cellSize);
        g.setColor(accentColor);
    }

    // Get the number that was clicked
    public int getNumber(int x, int y) {
        int i = x / cellSize;
        int j = y / cellSize;

        if (j == subGrid) {
            return 0;
        } else {
            return numberArray[j][i];
        }
    }

    // Check if the number is available to place if not paint it
    public void updateNumberDisplay(int number, boolean available) {
        numberAvailability.put(number, available);
        repaint();
    }

    public void update() {
        backgroundColor =
                Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background
        accentColor = Config.getDarkMode() ? new Color(237, 224, 186) : Color.BLACK;

        // Ensure NON_HIGHLIGHT_COLOR is updated as well
        nonhighlightColor = backgroundColor;

        setBackground(backgroundColor);
        revalidate();
        repaint();
    }
}
