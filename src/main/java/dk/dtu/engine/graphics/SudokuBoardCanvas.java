package dk.dtu.engine.graphics;

import java.awt.*;
import java.util.Arrays;

class Cell {
    public boolean isMarked = false;
    Color backgroundColor = Color.WHITE; // Default background color
    public boolean isHighlighted = false;
    // Constructor, getters, and setters as needed
    public Cell() {
    }
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


}

public class SudokuBoardCanvas extends Component {
    private final int gridSize; // The number of cells in a row/column
    private final int cellSize; // The size of each cell in pixels
    private Image offScreenImage; // Off-screen buffer
    private Graphics offScreenGraphics; // Graphics context for the off-screen buffer

    private Cell[][] cells;

    public SudokuBoardCanvas(int n, int k, int cellSize) {
        this.gridSize = n * k;
        this.cellSize = cellSize;
        cells = new Cell[gridSize][gridSize];
        setFocusable(true);

        // Initialize cell states
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col] = new Cell();
            }
        }

        setSize(gridSize * cellSize, gridSize * cellSize);
    }


    @Override
    public void paint(Graphics graphics) {
        if (offScreenImage == null || offScreenImage.getWidth(null) != getWidth() || offScreenImage.getHeight(null) != getHeight()) {
            offScreenImage = createImage(getWidth(), getHeight());
            offScreenGraphics = offScreenImage.getGraphics();
        }

        // Clear the off-screen buffer
        offScreenGraphics.clearRect(0, 0, getWidth(), getHeight());

        Graphics2D graphics2D = (Graphics2D) offScreenGraphics;
//        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // Set the stroke for internal sub-square lines to gray
        graphics2D.setStroke(new BasicStroke((float)1/2));

        // Draw each cell and thin lines
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Cell cell = cells[row][col];
                int y = row * cellSize;
                int x = col * cellSize;

                // Fill cell background
                graphics2D.setColor(cell.backgroundColor);
                graphics2D.fillRect(x, y, cellSize, cellSize);

                // Draw thin cell border
                graphics2D.setColor(Color.BLACK);
                graphics2D.drawRect(x, y, cellSize, cellSize);
            }
        }

        graphics2D.setStroke(new BasicStroke(3)); // Adjust thickness as desired
        graphics2D.setColor(Color.BLACK); // Change color to black for major grid divisions and border

        // Draw the major grid divisions and border
        int subGridSize = (int)Math.sqrt(gridSize);
        for (int i = 0; i <= gridSize; i += subGridSize) {
            int pos = i * cellSize;
            graphics2D.drawLine(pos, 0, pos, gridSize * cellSize); // Vertical lines
            graphics2D.drawLine(0, pos, gridSize * cellSize, pos); // Horizontal lines
        }

        int offset = 1; // Adjust the offset if needed
        graphics2D.drawLine(gridSize * cellSize - offset, 0, gridSize * cellSize - offset, gridSize * cellSize); // Right border
        graphics2D.drawLine(0, gridSize * cellSize - offset, gridSize * cellSize, gridSize * cellSize - offset); // Bottom border

        // Finally, draw the off-screen image to the screen
        graphics.drawImage(offScreenImage, 0, 0, this);
    }




    private int highlightedRow = -1;
    private int highlightedColumn = -1;

    public void highlightCell(int rows, int columns) {

        // Calculate sub-square size (assuming a 9x9 grid for standard Sudoku)
        int subGridSize = (int) Math.sqrt(gridSize);
        int subGridRowStart = (rows / subGridSize) * subGridSize;
        int subGridColStart = (columns / subGridSize) * subGridSize;

        // Clear previous highlights
        if (highlightedRow >= 0 && highlightedColumn >= 0) {
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    // Reset background color only for previously marked cells
                    if (cells[row][col].isHighlighted || cells[row][col].isMarked) {
                        cells[row][col].isHighlighted = false;
                        cells[row][col].isMarked = false;
                        cells[row][col].setBackgroundColor(Color.WHITE);
                        // Repaint the affected cell
                        repaint(col * cellSize, row * cellSize, cellSize, cellSize);
                    }
                }
            }
        }

        // Set the new cell to be highlighted
        cells[rows][columns].isHighlighted = true;
        cells[rows][columns].setBackgroundColor(Color.GRAY);

        // Mark entire rows and columns
        for (int i = 0; i < gridSize; i++) {
            // Mark rows and repaint
            if (i != columns) {  // Avoid re-marking the clicked cell
                cells[rows][i].isMarked = true;
                cells[rows][i].setBackgroundColor(Color.LIGHT_GRAY);
                repaint(columns*cellSize, i * cellSize, cellSize, cellSize);
            }
            // Mark columns and repaint
            if (i != rows) {  // Avoid re-marking the clicked cell
                cells[i][columns].isMarked = true;
                cells[i][columns].setBackgroundColor(Color.LIGHT_GRAY);
                repaint(i*cellSize, rows*cellSize, cellSize, cellSize);
            }
        }

        // Highlight sub-square
        for (int row = subGridRowStart; row < subGridRowStart + subGridSize; row++) {
            for (int col = subGridColStart; col < subGridColStart + subGridSize; col++) {
                if (row != rows || col != columns) {  // Avoid re-marking the clicked cell
                    cells[row][col].isMarked = true;
                    cells[row][col].setBackgroundColor(Color.LIGHT_GRAY);

                }
            }
        }

        // Update tracking for the highlighted cell
        highlightedRow = rows;
        highlightedColumn = columns;

        // Repaint the clicked cell (this may be redundant if done above)
        repaint(columns * cellSize, rows * cellSize, cellSize, cellSize);
    }


    public void drawNumber(int row, int col, int number, Graphics graphics) {
        if(number == 0){
            return;
        }
        int fontSize = cellSize / 2; // Adjust the font size based on the cell size
        Font font = new Font("TimesRoman", Font.PLAIN, fontSize);
        graphics.setFont(font);
        graphics.setColor(Color.BLUE);

        // Convert number to string
        String text = String.valueOf(number);

        // Get metrics from the graphics
        FontMetrics metrics = graphics.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        // Calculate the row and col position to make the text centered in the cell
        int xPos = col * cellSize + (cellSize - textWidth) / 2;
        int yPos = row * cellSize + ((cellSize - textHeight) / 2) + metrics.getAscent();


        graphics.drawString(text, xPos, yPos);
    }

    // Ensure to override update() to prevent clearing the background before paint() is called
    @Override
    public void update(Graphics graphics) {
        paint(graphics);
    }

    public void removeNumber(int row, int column) {
        // Ensure operations are on the offScreenGraphics
        if (offScreenGraphics != null) {
            // Calculate the top-left corner of the cell
            int x = column * cellSize;
            int y = row * cellSize;

            // Clear the specified cell by redrawing the background
            offScreenGraphics.setColor(Color.WHITE); // Assume background color
            offScreenGraphics.fillRect(x, y, cellSize, cellSize);

            // Redraw the cell border
            offScreenGraphics.setColor(Color.BLACK);
            offScreenGraphics.drawRect(x, y, cellSize, cellSize);

            // Finally, trigger a repaint for only the affected cell area
            repaint(x, y, cellSize, cellSize);
        }
    }

    public boolean isACellHighligthed(){
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if(cells[row][col].isHighlighted){
                    return true;
                }
            }
        }
        return false;
    }
    public int[] getHightligtedCell(){
        int[] cell = new int[2];
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if(cells[row][col].isHighlighted){
                    cell[0] = row;
                    cell[1] = col;
                    return cell;

                }
            }
        }
        return cell;
    }

}