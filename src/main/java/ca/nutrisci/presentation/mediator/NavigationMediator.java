package ca.nutrisci.presentation.mediator;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * NavigationMediator - Handles navigation between different UI panels.
 * Part of the Presentation Layer - Mediator Pattern
 * This class decouples the UI panels from each other, allowing them to
 * communicate and switch views through the mediator.
 */
public class NavigationMediator {

    private JTabbedPane tabbedPane;
    private Map<String, JComponent> panels;

    /**
     * Constructor for NavigationMediator.
     * @param tabbedPane The main tabbed pane that holds the UI panels.
     */
    public NavigationMediator(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        this.panels = new HashMap<>();
    }

    /**
     * Registers a UI panel with a unique name.
     * @param name The name of the panel to register.
     * @param panel The UI component (e.g., JPanel) to register.
     */
    public void registerPanel(String name, JComponent panel) {
        panels.put(name, panel);
        tabbedPane.addTab(name, panel);
    }

    /**
     * Switches the view to the specified panel.
     * @param name The name of the panel to navigate to.
     */
    public void navigateTo(String name) {
        JComponent panel = panels.get(name);
        if (panel != null) {
            tabbedPane.setSelectedComponent(panel);
        } else {
            System.err.println("Navigation error: Panel '" + name + "' not registered.");
        }
    }

    /**
     * Gets the currently selected panel.
     * @return The currently visible JComponent.
     */
    public JComponent getCurrentPanel() {
        return (JComponent) tabbedPane.getSelectedComponent();
    }
    
    /**
     * Gets the name of the currently selected panel.
     * @return The title of the currently selected tab.
     */
    public String getCurrentPanelName() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            return tabbedPane.getTitleAt(selectedIndex);
        }
        return null;
    }
} 