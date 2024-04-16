package dk.dtu.engine.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class Cell {
    boolean isMarked = false;
    boolean isHighlighted = false;
    private Color backgroundColor = Color.WHITE;
    private int number = 0; // 0 indicates no number

    boolean isVisualizingHint = false;
    boolean wasHighlightedBeforeHint = false;

    public Cell() {

    }

    public void startHintVisualization() {
        wasHighlightedBeforeHint = isHighlighted;
        isVisualizingHint = true;
        isHighlighted = false;
    }

    public void endHintVisualization() {
        isVisualizingHint = false;
        isHighlighted = wasHighlightedBeforeHint;
    }

    public void paintCell(Graphics g, int x, int y, int cellSize, int currentNumber) {
        if (isHighlighted) {
            g.setColor(Color.LIGHT_GRAY);
            if (isMarked) {
                g.setColor(Color.DARK_GRAY);
            }
        } else {
            g.setColor(backgroundColor);
        }
        g.fillRect(x, y, cellSize, cellSize);

        if (number > 0) {
            if (number == currentNumber) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
            Font font = new Font("Arial", Font.BOLD, cellSize / 2);
            g.setFont(font);
            String numberStr = Integer.toString(number);
            g.drawString(numberStr, x + cellSize / 2 - g.getFontMetrics().stringWidth(numberStr) / 2,
                    y + cellSize / 2 + g.getFontMetrics().getAscent() / 2);
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, cellSize, cellSize);
    }


    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}

public class SudokuBoardCanvas extends JPanel {
    private final int gridSize;
    private final int cellSize;
    private final Cell[][] cells;

    int chosenNumber = 0;

    public SudokuBoardCanvas(int n, int k, int cellSize) {
        this.gridSize = n * k;
        this.cellSize = cellSize;
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
            }
        }
        drawSubGrids(g);
    }

    private void drawSubGrids(Graphics g) {
        int subGridSize = (int) Math.sqrt(gridSize);
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
            int subGridSize = (int) Math.sqrt(gridSize);
            int subGridRowStart = (row / subGridSize) * subGridSize;
            int subGridColStart = (col / subGridSize) * subGridSize;

            // Highlight the entire row and column
            for (int i = 0; i < gridSize; i++) {
                cells[row][i].setHighlighted(highlight); // Highlight row
                cells[i][col].setHighlighted(highlight); // Highlight column
            }

            // Highlight the subgrid
            for (int subRow = subGridRowStart; subRow < subGridRowStart + subGridSize; subRow++) {
                for (int subCol = subGridColStart; subCol < subGridColStart + subGridSize; subCol++) {
                    cells[subRow][subCol].setHighlighted(highlight);
                }
            }

            // Set the clicked cell as highlighted to keep track of the last clicked cell
            cells[row][col].setHighlighted(highlight);
            repaint();
        }
    }

    public void visualizeCell(int row, int col, Color startColor) {

        final int totalSteps = 10; // Total steps to fade color
        final int delay = 100;
        Cell cell = cells[row][col];
        cell.startHintVisualization();


        ActionListener fadeAction = new ActionListener() {
            private int step = 0;

            public void actionPerformed(ActionEvent e) {
                if (step < totalSteps) {
                    // Calculate the step color
                    int r = startColor.getRed() + (Color.WHITE.getRed() - startColor.getRed()) * step / totalSteps;
                    int g = startColor.getGreen() + (Color.WHITE.getGreen() - startColor.getGreen()) * step / totalSteps;
                    int b = startColor.getBlue() + (Color.WHITE.getBlue() - startColor.getBlue()) * step / totalSteps;
                    Color stepColor = new Color(r, g, b);
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
        };

        new Timer(delay, fadeAction).start();
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
                    return new int[]{i, j};
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
    }

    public boolean isACellHighligthed() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cells[row][col].isHighlighted) {
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
}
