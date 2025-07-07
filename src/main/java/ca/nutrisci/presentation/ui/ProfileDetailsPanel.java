package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IProfileFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ProfileDetailsPanel - Panel for displaying and editing the selected profile's details
 * 
 * PURPOSE:
 * - Shows detailed information for the currently selected/active profile
 * - Allows users to edit profile information (name, age, weight, height, units)
 * - Provides a clean interface for profile management within the main application
 * - Replaces the old ProfilePanel with focused functionality for active profile only
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only profile details display and editing
 * - Open/Closed: Extensible for additional profile detail features  
 * - Liskov Substitution: Can be used anywhere a JPanel is expected
 * - Interface Segregation: Uses specific IProfileFacade interface only
 * - Dependency Inversion: Depends on IProfileFacade abstraction, not concrete implementation
 * 
 * DESIGN PATTERNS:
 * - Facade Pattern: Uses ProfileManagement facade for profile operations
 * - Observer Pattern: Listens to user interactions and profile changes
 * - Template Method: Follows standard panel initialization pattern
 * 
 * USAGE:
 * - Integrated into main application after profile selection
 * - Displays active profile information
 * - Handles profile updates and validation
 * - Provides BMI calculation and health suggestions
 */
public class ProfileDetailsPanel extends JPanel {
    
    // Core dependencies - injected via constructor (Dependency Inversion)
    private final IProfileFacade profileFacade;
    
    // Current profile state
    private ProfileDTO currentProfile;
    
    // Display components - read-only information
    private JLabel nameDisplayLabel;
    private JLabel ageDisplayLabel;
    private JLabel sexDisplayLabel;
    private JLabel weightDisplayLabel;
    private JLabel heightDisplayLabel;
    private JLabel unitsDisplayLabel;
    private JLabel bmiDisplayLabel;
    private JTextArea healthSuggestionsArea;
    
    // Edit components - for updating profile
    private JTextField nameEditField;
    private JTextField ageEditField;
    private JTextField weightEditField;
    private JTextField heightEditField;
    private JComboBox<String> unitsEditCombo;
    
    // Control buttons
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton refreshButton;
    
    // Status and mode tracking
    private JLabel statusLabel;
    private boolean isEditing = false;
    
    /**
     * Constructor - Initializes the profile details panel
     * 
     * @param profileFacade Facade for profile management operations (Dependency Injection)
     * @param initialProfile Initial profile to display (can be null if loading from active)
     */
    public ProfileDetailsPanel(IProfileFacade profileFacade, ProfileDTO initialProfile) {
        this.profileFacade = profileFacade;
        this.currentProfile = initialProfile;
        
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        
        // Load profile if not provided
        if (currentProfile == null) {
            loadActiveProfile();
        } else {
            updateDisplayFromProfile();
        }
        
        setEditMode(false);
    }
    
    /**
     * Initialize all UI components with proper configuration
     * Follows Single Responsibility - each component has one clear purpose
     */
    private void initializeComponents() {
        // Header components
        statusLabel = new JLabel("Profile Details");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(new Color(51, 51, 51));
        
        // Display labels (read-only view)
        nameDisplayLabel = createDisplayLabel("--");
        ageDisplayLabel = createDisplayLabel("--");
        sexDisplayLabel = createDisplayLabel("--");
        weightDisplayLabel = createDisplayLabel("--");
        heightDisplayLabel = createDisplayLabel("--");
        unitsDisplayLabel = createDisplayLabel("--");
        bmiDisplayLabel = createDisplayLabel("--");
        
        // Edit fields (editing mode)
        nameEditField = new JTextField(20);
        nameEditField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        ageEditField = new JTextField(20);
        ageEditField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        weightEditField = new JTextField(20);
        weightEditField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        heightEditField = new JTextField(20);
        heightEditField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        unitsEditCombo = new JComboBox<>(new String[]{"Metric (kg/cm)", "Imperial (lbs/in)"});
        unitsEditCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Health suggestions area
        healthSuggestionsArea = new JTextArea(4, 30);
        healthSuggestionsArea.setFont(new Font("Arial", Font.PLAIN, 11));
        healthSuggestionsArea.setEditable(false);
        healthSuggestionsArea.setWrapStyleWord(true);
        healthSuggestionsArea.setLineWrap(true);
        healthSuggestionsArea.setBackground(this.getBackground());
        healthSuggestionsArea.setBorder(BorderFactory.createEtchedBorder());
        
        // Control buttons
        editButton = createButton("Edit Profile", new Color(33, 150, 243), Color.WHITE);
        saveButton = createButton("Save Changes", new Color(76, 175, 80), Color.WHITE);
        cancelButton = createButton("Cancel", new Color(158, 158, 158), Color.WHITE);
        refreshButton = createButton("Refresh", new Color(255, 152, 0), Color.WHITE);
    }
    
    /**
     * Create a styled display label
     * Follows DRY principle - reusable label creation
     */
    private JLabel createDisplayLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(51, 51, 51));
        return label;
    }
    
    /**
     * Create a styled button
     * Follows DRY principle - reusable button creation
     */
    private JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }
    
    /**
     * Layout components using appropriate layout managers
     * Follows Open/Closed - easy to modify layout without changing component logic
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(statusLabel, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        
        // Profile information panel
        JPanel infoPanel = createProfileInfoPanel();
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Health information panel
        JPanel healthPanel = createHealthInfoPanel();
        contentPanel.add(healthPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Add all panels
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create the profile information panel with both display and edit components
     * Follows Single Responsibility - handles only profile info layout
     */
    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Profile Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        
        // Name row
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(nameDisplayLabel, gbc);
        panel.add(nameEditField, gbc); // Same position, visibility controlled
        
        // Age row
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(ageDisplayLabel, gbc);
        panel.add(ageEditField, gbc); // Same position, visibility controlled
        
        // Sex row (read-only)
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Sex:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(sexDisplayLabel, gbc);
        
        // Weight row
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Weight:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(weightDisplayLabel, gbc);
        panel.add(weightEditField, gbc); // Same position, visibility controlled
        
        // Height row
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Height:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(heightDisplayLabel, gbc);
        panel.add(heightEditField, gbc); // Same position, visibility controlled
        
        // Units row
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Units:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(unitsDisplayLabel, gbc);
        panel.add(unitsEditCombo, gbc); // Same position, visibility controlled
        
        // BMI row (read-only)
        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("BMI:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(bmiDisplayLabel, gbc);
        
        return panel;
    }
    
    /**
     * Create the health information panel
     * Follows Single Responsibility - handles only health info layout
     */
    private JPanel createHealthInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Health Suggestions"));
        
        JScrollPane scrollPane = new JScrollPane(healthSuggestionsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Create the button panel
     * Follows Single Responsibility - handles only button layout
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(editButton);
        panel.add(saveButton);
        panel.add(cancelButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    /**
     * Attach event listeners to components
     * Follows Interface Segregation - each listener handles specific events
     */
    private void attachEventListeners() {
        editButton.addActionListener(e -> handleEdit());
        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> handleCancel());
        refreshButton.addActionListener(e -> handleRefresh());
        
        // Units change listener to update display format
        unitsEditCombo.addActionListener(e -> updateUnitsDisplay());
    }
    
    /**
     * Load the active profile from the facade
     * Follows Single Responsibility - handles only profile loading
     */
    private void loadActiveProfile() {
        try {
            currentProfile = profileFacade.getActiveProfile();
            if (currentProfile != null) {
                updateDisplayFromProfile();
                statusLabel.setText("Profile Details - " + currentProfile.getName());
            } else {
                statusLabel.setText("No Active Profile Found");
                showError("No active profile found. Please select a profile.");
            }
        } catch (Exception e) {
            showError("Error loading profile: " + e.getMessage());
        }
    }
    
    /**
     * Update all display components from the current profile
     * Follows Single Responsibility - handles only UI updates from profile data
     */
    private void updateDisplayFromProfile() {
        if (currentProfile == null) return;
        
        // Update display labels
        nameDisplayLabel.setText(currentProfile.getName());
        ageDisplayLabel.setText(String.valueOf(currentProfile.getAge()));
        sexDisplayLabel.setText(capitalizeFirst(currentProfile.getSex()));
        
        String weightUnit = currentProfile.getUnits().equals("metric") ? "kg" : "lbs";
        String heightUnit = currentProfile.getUnits().equals("metric") ? "cm" : "in";
        
        weightDisplayLabel.setText(String.format("%.1f %s", currentProfile.getWeight(), weightUnit));
        heightDisplayLabel.setText(String.format("%.1f %s", currentProfile.getHeight(), heightUnit));
        unitsDisplayLabel.setText(capitalizeFirst(currentProfile.getUnits()));
        
        // Update BMI and health suggestions
        updateHealthInfo();
        
        // Update edit fields
        nameEditField.setText(currentProfile.getName());
        ageEditField.setText(String.valueOf(currentProfile.getAge()));
        weightEditField.setText(String.format("%.1f", currentProfile.getWeight()));
        heightEditField.setText(String.format("%.1f", currentProfile.getHeight()));
        
        if (currentProfile.getUnits().equals("metric")) {
            unitsEditCombo.setSelectedIndex(0);
        } else {
            unitsEditCombo.setSelectedIndex(1);
        }
    }
    
    /**
     * Update health information (BMI and suggestions)
     * Follows Single Responsibility - handles only health info calculations
     */
    private void updateHealthInfo() {
        if (currentProfile == null) return;
        
        try {
            // Calculate and display BMI
            double bmi = profileFacade.calculateBMI(currentProfile.getId());
            bmiDisplayLabel.setText(String.format("%.1f", bmi));
            
            // Get and display health suggestions
            String suggestions = profileFacade.getHealthSuggestions(currentProfile.getId());
            healthSuggestionsArea.setText(suggestions);
            
        } catch (Exception e) {
            bmiDisplayLabel.setText("Error");
            healthSuggestionsArea.setText("Unable to calculate health information: " + e.getMessage());
        }
    }
    
    /**
     * Set edit mode on/off
     * Follows Single Responsibility - handles only edit mode UI changes
     */
    private void setEditMode(boolean editing) {
        this.isEditing = editing;
        
        // Toggle visibility of display vs edit components
        nameDisplayLabel.setVisible(!editing);
        nameEditField.setVisible(editing);
        
        ageDisplayLabel.setVisible(!editing);
        ageEditField.setVisible(editing);
        
        weightDisplayLabel.setVisible(!editing);
        weightEditField.setVisible(editing);
        
        heightDisplayLabel.setVisible(!editing);
        heightEditField.setVisible(editing);
        
        unitsDisplayLabel.setVisible(!editing);
        unitsEditCombo.setVisible(editing);
        
        // Toggle button visibility
        editButton.setVisible(!editing);
        saveButton.setVisible(editing);
        cancelButton.setVisible(editing);
        
        // Update status
        if (editing) {
            statusLabel.setText("Editing Profile - " + (currentProfile != null ? currentProfile.getName() : ""));
            statusLabel.setForeground(Color.ORANGE);
        } else {
            statusLabel.setText("Profile Details - " + (currentProfile != null ? currentProfile.getName() : ""));
            statusLabel.setForeground(new Color(51, 51, 51));
        }
        
        revalidate();
        repaint();
    }
    
    /**
     * Handle edit button action
     * Follows Single Responsibility - handles only edit mode activation
     */
    private void handleEdit() {
        if (currentProfile == null) {
            showError("No profile to edit. Please refresh or select a profile.");
            return;
        }
        setEditMode(true);
    }
    
    /**
     * Handle save button action with validation
     * Follows Single Responsibility - handles only save logic
     */
    private void handleSave() {
        try {
            // Validate inputs
            String name = validateName();
            int age = validateAge();
            double weight = validateWeight();
            double height = validateHeight();
            
            // Update profile
            currentProfile = profileFacade.updateProfile(
                currentProfile.getId(), 
                name, 
                age, 
                currentProfile.getSex(), // Sex cannot be changed
                weight, 
                height
            );
            
            // Update units if changed
            String selectedUnits = unitsEditCombo.getSelectedIndex() == 0 ? "metric" : "imperial";
            if (!selectedUnits.equals(currentProfile.getUnits())) {
                profileFacade.updateSettings(currentProfile.getId(), selectedUnits);
                currentProfile = profileFacade.getProfile(currentProfile.getId()); // Reload with new units
            }
            
            // Update display and exit edit mode
            updateDisplayFromProfile();
            setEditMode(false);
            
            showSuccess("Profile updated successfully!");
            
        } catch (Exception e) {
            showError("Error saving profile: " + e.getMessage());
        }
    }
    
    /**
     * Handle cancel button action
     * Follows Single Responsibility - handles only edit cancellation
     */
    private void handleCancel() {
        // Restore original values
        updateDisplayFromProfile();
        setEditMode(false);
    }
    
    /**
     * Handle refresh button action
     * Follows Single Responsibility - handles only profile refresh
     */
    private void handleRefresh() {
        loadActiveProfile();
        showSuccess("Profile refreshed!");
    }
    
    /**
     * Update units display when selection changes
     * Follows Single Responsibility - handles only units display updates
     */
    private void updateUnitsDisplay() {
        // This could be expanded to show conversion hints or update field labels
    }
    
    // Validation methods - each follows Single Responsibility principle
    
    private String validateName() throws IllegalArgumentException {
        String name = nameEditField.getText().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }
        return name;
    }
    
    private int validateAge() throws IllegalArgumentException {
        try {
            int age = Integer.parseInt(ageEditField.getText().trim());
            if (age < 1 || age > 120) {
                throw new IllegalArgumentException("Age must be between 1 and 120");
            }
            return age;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a valid number");
        }
    }
    
    private double validateWeight() throws IllegalArgumentException {
        try {
            double weight = Double.parseDouble(weightEditField.getText().trim());
            if (weight <= 0 || weight > 1000) {
                throw new IllegalArgumentException("Weight must be between 0 and 1000");
            }
            return weight;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Weight must be a valid number");
        }
    }
    
    private double validateHeight() throws IllegalArgumentException {
        try {
            double height = Double.parseDouble(heightEditField.getText().trim());
            if (height <= 0 || height > 300) {
                throw new IllegalArgumentException("Height must be between 0 and 300");
            }
            return height;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Height must be a valid number");
        }
    }
    
    // Utility methods
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update profile data (called externally when profile changes)
     * Follows Interface Segregation - provides only necessary profile update functionality
     */
    public void updateProfile(ProfileDTO newProfile) {
        this.currentProfile = newProfile;
        updateDisplayFromProfile();
        setEditMode(false);
    }
    
    /**
     * Get current profile
     * Follows Interface Segregation - provides only necessary data access
     */
    public ProfileDTO getCurrentProfile() {
        return currentProfile;
    }
} 