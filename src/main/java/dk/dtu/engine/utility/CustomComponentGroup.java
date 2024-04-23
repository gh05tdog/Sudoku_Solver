package dk.dtu.engine.utility;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomComponentGroup {


    private final List<CustomBoardPanel> components = new ArrayList<>();

    public final List<CustomBoardPanel> components = new ArrayList<>();
    public CustomBoardPanel selectedComponent = null;

    public void addComponent(CustomBoardPanel component) {
        components.add(component);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedComponent(component);
            }
        });
    }

    private void setSelectedComponent(CustomBoardPanel component) {
        selectedComponent(component);

    }

    private void selectedComponent(CustomBoardPanel component) {
        deselectComponent(component);
        component.updateBackgroundColor(Color.LIGHT_GRAY);

    }

    private void deselectComponent(CustomBoardPanel component) {
        for(CustomBoardPanel c : components) {
            if(c != component) {
                c.updateBackgroundColor(Color.WHITE);
            }
        }

    }
}
