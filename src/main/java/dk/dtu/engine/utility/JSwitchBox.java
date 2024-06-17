/* (C)2024 */
package dk.dtu.engine.utility;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * This class makes a custom Component that acts like a slider for the game rules, it has an on/off state and is toggleable.
 */
public class JSwitchBox extends JComponent {
    private boolean selected;
    private static final int BOX_WIDTH = 100;
    private static final int BOX_HEIGHT = 40;
    private final Color green = new Color(130, 160, 130);
    private final Color red = new Color(160, 130, 130);
    private final Color sliderColor = new Color(220, 220, 220);
    private final transient Consumer<Boolean> toggleAction;

    public JSwitchBox(boolean initialState, Consumer<Boolean> toggleAction) {
        this.selected = initialState;
        this.toggleAction = toggleAction;
        setPreferredSize(new Dimension(BOX_WIDTH, BOX_HEIGHT));
        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (new Rectangle(getPreferredSize()).contains(e.getPoint())) {
                            toggle();
                        }
                    }
                });
    }

    private void toggle() {
        selected = !selected;
        toggleAction.accept(selected);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2.setColor(selected ? green : red);
        g2.fillRoundRect(0, 0, BOX_WIDTH, BOX_HEIGHT, 20, 20);

        // Draw slider
        g2.setColor(sliderColor);
        int sliderX = selected ? BOX_WIDTH - BOX_HEIGHT : 0;
        g2.fillRoundRect(
                sliderX, 0, 40, 40, 20, 20);
    }
}
