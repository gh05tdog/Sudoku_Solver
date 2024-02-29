package dk.dtu.engine.gui;

import java.awt.*;

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

    private   Cell[][] cells;

    public SudokuBoardCanvas(int n, int k, int cellSize) {
        this.gridSize = n * k;
        this.cellSize = cellSize;
        cells = new Cell[gridSize][gridSize];
        setFocusable(true);

        // Initialize cell states
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j] = new Cell();
            }
        }

        setSize(gridSize * cellSize, gridSize * cellSize);
    }


    @Override
    public void paint(Graphics g) {
        if (offScreenImage == null || offScreenImage.getWidth(null) != getWidth() || offScreenImage.getHeight(null) != getHeight()) {
            offScreenImage = createImage(getWidth(), getHeight());
            offScreenGraphics = offScreenImage.getGraphics();
        }

        // Clear the off-screen buffer
        offScreenGraphics.clearRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) offScreenGraphics;
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // Set the stroke for internal sub-square lines to gray
        g2.setStroke(new BasicStroke((float)1/2));

        // Draw each cell and thin lines
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Cell cell = cells[row][col];
                int x = col * cellSize;
                int y = row * cellSize;

                // Fill cell background
                g2.setColor(cell.backgroundColor);
                g2.fillRect(x, y, cellSize, cellSize);

                // Draw thin cell border
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, cellSize, cellSize);
            }
        }

        g2.setStroke(new BasicStroke(3)); // Adjust thickness as desired
        g2.setColor(Color.BLACK); // Change color to black for major grid divisions and border

        // Draw the major grid divisions and border
        int subGridSize = (int)Math.sqrt(gridSize);
        for (int i = 0; i <= gridSize; i += subGridSize) {
            int pos = i * cellSize;
            g2.drawLine(pos, 0, pos, gridSize * cellSize); // Vertical lines
            g2.drawLine(0, pos, gridSize * cellSize, pos); // Horizontal lines
        }

        int offset = 1; // Adjust the offset if needed
        g2.drawLine(gridSize * cellSize - offset, 0, gridSize * cellSize - offset, gridSize * cellSize); // Right border
        g2.drawLine(0, gridSize * cellSize - offset, gridSize * cellSize, gridSize * cellSize - offset); // Bottom border

        // Finally, draw the off-screen image to the screen
        g.drawImage(offScreenImage, 0, 0, this);
    }


    private int highlightedRow = -1;
    private int highlightedColumn = -1;

    public void highlightCell(int row, int column) {
        // Calculate sub-square size (assuming a 9x9 grid for standard Sudoku)
        int subGridSize = (int) Math.sqrt(gridSize);
        int subGridRowStart = (row / subGridSize) * subGridSize;
        int subGridColStart = (column / subGridSize) * subGridSize;

        // Clear previous highlights
        if (highlightedRow >= 0 && highlightedColumn >= 0) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    // Reset background color only for previously marked cells
                    if (cells[i][j].isHighlighted || cells[i][j].isMarked) {
                        cells[i][j].isHighlighted = false;
                        cells[i][j].isMarked = false;
                        cells[i][j].setBackgroundColor(Color.WHITE);
                        // Repaint the affected cell
                        repaint(j * cellSize, i * cellSize, cellSize, cellSize);
                    }
                }
            }
        }

        // Set the new cell to be highlighted
        cells[row][column].isHighlighted = true;
        cells[row][column].setBackgroundColor(Color.GRAY);

        // Mark entire row and column
        for (int i = 0; i < gridSize; i++) {
            // Mark row and repaint
            if (i != column) {  // Avoid re-marking the clicked cell
                cells[row][i].isMarked = true;
                cells[row][i].setBackgroundColor(Color.LIGHT_GRAY);
                repaint(i * cellSize, row * cellSize, cellSize, cellSize);
            }
            // Mark column and repaint
            if (i != row) {  // Avoid re-marking the clicked cell
                cells[i][column].isMarked = true;
                cells[i][column].setBackgroundColor(Color.LIGHT_GRAY);
                repaint(column * cellSize, i * cellSize, cellSize, cellSize);
            }
        }

        // Highlight sub-square
        for (int i = subGridRowStart; i < subGridRowStart + subGridSize; i++) {
            for (int j = subGridColStart; j < subGridColStart + subGridSize; j++) {
                if (i != row || j != column) {  // Avoid re-marking the clicked cell
                    cells[i][j].isMarked = true;
                    cells[i][j].setBackgroundColor(Color.LIGHT_GRAY);
                    repaint(j * cellSize, i * cellSize, cellSize, cellSize);
                }
            }
        }

        // Update tracking for the highlighted cell
        highlightedRow = row;
        highlightedColumn = column;

        // Repaint the clicked cell (this may be redundant if done above)
        repaint(column * cellSize, row * cellSize, cellSize, cellSize);
    }


    public void drawNumber(int x, int y, int number, Graphics g) {
        if(number == 0){
            return;
        }
        int fontSize = cellSize / 2; // Adjust the font size based on the cell size
        Font font = new Font("TimesRoman", Font.PLAIN, fontSize);
        g.setFont(font);
        g.setColor(Color.BLUE);

        // Convert number to string
        String text = String.valueOf(number);

        // Get metrics from the graphics
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        // Calculate the x and y position to make the text centered in the cell
        int xPos = x * cellSize + (cellSize - textWidth) / 2;
        int yPos = y * cellSize + ((cellSize - textHeight) / 2) + metrics.getAscent();


        g.drawString(text, xPos, yPos);
    }

    // Ensure to override update() to prevent clearing the background before paint() is called
    @Override
    public void update(Graphics g) {
        paint(g);
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
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if(cells[i][j].isHighlighted){
                    return true;
                }
            }
        }
        return false;
    }
    public int[] getHightligtedCell(){
        int[] cell = new int[2];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if(cells[i][j].isHighlighted){
                    cell[0] = i;
                    cell[1] = j;
                    return cell;

                }
            }
        }
        return cell;
    }

}