/* (C)2024 */
package dk.dtu.engine.graphics;

import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

class Cell implements Serializable {

    boolean isMarked = false;
    boolean isHighlighted = false;
    private Color backgroundColor = Color.WHITE;
    boolean unPlaceable = false;

    private int number = 0; // 0 indicates no number
    private int wrongNumber = 0;

    boolean isVisualizingHint = false;
    boolean wasHighlightedBeforeHint = false;

    private final Set<Integer> notes;
    transient Set<Integer> hideList;

    boolean shouldHideNotes = false;
    private Color textColor;

    public Cell() {
        this.notes = new HashSet<>();
        this.hideList = new HashSet<>();
        this.textColor = Color.BLACK;
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
        if (unPlaceable && currentNumber != 0) {
            g.setColor(new Color(250, 200, 200));
        } else if (isMarked) {
            g.setColor(new Color(169, 169, 167));
        } else if (isHighlighted) {
            g.setColor(new Color(225, 223, 221));
        } else {
            g.setColor(backgroundColor);
        }
        g.fillRect(x, y, cellSize, cellSize);

        if (number > 0) {
            if (number == wrongNumber) {
                g.setColor(Color.RED);
            } else if (number == currentNumber) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(textColor);
            }
            Font font = new Font("Arial", Font.BOLD, cellSize / 2);
            g.setFont(font);
            String numberStr = Integer.toString(number);
            g.drawString(
                    numberStr,
                    x + cellSize / 2 - g.getFontMetrics().stringWidth(numberStr) / 2,
                    y + cellSize / 2 + g.getFontMetrics().getAscent() / 2);
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, cellSize, cellSize);
    }

    public void paintNotes(Graphics g, int x, int y, int cellSize) {
        if (shouldHideNotes) {
            return;
        }
        g.setColor(textColor);
        Font font = new Font("Arial", Font.BOLD, cellSize / 6); // Smaller font size
        g.setFont(font);
        int subCellSize = cellSize / 3 - 6; // Further reduce the sub-cell size to bring notes closer
        int gridOffsetX = ((cellSize - (subCellSize * 3)) / 2); // Center the 3x3 grid horizontally
        int gridOffsetY = ((cellSize - (subCellSize * 3)) / 2) + 2; // Center the 3x3 grid vertically
        int offsetX = subCellSize / 2; // Center the number horizontally within sub-cell
        int offsetY = subCellSize / 2 + g.getFontMetrics().getAscent() / 3; // Center the number vertically within sub-cell

        for (int note : notes) {
            String noteStr = Integer.toString(note);
            if (hideList.contains(note) || note == 0) {
                continue;
            }
            int row = (note - 1) / 3;
            int col = (note - 1) % 3;
            g.drawString(
                    noteStr,
                    x + col * subCellSize + gridOffsetX + offsetX - g.getFontMetrics().stringWidth(noteStr) / 2,
                    y + row * subCellSize + gridOffsetY + offsetY - g.getFontMetrics().getDescent() / 2
            );
        }
        g.setColor(Color.BLACK);
        g.drawRect(x, y, cellSize, cellSize);
    }






    public void addNote(int note) {
        notes.add(note);
    }

    public void clearNotes() {
        notes.clear();
    }

    public void removeNote(int note) {
        notes.remove(note);
    }

    public Set<Integer> getNotes() {
        return notes;
    }

    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setWrongNumber(int number) {
        this.wrongNumber = number;
    }

    public int getNumber() {
        return number;
    }

    public void setUnplaceableCell(boolean unPlaceable) {
        this.unPlaceable = unPlaceable;
    }
}
