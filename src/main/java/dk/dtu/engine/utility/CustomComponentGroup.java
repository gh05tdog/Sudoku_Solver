/* (C)2024 */
package dk.dtu.engine.utility;

import dk.dtu.game.core.Config;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomComponentGroup {

    private static final Color darkModebackgroundColor = new Color(64, 64, 64);
    private static final Color backgroundColor =
            Config.getDarkMode() ? darkModebackgroundColor : Color.WHITE; // Default background
    private static final Color lightGray = new Color(80, 80, 80);
    private static final Color selectedColor = Config.getDarkMode() ? lightGray : Color.LIGHT_GRAY;
    public final List<CustomBoardPanel> components = new ArrayList<>();
    private CustomBoardPanel selectedComponent = null;

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
        component.updateBackgroundColor(selectedColor);
    }

    private void deselectComponent(CustomBoardPanel component) {
        for (CustomBoardPanel c : components) {
            if (c != component) {
                c.updateBackgroundColor(backgroundColor);
            }
        }
    }

    public CustomBoardPanel getSelectedComponent() {
        return selectedComponent;
    }
}
