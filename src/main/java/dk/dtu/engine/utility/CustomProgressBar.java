/* (C)2024 */
package dk.dtu.engine.utility;

import java.awt.*;
import javax.swing.*;

public class CustomProgressBar extends JProgressBar {
    private CustomProgressBarUI customUI;

    public CustomProgressBar(int min, int max) {
        super(min, max);
        customUI = new CustomProgressBarUI();
        setUI(customUI);
        setStringPainted(true);
    }

    public void setTextColor(Color color) {
        customUI.setTextColor(color);
        repaint();
    }
}
