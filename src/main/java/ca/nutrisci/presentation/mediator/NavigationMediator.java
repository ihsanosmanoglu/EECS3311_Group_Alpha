package ca.nutrisci.presentation.mediator;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * NavigationMediator - Handles navigation between different UI panels in the main application
 * 
 * PURPOSE:
 * - Decouples UI panels from each other using the Mediator pattern
 * - Provides centralized navigation control for the tabbed interface
 * - Manages panel registration and tab switching functionality
 * - Supports the main application's multi-panel architecture
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only navigation and panel coordination
 * - Open/Closed: Extensible for new panels without modifying existing code
 * - Liskov Substitution: Works with any JComponent implementations
 * - Interface Segregation: Provides only necessary navigation methods
 * - Dependency Inversion: Depends on JTabbedPane abstraction, not specific implementations
 * 
 * DESIGN PATTERNS:
 * - Mediator Pattern: Centralizes communication between UI panels
 * - Registry Pattern: Maintains a registry of available panels
 * 
 * USAGE:
 * - Integrated into MainApplication for tab management
 * - Panels register themselves during application initialization
 * - Supports both programmatic and user-driven navigation
 * - Provides current panel information for context-aware operations
 */
public class NavigationMediator {

    // Core UI component - the main tabbed pane (Dependency Inversion)
    private final JTabbedPane tabbedPane;
    
    // Panel registry - maps panel names to components (Registry Pattern)
    private final Map<String, JComponent> panels;

    /**
     * Constructor for NavigationMediator
     * Follows Dependency Inversion - depends on JTabbedPane abstraction
     * 
     * @param tabbedPane The main tabbed pane that holds the UI panels
     */
    public NavigationMediator(JTabbedPane tabbedPane) {
        if (tabbedPane == null) {
            throw new IllegalArgumentException("TabbedPane cannot be null");
        }
        
        this.tabbedPane = tabbedPane;
        this.panels = new HashMap<>();
    }

    /**
     * Registers a UI panel with a unique name
     * Follows Single Responsibility - handles only panel registration
     * 
     * @param name The unique name of the panel to register
     * @param panel The UI component (e.g., JPanel) to register
     * @throws IllegalArgumentException if name or panel is null, or if name already exists
     */
    public void registerPanel(String name, JComponent panel) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Panel name cannot be null or empty");
        }
        
        if (panel == null) {
            throw new IllegalArgumentException("Panel component cannot be null");
        }
        
        if (panels.containsKey(name)) {
            throw new IllegalArgumentException("Panel with name '" + name + "' is already registered");
        }
        
        panels.put(name, panel);
        tabbedPane.addTab(name, panel);
    }

    /**
     * Switches the view to the specified panel
     * Follows Single Responsibility - handles only navigation logic
     * 
     * @param name The name of the panel to navigate to
     * @return true if navigation was successful, false if panel not found
     */
    public boolean navigateTo(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Navigation error: Panel name cannot be null or empty");
            return false;
        }
        
        JComponent panel = panels.get(name);
        if (panel != null) {
            tabbedPane.setSelectedComponent(panel);
            return true;
        } else {
            System.err.println("Navigation error: Panel '" + name + "' not registered.");
            return false;
        }
    }

    /**
     * Gets the currently selected panel component
     * Follows Interface Segregation - provides only necessary component access
     * 
     * @return The currently visible JComponent, or null if no panel is selected
     */
    public JComponent getCurrentPanel() {
        return (JComponent) tabbedPane.getSelectedComponent();
    }
    
    /**
     * Gets the name of the currently selected panel
     * Follows Interface Segregation - provides only necessary information access
     * 
     * @return The title of the currently selected tab, or null if no tab is selected
     */
    public String getCurrentPanelName() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            return tabbedPane.getTitleAt(selectedIndex);
        }
        return null;
    }
    
    /**
     * Gets the number of registered panels
     * Follows Interface Segregation - provides only necessary count information
     * 
     * @return The number of panels currently registered
     */
    public int getPanelCount() {
        return panels.size();
    }
    
    /**
     * Gets a set of all registered panel names
     * Follows Interface Segregation - provides only necessary registry information
     * 
     * @return A set containing all registered panel names
     */
    public Set<String> getRegisteredPanelNames() {
        return panels.keySet();
    }
    
    /**
     * Checks if a panel with the given name is registered
     * Follows Single Responsibility - handles only panel existence checking
     * 
     * @param name The name of the panel to check
     * @return true if the panel is registered, false otherwise
     */
    public boolean isRegistered(String name) {
        return name != null && panels.containsKey(name);
    }
    
    /**
     * Unregisters a panel by name (useful for dynamic panel management)
     * Follows Single Responsibility - handles only panel removal
     * 
     * @param name The name of the panel to unregister
     * @return true if the panel was found and removed, false otherwise
     */
    public boolean unregisterPanel(String name) {
        if (name == null || !panels.containsKey(name)) {
            return false;
        }
        
        JComponent panel = panels.remove(name);
        tabbedPane.remove(panel);
        return true;
    }
    
    /**
     * Sets the enabled state of a specific tab
     * Follows Single Responsibility - handles only tab state management
     * 
     * @param name The name of the panel whose tab should be enabled/disabled
     * @param enabled true to enable the tab, false to disable it
     * @return true if the operation was successful, false if panel not found
     */
    public boolean setTabEnabled(String name, boolean enabled) {
        if (!isRegistered(name)) {
            return false;
        }
        
        // Find the tab index for the panel
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (name.equals(tabbedPane.getTitleAt(i))) {
                tabbedPane.setEnabledAt(i, enabled);
                return true;
            }
        }
        
        return false;
    }
} 