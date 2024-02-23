package dk.dtu.gui;

import java.awt.*;

public class SudokuBoardCanvas extends Component {
    private int gridSize; // The number of cells in a row/column
    private int cellSize; // The size of each cell in pixels

    public SudokuBoardCanvas(int gridSize, int cellSize) {
        this.gridSize = gridSize;
        this.cellSize = cellSize;
        // Assuming a square board, set the preferred size to accommodate the grid
        setSize(gridSize * cellSize, gridSize * cellSize);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawGrid(g);
    }

    private void drawGrid(Graphics g) {
        // Set the color for the grid lines
        g.setColor(Color.BLACK);

        // Draw the cell borders
        for (int i = 0; i <= gridSize; i++) {
            for (int j = 0; j <= gridSize; j++) {
                int x = i * cellSize;
                int y = j * cellSize;
                g.drawLine(x, 0, x, gridSize * cellSize); // Vertical lines
                g.drawLine(0, y, gridSize * cellSize, y); // Horizontal lines
            }
        }

        // Optionally, draw thicker lines to delineate Sudoku blocks
        Graphics2D g2 = (Graphics2D) g;
        float thickness = 2; // Specify the thickness of the block borders
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));

        int blocksize = (int) Math.sqrt(gridSize); // Calculate the size of the blocks, assuming a perfect square
        for (int i = 0; i <= gridSize; i += blocksize) {
            int x = i * cellSize;
            int y = i * cellSize;
            // Draw thicker vertical and horizontal block lines
            g2.drawLine(x, 0, x, gridSize * cellSize);
            g2.drawLine(0, y, gridSize * cellSize, y);
        }

        g2.setStroke(oldStroke); // Reset to the original stroke
    }
}
