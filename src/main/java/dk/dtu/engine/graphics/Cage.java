/* (C)2024 */
package dk.dtu.engine.graphics;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cage {
    private final List<Point> cells;
    private final int sum;
    private final Set<Integer> currentNumbers; // Track numbers in the cage
    private final int id; // Unique ID for the cage

    public Cage(List<Point> cells, int sum, int id) {
        this.cells = cells;
        this.sum = sum;
        this.id = id;
        this.currentNumbers = new HashSet<>();
    }

    public List<Point> getCells() {
        return cells;
    }

    public int getSum() {
        return sum;
    }

    public boolean contains(int number) {
        return currentNumbers.contains(number);
    }

    public int getId() {
        return id;
    }

    public Integer[] getNumbers() {
        return currentNumbers.toArray(new Integer[0]);
    }

    public void addCurrentNumber(int number) {
        currentNumbers.add(number);
    }

    public void removeCurrentNumber(int number) {
        currentNumbers.remove(number);
    }

    public Set<Integer> getCurrentNumbers() {
        return currentNumbers;
    }
}
