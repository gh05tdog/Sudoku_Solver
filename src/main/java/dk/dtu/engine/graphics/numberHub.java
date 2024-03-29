package dk.dtu.engine.graphics;

import javax.swing.*;
import java.awt.*;

public class numberHub extends JPanel {

    private final int gridSize;
    private final int cellSize;

    public numberHub(int gridSize, int cellSize) {
        this.gridSize = gridSize;
        this.cellSize = cellSize;
        setPreferredSize(new Dimension(50, gridSize * cellSize));

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 555, gridSize * cellSize);

    }


}
