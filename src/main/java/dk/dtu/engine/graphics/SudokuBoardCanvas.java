/* (C)2024 */
package dk.dtu.engine.graphics;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SudokuBoardCanvas extends JPanel {
    private final int gridSize;
    private final int cellSize;
    private final int nSize;
    private final Cell[][] cells;

    private static final Logger logger = LoggerFactory.getLogger(SudokuBoardCanvas.class);

    int chosenNumber = 0;

    public SudokuBoardCanvas(int n, int k, int cellSize) {
        this.gridSize = n * k;
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
    }

    private void drawSubGrids(Graphics g) {
        int subGridSize = nSize;
        g.setColor(Color.BLACK);

        for (int i = 0; i <= gridSize; i += subGridSize) {
            int pos = i * cellSize;
            if (i % subGridSize == 0) {
                g.fillRect(pos - 2, 0, 4, gridSize * cellSize);
                g.fillRect(0, pos - 2, gridSize * cellSize, 4);
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
                                            + (Color.WHITE.getRed() - startColor.getRed())
                                                    * step
                                                    / totalSteps;
                            Color stepColor = getColor(r);
                            cell.setBackgroundColor(stepColor);
                            repaint();
                            step++;
                        } else {
                            // Stop the timer and reset the background color to white at the end
                            ((Timer) e.getSource()).stop();
                            cell.setBackgroundColor(Color.WHITE);
                            cell.endHintVisualization();
                            repaint();
                        }
                    }

                    private Color getColor(int r) {
                        int g =
                                startColor.getGreen()
                                        + (Color.WHITE.getGreen() - startColor.getGreen())
                                                * step
                                                / totalSteps;
                        int b =
                                startColor.getBlue()
                                        + (Color.WHITE.getBlue() - startColor.getBlue())
                                                * step
                                                / totalSteps;
                        return new Color(r, g, b);
                    }
                };

        new Timer(delay, fadeAction).start();
        // Set the cell color to the start color
        cell.setTextColor(Color.BLUE);
    }

    private void clearHighlights() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col].setHighlighted(false);
            }
        }
    }

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

    public void setMarkedCell(int row, int col) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j].isMarked = false;
            }
        }
        cells[row][col].isMarked = true;
        logger.debug("Setting the marked cell {} {}", row, col);
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
}
