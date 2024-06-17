/* (C)2024 */
package dk.dtu.engine.utility;

import dk.dtu.game.core.Config;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom component group that contains a list of custom board panels, this makes them selectable, just like a Togglebutton.
 * When clicking one panel all the other panels deselects
 */
public class CustomComponentGroup {

    public final List<CustomBoardPanel> components = new ArrayList<>();
    private CustomBoardPanel selectedComponent;

    public void addComponent(CustomBoardPanel component) {
        components.add(component);
        component.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        setSelectedComponent(component);
                    }
                });
    }

    private void setSelectedComponent(CustomBoardPanel component) {
        selectedComponent(component);
        selectedComponent = component;
    }

    private void selectedComponent(CustomBoardPanel component) {
        deselectComponent(component);
        component.updateBackgroundColor(getSelectedColor());
    }

    private void deselectComponent(CustomBoardPanel component) {
        for (CustomBoardPanel c : components) {
            if (c != component) {
                c.updateBackgroundColor(getBackgroundColor());
            }
        }
    }

    private Color getBackgroundColor() {
        return Config.getDarkMode() ? new Color(64, 64, 64) : Color.WHITE;
    }

    private Color getSelectedColor() {
        return Config.getDarkMode() ? new Color(80, 80, 80) : Color.LIGHT_GRAY;
    }

    public CustomBoardPanel getSelectedComponent() {
        return selectedComponent;
    }
}
