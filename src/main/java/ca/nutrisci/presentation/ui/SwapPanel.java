package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.ISwapFacade;
import ca.nutrisci.application.services.observers.ProfileChangeListener;

import javax.swing.*;
import java.awt.*;

/**
 * SwapPanel - Main panel for food swap functionality
 * 
 * PURPOSE:
 * - Provides interface for getting food swap suggestions
 * - Allows users to apply food swaps to their meals
 * - Displays nutritional impact of proposed swaps
 * - Follows the same architecture pattern as MealLogPanel
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Only depends on Application layer (ISwapFacade)
 * - DD-2: Four Domain Façades - Uses ISwapFacade for swap operations
 * - DD-8: Observer Pattern - Implements ProfileChangeListener for profile updates
 * - DD-9: Naming Conventions - SwapPanel follows established naming patterns
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only swap UI coordination
 * - Open/Closed: Extensible for additional swap features
 * - Liskov Substitution: Can be used anywhere a JPanel is expected
 * - Interface Segregation: Uses specific facade interfaces only
 * - Dependency Inversion: Depends on abstractions, not concrete implementations
 */
public class SwapPanel extends JPanel implements ProfileChangeListener {
    
    // Core dependencies (DD-1: Layered Architecture, DD-2: Domain Façades)
    private final ISwapFacade swapFacade;
    
    // UI Components
    private JPanel goalSelectionPanel;
    private JPanel suggestionsPanel;
    private JPanel mealSelectionPanel;
    
    // Current state
    private ProfileDTO activeProfile;
    
    /**
     * Constructor - Initializes the swap panel
     * 
     * @param swapFacade Facade for swap operations (DD-2: Domain Façades)
     * @param activeProfile Currently active user profile
     */
    public SwapPanel(ISwapFacade swapFacade, ProfileDTO activeProfile) {
        this.swapFacade = swapFacade;
        this.activeProfile = activeProfile;
        
        // Template Method pattern - standardized initialization sequence
        initializeComponents();
        layoutComponents();
        
        System.out.println("✅ SwapPanel initialized for profile: " + 
                         (activeProfile != null ? activeProfile.getName() : "none"));
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Goal Selection Panel - where users choose their nutrition goals
        goalSelectionPanel = createGoalSelectionPanel();
        
        // Suggestions Panel - where swap suggestions are displayed
        suggestionsPanel = createSuggestionsPanel();
        
        // Meal Selection Panel - where users select which meal to apply swaps to
        mealSelectionPanel = createMealSelectionPanel();
        
        System.out.println("✅ SwapPanel components initialized");
    }
    
    /**
     * Create the goal selection panel
     */
    private JPanel createGoalSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nutrition Goals"));
        panel.setPreferredSize(new Dimension(350, 300));
        
        // Title
        JLabel titleLabel = new JLabel("Select Your Nutrition Goals", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Goal checkboxes panel
        JPanel goalsPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        goalsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Create checkboxes for each nutrition goal
        String[] goals = {"Calories", "Protein", "Carbohydrates", "Fat", "Fiber"};
        String[] actions = {"Decrease", "Increase"};
        
        for (String goal : goals) {
            for (String action : actions) {
                JCheckBox checkbox = new JCheckBox(action + " " + goal);
                checkbox.setFont(new Font("Arial", Font.PLAIN, 12));
                goalsPanel.add(checkbox);
            }
        }
        
        // Get Suggestions button
        JButton getSuggestionsBtn = new JButton("Get Swap Suggestions");
        getSuggestionsBtn.setFont(new Font("Arial", Font.BOLD, 14));
        getSuggestionsBtn.setBackground(new Color(76, 175, 80));
        getSuggestionsBtn.setForeground(Color.WHITE);
        getSuggestionsBtn.setFocusPainted(false);
        getSuggestionsBtn.setPreferredSize(new Dimension(200, 40));
        getSuggestionsBtn.addActionListener(e -> getSuggestions());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(getSuggestionsBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(goalsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the suggestions display panel
     */
    private JPanel createSuggestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Swap Suggestions"));
        panel.setPreferredSize(new Dimension(400, 300));
        
        // Placeholder content
        JLabel placeholderLabel = new JLabel(
            "<html><div style='text-align: center;'>" +
            "Select your nutrition goals and click<br/>" +
            "'Get Swap Suggestions' to see recommendations<br/><br/>" +
            "Suggestions will appear here with:<br/>" +
            "• Food replacement options<br/>" +
            "• Nutritional impact<br/>" +
            "• Apply buttons" +
            "</div></html>", 
            SwingConstants.CENTER
        );
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        placeholderLabel.setForeground(Color.GRAY);
        
        panel.add(placeholderLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create the meal selection panel
     */
    private JPanel createMealSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Apply to Meal"));
        panel.setPreferredSize(new Dimension(350, 300));
        
        // Meal selection dropdown
        JLabel selectLabel = new JLabel("Select a meal to apply swaps to:");
        selectLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        selectLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JComboBox<String> mealComboBox = new JComboBox<>();
        mealComboBox.addItem("Select a meal...");
        mealComboBox.addItem("Today's Breakfast");
        mealComboBox.addItem("Today's Lunch");
        mealComboBox.addItem("Today's Dinner");
        mealComboBox.addItem("Recent meals...");
        mealComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        mealComboBox.setPreferredSize(new Dimension(250, 30));
        
        // Instructions
        JTextArea instructionsArea = new JTextArea(
            "Instructions:\n\n" +
            "1. Select your nutrition goals above\n" +
            "2. Click 'Get Swap Suggestions'\n" +
            "3. Choose a meal to modify\n" +
            "4. Apply suggested swaps\n\n" +
            "The system will analyze your current meals\n" +
            "and suggest healthier alternatives that\n" +
            "meet your nutrition goals."
        );
        instructionsArea.setEditable(false);
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionsArea.setBackground(getBackground());
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(selectLabel, BorderLayout.NORTH);
        topPanel.add(mealComboBox, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(instructionsArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Layout all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main container with proper spacing
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Food Swap Engine - Smart Nutrition Recommendations", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Layout the three panels horizontally
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        contentPanel.add(goalSelectionPanel);
        contentPanel.add(suggestionsPanel);
        contentPanel.add(mealSelectionPanel);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        System.out.println("✅ SwapPanel layout completed");
    }
    
    /**
     * Handle getting swap suggestions (placeholder for now)
     */
    private void getSuggestions() {
        // TODO: Phase 2 - Implement actual suggestion logic
        JOptionPane.showMessageDialog(this, 
            "Swap suggestion feature coming in Phase 2!\n\n" +
            "This will:\n" +
            "• Analyze your selected goals\n" +
            "• Generate smart food swaps\n" +
            "• Show nutritional impact\n" +
            "• Allow you to apply changes", 
            "Feature Preview", 
            JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("🎯 Swap suggestions requested - Phase 2 implementation pending");
    }
    
    // ProfileChangeListener implementation (DD-8: Observer Pattern)
    
    @Override
    public void onProfileActivated(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
        System.out.println("✅ SwapPanel updated for new active profile: " + activeProfile.getName());
    }
    
    @Override
    public void onProfileDeactivated(ProfileDTO deactivatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(deactivatedProfile.getId())) {
            this.activeProfile = null;
        }
    }
    
    @Override
    public void onProfileCreated(ProfileDTO newProfile) {
        // No specific action needed
    }
    
    @Override
    public void onProfileUpdated(ProfileDTO updatedProfile) {
        if (activeProfile != null && activeProfile.getId().equals(updatedProfile.getId())) {
            this.activeProfile = updatedProfile;
        }
    }
    
    @Override
    public void onProfileDeleted(String deletedProfileId) {
        if (activeProfile != null && activeProfile.getId().toString().equals(deletedProfileId)) {
            this.activeProfile = null;
            System.out.println("Active profile deleted - cleared swap panel data");
        }
    }
    
    // Public API methods
    
    public ProfileDTO getActiveProfile() {
        return activeProfile;
    }
    
    public void setActiveProfile(ProfileDTO activeProfile) {
        this.activeProfile = activeProfile;
    }
    
    public void refreshPanel() {
        // Refresh logic for external triggers
        repaint();
        System.out.println("✅ SwapPanel refreshed");
    }
} 