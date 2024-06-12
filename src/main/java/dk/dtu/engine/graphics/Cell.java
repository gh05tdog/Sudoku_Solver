/* (C)2024 */
package dk.dtu.engine.graphics;

import dk.dtu.game.core.Config;

import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

class Cell implements Serializable {

    boolean isMarked = false;
    boolean isHighlighted = false;
    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private Color backgroundColor = Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background
    private Color accentColor;

    private Color highlightColor = Config.getDarkMode() ? new Color(105, 104, 104): new Color(225, 223, 221);
    private Color markedColor = Config.getDarkMode() ?  new Color(84, 84, 84)  : new Color(169, 169, 167);
    private Color placeAbleColor = Config.getDarkMode() ? new Color(119, 111, 73) : new Color(250, 200, 200);

    private Color textColor;
    boolean isUnplacable = false;

    private int number = 0; // 0 indicates no number
    private int wrongNumber = 0;

    boolean isVisualizingHint = false;
    boolean wasHighlightedBeforeHint = false;

    private final Set<Integer> notes;
    transient Set<Integer> hideList;

    boolean shouldHideNotes = false;

    public Cell() {
        this.notes = new HashSet<>();
        this.hideList = new HashSet<>();
        this.accentColor = Config.getDarkMode() ? new Color(237, 224, 186) : Color.BLACK;
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
        if (isUnplacable && currentNumber != 0) {
            g.setColor(placeAbleColor);
        } else if (isMarked) {
            g.setColor(markedColor);
        } else if (isHighlighted) {
            g.setColor(highlightColor);
        } else {
            g.setColor(backgroundColor);
        }
        g.fillRect(x, y, cellSize, cellSize);

        if (number > 0) {
            if (number == wrongNumber) {
                g.setColor(Color.RED);
            } else if (number == currentNumber) {
                g.setColor(new Color(96, 115, 215));
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

        g.setColor(accentColor);
        g.drawRect(x, y, cellSize, cellSize);
    }

    public void paintNotes(Graphics g2, int x, int y, int cellSize) {
        if (shouldHideNotes) {
            return;
        }
        g2.setColor(accentColor);
        Font font = new Font("Arial", Font.BOLD, cellSize / 5);
        g2.setFont(font);
        int subCellSize = cellSize / 3; // Divide the cellSize by 3 to get the size of each sub-cell
        int offsetX =
                (subCellSize / 2) + 2; // Half of the subCellSize to center the number horizontally
        int offsetY = (cellSize / 6 + subCellSize / 2) - 5; // Position for vertical centering

        for (int note : notes) {
            String noteStr = Integer.toString(note);
            if (hideList.contains(note) || note == 0) {
                continue;
            }
            int row = (note - 1) / 3;
            int col = (note - 1) % 3;
            g2.drawString(
                    noteStr,
                    x + col * subCellSize + offsetX - g2.getFontMetrics().stringWidth(noteStr) / 2,
                    y + row * subCellSize + offsetY);
        }
        g2.setColor(accentColor);
        g2.drawRect(x, y, cellSize, cellSize);
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

    public void setUnplaceableCell(boolean isUnplacable) {
        this.isUnplacable = isUnplacable;
    }
}
