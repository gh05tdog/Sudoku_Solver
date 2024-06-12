package dk.dtu.engine.utility;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class CustomProgressBarUI extends BasicProgressBarUI {
    private Color textColor = Color.BLACK;

    public void setTextColor(Color color) {
        this.textColor = color;
    }

    @Override
    protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
        if (progressBar.isStringPainted()) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(textColor);
            String progressString = progressBar.getString();
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int stringWidth = fontMetrics.stringWidth(progressString);
            int stringHeight = fontMetrics.getAscent();
            int textX = (progressBar.getWidth() - stringWidth) / 2;
            int textY = (progressBar.getHeight() + stringHeight) / 2 - 1;
            g2d.drawString(progressString, textX, textY);
            g2d.dispose();
        }
    }
}

