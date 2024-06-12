package dk.dtu.engine.utility;

import javax.swing.*;
import java.awt.*;

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
