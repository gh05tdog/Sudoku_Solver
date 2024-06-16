/* (C)2024 */
package dk.dtu.engine.graphics;

import dk.dtu.game.core.Board;
import dk.dtu.game.core.Config;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SudokuBoardCanvas class is the visual representation of the game. It features many functions to paint the grid and the subgrids,
 * it also makes a 2D array of cells, which is an object that stores the numbers on the board and the notes. This class also paints the
 * killer sudoku, the hightlighting of cells and the hint visualization
 */
public class SudokuBoardCanvas extends JPanel {
    private final int gridSize;
    private final int subgridSize;
    private final int cellSize;
    private final int nSize;
    private final Cell[][] cells;

    private static final Logger logger = LoggerFactory.getLogger(SudokuBoardCanvas.class);
    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private static final Color backgroundColor =
            Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background
    private static Color accentColor =
            Config.getDarkMode() ? new Color(237, 224, 186) : Color.BLACK;
    private static final Color hintColor =
            Config.getDarkMode() ? new Color(109, 181, 208) : Color.BLUE;

    int chosenNumber = 0;

    private final Map<Integer, Cage> cages = new HashMap<>();
    private final Map<Point, Integer> cellToCageMap = new HashMap<>();

    public SudokuBoardCanvas(int n, int k, int cellSize) {
        this.gridSize = n * k;
        this.subgridSize = n;
        this.cellSize = cellSize;
        this.nSize = n;
        cells = new Cell[gridSize][gridSize];
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col] = new Cell();
            }
        }
        setPreferredSize(new Dimension(gridSize * cellSize, gridSize * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Cell cell = cells[row][col];
                int x = col * cellSize;
                int y = row * cellSize;
                cell.paintCell(g, x, y, cellSize, chosenNumber);
                cell.paintNotes(g, x, y, cellSize);
            }
        }
        drawSubGrids(g);

        if (Config.getEnableKillerSudoku()) {
            drawCages(g);
        }
        repaint();
    }

    private void drawSubGrids(Graphics g) {
        int subGridSize = nSize;
        g.setColor(accentColor);

        for (int i = 0; i <= gridSize; i += subGridSize) {
            int pos = i * cellSize;
            if (i % subGridSize == 0) {
                g.fillRect(pos - 2, 0, 4, gridSize * cellSize);
                g.fillRect(0, pos - 2, gridSize * cellSize, 4);
            }
        }
    }

    public void drawCages(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        float[] dash = {4f, 4f}; //
        BasicStroke stroke =
                new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0f);
        g2.setStroke(stroke);
        g2.setColor(accentColor);
        g2.setFont(new Font("Arial", Font.BOLD, 12));

        int margin = 6; // Margin from the edges
        int sumMargin = 3; // Smaller margin for the sum display

        for (Cage cage : cages.values()) {
            List<Point> cells = cage.getCells();
            int sum = cage.getSum();
            String sumStr = Integer.toString(sum);

            if (!cells.isEmpty()) {
                // Calculate position for the sum
                Point firstCell = cells.getFirst();
                int sumX = firstCell.x * cellSize + sumMargin;
                int sumY = firstCell.y * cellSize + sumMargin + 10;

                // Adjust position for double-digit sums
                if (sumStr.length() > 1) {
                    sumX -= 1;
                }

                g2.drawString(sumStr, sumX, sumY);

                // Draw the cage borders
                for (Point cell : cells) {
                    int cellX = cell.x * cellSize + margin;
                    int cellY = cell.y * cellSize + margin;
                    int size = cellSize - 2 * margin;

                    boolean isSumCell = cell.equals(firstCell);

                    // Draw the top border (skip small section if sum cell)
                    if (cell.y == 0 || !cells.contains(new Point(cell.x, cell.y - 1))) {
                        if (isSumCell) {
                            g2.drawLine(cellX + margin * 2, cellY, cellX + size, cellY);
                        } else {
                            g2.drawLine(cellX, cellY, cellX + size, cellY);
                        }
                    }

                    // Draw the bottom border
                    if (cell.y == gridSize - 1 || !cells.contains(new Point(cell.x, cell.y + 1))) {
                        g2.drawLine(cellX, cellY + size, cellX + size, cellY + size);
                    }

                    // Draw the left border (skip small section if sum cell)
                    if (cell.x == 0 || !cells.contains(new Point(cell.x - 1, cell.y))) {
                        if (isSumCell) {
                            g2.drawLine(cellX, cellY + margin * 2, cellX, cellY + size);
                        } else {
                            g2.drawLine(cellX, cellY, cellX, cellY + size);
                        }
                    }

                    // Draw the right border
                    if (cell.x == gridSize - 1 || !cells.contains(new Point(cell.x + 1, cell.y))) {
                        g2.drawLine(cellX + size, cellY, cellX + size, cellY + size);
                    }
                }
            }
        }
    }

    public void setCellNumber(int row, int col, int number) {
        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            cells[row][col].setNumber(number);
            repaint();
        }
    }

    public void highlightCell(int row, int col, boolean highlight) {
        // First, clear all existing highlights
        clearHighlights();

        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            // Calculate the subgrid's top-left coordinates
            int subGridSize = nSize;
            int subGridRowStart = (row / subGridSize) * subGridSize;
            int subGridColStart = (col / subGridSize) * subGridSize;

            // Highlight the entire row and column
            for (int i = 0; i < gridSize; i++) {
                cells[row][i].setHighlighted(highlight); // Highlight row
                cells[i][col].setHighlighted(highlight); // Highlight column
            }

            // Highlight the subgrid
            for (int subRow = subGridRowStart; subRow < subGridRowStart + subGridSize; subRow++) {
                for (int subCol = subGridColStart;
                        subCol < subGridColStart + subGridSize;
                        subCol++) {
                    cells[subRow][subCol].setHighlighted(highlight);
                }
            }

            // Set the clicked cell as highlighted to keep track of the last clicked cell
            cells[row][col].setHighlighted(highlight);
            repaint();
        }
    }

    public void visualizeCell(int row, int col, Color startColor) {
        final int totalSteps = 5; // Total steps to fade color
        final int delay = 100;
        Cell cell = cells[row][col];
        cell.startHintVisualization();

        ActionListener fadeAction =
                new ActionListener() {
                    private int step = 0;

                    public void actionPerformed(ActionEvent e) {
                        if (step < totalSteps) {
                            // Calculate the step color
                            int r =
                                    startColor.getRed()
                                            + (backgroundColor.getRed() - startColor.getRed())
                                                    * step
                                                    / totalSteps;
                            Color stepColor = getColor(r);
                            cell.setBackgroundColor(stepColor);
                            repaint();
                            step++;
                        } else {
                            // Stop the timer and reset the background color to white at the end
                            ((Timer) e.getSource()).stop();
                            cell.setBackgroundColor(backgroundColor);
                            cell.endHintVisualization();
                            repaint();
                        }
                    }

                    private Color getColor(int r) {
                        int g =
                                startColor.getGreen()
                                        + (backgroundColor.getGreen() - startColor.getGreen())
                                                * step
                                                / totalSteps;
                        int b =
                                startColor.getBlue()
                                        + (backgroundColor.getBlue() - startColor.getBlue())
                                                * step
                                                / totalSteps;
                        return new Color(r, g, b);
                    }
                };

        new Timer(delay, fadeAction).start();
        // Set the cell color to the start color
        cell.setTextColor(hintColor);
    }

    private void clearHighlights() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col].setHighlighted(false);
            }
        }
    }

    // The marked cell is the cell clicked on, it is used to highlight the marked cell in a darker
    // color
    public int[] getMarkedCell() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (cells[i][j].isMarked) {
                    return new int[] {i, j};
                }
            }
        }
        return new int[0];
    }

    // The marked cell is the cell clicked on, it is used to highlight the marked cell in a darker
    // color
    public void setMarkedCell(int row, int col) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j].isMarked = false;
            }
        }
        cells[row][col].isMarked = true;
        logger.debug("Setting the marked cell {} {}", row, col);
    }

    // When easymode is enabled, this will highlight all the places where you can place the current
    // number you have clicked on.
    public void highlightPlaceableCells(int number) {
        clearUnplacableCells();
        boolean[][] unPlaceable = new boolean[gridSize][gridSize];

        if (number == 0) {
            return;
        }

        // Mark all cells as unPlaceable initially
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                unPlaceable[row][col] = false;
            }
        }

        // Iterate through all cells to find the number
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cells[row][col].getNumber() == number) {
                    // Mark entire row and column as unPlaceable
                    for (int i = 0; i < gridSize; i++) {
                        unPlaceable[row][i] = true;
                        unPlaceable[i][col] = true;
                    }

                    int subgridSize = (int) Math.sqrt(gridSize);
                    int startRow = (row / subgridSize) * subgridSize;
                    int startCol = (col / subgridSize) * subgridSize;
                    for (int i = startRow; i < startRow + subgridSize; i++) {
                        for (int j = startCol; j < startCol + subgridSize; j++) {
                            unPlaceable[i][j] = true;
                        }
                    }
                }
            }
        }

        if (Config.getEnableKillerSudoku()) {
            for (Cage cage : cages.values()) {
                List<Point> cells = cage.getCells();
                Set<Integer> currentNumbers = cage.getCurrentNumbers();
                int currentSum = currentNumbers.stream().mapToInt(Integer::intValue).sum();

                for (Point cell : cells) {
                    int row = cell.y;
                    int col = cell.x;

                    // Check if the number is already present in the cage
                    if (currentNumbers.contains(number)) {
                        unPlaceable[row][col] = true;
                    }

                    // Check if placing the number would exceed the cage's sum constraint
                    if (currentSum + number > cage.getSum()) {
                        unPlaceable[row][col] = true;
                    }
                }
            }
        }

        // Apply highlights only to placeable cells (i.e., not marked as unPlaceable)
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!unPlaceable[row][col] && cells[row][col].getNumber() == 0) {
                    cells[row][col].setUnplaceableCell(true);
                }
            }
        }
    }

    public void clearUnplacableCells() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j].setUnplaceableCell(false);
            }
        }
    }

    public boolean isACellMarked() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cells[row][col].isMarked) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeNumber(int row, int column) {
        if (row >= 0 && row < gridSize && column >= 0 && column < gridSize) {
            cells[row][column].setNumber(0);
            repaint();
        }
    }

    public void setChosenNumber(int number) {
        chosenNumber = number;
    }

    public void setWrongNumber(int row, int column, int number) {
        if (row >= 0 && row < gridSize && column >= 0 && column < gridSize) {
            cells[row][column].setWrongNumber(number);
        }
    }

    public int getMarkedNumber() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (cells[i][j].isMarked) {
                    return cells[i][j].getNumber();
                }
            }
        }
        return 0;
    }

    public void addNoteToCell(int row, int col, int note) {
        cells[row][col].addNote(note);
    }

    public void clearNotes() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j].clearNotes();
            }
        }
    }

    public void removeNoteFromCell(int row, int col, int note) {
        cells[row][col].removeNote(note);
    }

    public Set<Integer> getNotesInCell(int row, int col) {
        return cells[row][col].getNotes();
    }

    public void setHiddenProperty(int row, int col, boolean hideNotes) {
        cells[row][col].shouldHideNotes = hideNotes;
    }

    public void addToHideList(int row, int col, int number) {
        cells[row][col].hideList.add(number);
    }

    public void removeFromHideList(int row, int col, int number) {
        cells[row][col].hideList.remove(number);
    }

    public Set<Integer> getHideList(int row, int col) {
        return cells[row][col].hideList;
    }

    public boolean getHiddenProperty(int row, int col) {
        return cells[row][col].shouldHideNotes;
    }

    public void setCellTextColor(int row, int col, Color color) {
        cells[row][col].setTextColor(color);
        repaint();
    }

    public void clearWrongNumbers() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j].setWrongNumber(0);
            }
        }
    }

    public void update() {
        accentColor = Config.getDarkMode() ? new Color(237, 224, 186) : Color.BLACK;
        revalidate();
        repaint();
    }

    public void addCage(int cageId, Cage cage) {
        cages.put(cageId, cage);
        for (Point cell : cage.getCells()) {
            cellToCageMap.put(cell, cageId);
        }
    }

    public List<Cage> getCages() {
        return new ArrayList<>(cages.values());
    }

    public Cage getCage(int row, int col) {
        Point cell = new Point(col, row);
        Integer cageId = cellToCageMap.get(cell);
        if (cageId != null) {
            return cages.get(cageId);
        }
        return null;
    }

    public void clearCages() {
        cages.clear();
        repaint();
    }

    public int[][] getCagesIntArray() {
        int[][] cagesArray = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Cage cage = getCage(i, j);
                if (cage != null) {
                    cagesArray[i][j] = cage.getId();
                } else {
                    cagesArray[i][j] = 0;
                }
            }
        }
        return cagesArray;
    }

    public void addCages(int[][] cages, Board board) {
        clearCages();
        Map<Integer, List<Point>> cageMap = new HashMap<>();
        int cageId;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cageId = cages[row][col];
                if (cageId != 0) {
                    cageMap.computeIfAbsent(cageId, k -> new ArrayList<>())
                            .add(new Point(col, row));
                }
            }
        }

        for (Map.Entry<Integer, List<Point>> entry : cageMap.entrySet()) {
            int sum = 0;
            for (Point cell : entry.getValue()) {
                sum += board.getNumber(cell.x, cell.y); // Use gameboard to get the number
            }
            addCage(entry.getKey(), new Cage(entry.getValue(), sum, entry.getKey()));
        }
    }

    public void setNotes(String notes) {
        String[] noteData = notes.split(";");
        for (String note : noteData) {
            String[] parts = note.split(":");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            String[] notesData = parts[2].split(",");
            for (String noteData1 : notesData) {
                cells[row][col].addNote(Integer.parseInt(noteData1));
            }
        }
    }

    public String getNotes() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (!cells[i][j].getNotes().isEmpty()) {
                    builder.append(i).append(":").append(j).append(":");
                    for (Integer note : cells[i][j].getNotes()) {
                        builder.append(note).append(",");
                    }
                    builder.setLength(builder.length() - 1); // Remove last comma
                    builder.append(";");
                }
            }
        }
        return builder.toString();
    }
}
