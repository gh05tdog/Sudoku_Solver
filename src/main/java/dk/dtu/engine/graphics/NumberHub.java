package dk.dtu.engine.graphics;

import javax.swing.*;
import java.awt.*;

public class NumberHub extends JPanel {
    private static int cellSize;

    private final int subGrid;

    private final int[][] numberArray;
    private static final float STROKE_WIDTH = 3.0f; // Define the thickness of the stroke

    private static final int MIN_CELL_SIZE = 50; // Minimum cell size to maintain visibility
    private static final Color HIGHLIGHT_COLOR = Color.LIGHT_GRAY;
    private static final Color NON_HIGHLIGHT_COLOR = Color.WHITE;

    private static Point selectedCell = null;

    public NumberHub(int gridSize, int cellSize) {
        this.subGrid = (int) Math.sqrt(gridSize);
        this.numberArray = new int[subGrid][subGrid];
        this.cellSize = Math.max(cellSize, MIN_CELL_SIZE); // Ensure cellSize is not too small
        setPreferredSize(new Dimension(subGrid * this.cellSize, (subGrid + 2) * this.cellSize));


        for(int i = 0; i<subGrid; i++){
            for(int j = 0; j<subGrid; j++) {

                numberArray[i][j] = j + i * subGrid + 1;
            }
        }
        setAlignmentX(Component.CENTER_ALIGNMENT);

    }

    public void highlightNumber(int x, int y) {
        selectedCell = new Point(x/cellSize, y/cellSize);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        for(int i = 0; i<subGrid; i++){
            for(int j = 0; j<subGrid; j++){
                int x = i * cellSize;
                int y = j * cellSize;

                if (selectedCell != null && selectedCell.x == i && selectedCell.y == j) {
                    g.setColor(HIGHLIGHT_COLOR);
                } else {
                    g.setColor(NON_HIGHLIGHT_COLOR);
                }

                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(STROKE_WIDTH));
                g.drawRect(x, y, cellSize, cellSize);
                int number = j * subGrid + i + 1;
                String num = Integer.toString(number);
                Font font = new Font("Arial", Font.BOLD, cellSize / 2);
                g.setFont(font);
                g.setColor(Color.BLACK);
                g.drawString(num, x + cellSize / 2 - g.getFontMetrics().stringWidth(num) / 2,
                        y + cellSize / 2 + g.getFontMetrics().getAscent() / 2);

            }


        }

        int clearBoxY = (subGrid)*cellSize;
        g.setColor(NON_HIGHLIGHT_COLOR);
        g.fillRect(0, clearBoxY, subGrid * cellSize,2 * cellSize);
        g.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g.drawRect(0, clearBoxY, subGrid * cellSize, cellSize);
        Font font = new Font("Arial", Font.BOLD, cellSize / 2);
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString("Clear", subGrid*cellSize/2 - g.getFontMetrics().stringWidth("Clear")/2,
                clearBoxY + cellSize / 2 + g.getFontMetrics().getAscent() / 2);

    }

    public int getNumber(int x, int y) {

        int i = x/cellSize;
        int j = y/cellSize;

        if (j == subGrid) {
            return 0;
        }
        else return numberArray[j][i];
    }


}
