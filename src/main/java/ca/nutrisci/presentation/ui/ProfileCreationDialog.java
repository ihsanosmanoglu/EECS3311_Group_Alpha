package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IProfileFacade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ProfileCreationDialog - Dialog for creating new user profiles
 * 
 * PURPOSE:
 * - Provides a user-friendly interface for creating new profiles
 * - Validates user input before profile creation
 * - Integrates with the ProfileSelectionDialog workflow
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only profile creation UI and validation
 * - Open/Closed: Extensible for additional profile creation features
 * - Liskov Substitution: Implements standard JDialog behavior
 * - Interface Segregation: Uses specific IProfileFacade interface
 * - Dependency Inversion: Depends on IProfileFacade abstraction
 * 
 * DESIGN PATTERNS:
 * - Facade Pattern: Uses ProfileManagement facade for profile operations
 * - Template Method: Follows standard dialog initialization pattern
 * - Observer Pattern: Listens to user interactions via ActionListeners
 * 
 * USAGE:
 * - Called from ProfileSelectionDialog when user wants to create new profile
 * - Returns created ProfileDTO to calling code
 * - Handles input validation and error display
 */
public class ProfileCreationDialog extends JDialog {
    
    // Core dependencies - injected via constructor (Dependency Inversion)
    private final IProfileFacade profileFacade;
    
    // UI Components for form input
    private JTextField nameField;
    private JTextField ageField;
    private JTextField weightField;
    private JTextField heightField;
    private JComboBox<String> sexComboBox;
    private JComboBox<String> unitsComboBox;
    
    // Action buttons
    private JButton createButton;
    private JButton cancelButton;
    
    // Status display
    private JLabel statusLabel;
    
    // Application state
    private ProfileDTO createdProfile;
    private boolean profileWasCreated = false;
    
    /**
     * Constructor - Initializes the profile creation dialog
     * 
     * @param parent Parent dialog for modal positioning
     * @param profileFacade Facade for profile management operations (Dependency Injection)
     */
    public ProfileCreationDialog(Dialog parent, IProfileFacade profileFacade) {
        super(parent, "Create New Profile", true);
        this.profileFacade = profileFacade;
        
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        
        // Configure dialog properties with improved sizing
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(parent);
        setResizable(true);
        setMinimumSize(new Dimension(480, 450));
    }
    
    /**
     * Initialize all UI components with proper configuration
     * Follows Single Responsibility - each component has one clear purpose
     */
    private void initializeComponents() {
        // Input fields with improved sizing
        nameField = new JTextField(25);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setPreferredSize(new Dimension(200, 30));
        nameField.setMinimumSize(new Dimension(200, 30));
        nameField.setToolTipText("Enter your first and last name");
        
        ageField = new JTextField(25);
        ageField.setFont(new Font("Arial", Font.PLAIN, 14));
        ageField.setPreferredSize(new Dimension(200, 30));
        ageField.setMinimumSize(new Dimension(200, 30));
        ageField.setToolTipText("Enter your age in years (1-120)");
        
        weightField = new JTextField(25);
        weightField.setFont(new Font("Arial", Font.PLAIN, 14));
        weightField.setPreferredSize(new Dimension(200, 30));
        weightField.setMinimumSize(new Dimension(200, 30));
        weightField.setToolTipText("Enter your weight (kg if metric, lbs if imperial)");
        
        heightField = new JTextField(25);
        heightField.setFont(new Font("Arial", Font.PLAIN, 14));
        heightField.setPreferredSize(new Dimension(200, 30));
        heightField.setMinimumSize(new Dimension(200, 30));
        heightField.setToolTipText("Enter your height (cm if metric, inches if imperial)");
        
        // Dropdown selections with improved sizing
        sexComboBox = new JComboBox<>(new String[]{"Male", "Female"});
        sexComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        sexComboBox.setPreferredSize(new Dimension(200, 30));
        sexComboBox.setMinimumSize(new Dimension(200, 30));
        sexComboBox.setToolTipText("Select your biological sex");
        
        unitsComboBox = new JComboBox<>(new String[]{"Metric (kg/cm)", "Imperial (lbs/in)"});
        unitsComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        unitsComboBox.setPreferredSize(new Dimension(200, 30));
        unitsComboBox.setMinimumSize(new Dimension(200, 30));
        unitsComboBox.setToolTipText("Choose your preferred measurement units");
        
        // Action buttons with improved styling
        createButton = new JButton("Create Profile");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setPreferredSize(new Dimension(140, 35));
        createButton.setBackground(new Color(76, 175, 80));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setBorderPainted(false);
        createButton.setOpaque(true);
        createButton.setContentAreaFilled(true);
        createButton.setToolTipText("Create your profile with the entered information");
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(140, 35));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setContentAreaFilled(true);
        cancelButton.setToolTipText("Cancel profile creation and return to previous screen");
        
        // Status label with improved font
        statusLabel = new JLabel("Fill in all fields to create your profile");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
    }
    
    /**
     * Layout components using appropriate layout managers
     * Follows Open/Closed - easy to modify layout without changing component logic
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        JLabel titleLabel = new JLabel("Create New Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel);
        
        // Form panel with improved layout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);
        
        // Age field
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(ageLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(ageField, gbc);
        
        // Sex field
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel sexLabel = new JLabel("Sex:");
        sexLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(sexLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(sexComboBox, gbc);
        
        // Weight field
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel weightLabel = new JLabel("Weight:");
        weightLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(weightLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(weightField, gbc);
        
        // Height field
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel heightLabel = new JLabel("Height:");
        heightLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(heightLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(heightField, gbc);
        
        // Units field
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel unitsLabel = new JLabel("Units:");
        unitsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(unitsLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(unitsComboBox, gbc);
        
        // Button panel with improved spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        // Status panel with improved padding
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        statusPanel.add(statusLabel);
        
        // Combined bottom panel to fix layout conflict
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Add all panels to main dialog
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Attach event listeners to components
     * Follows Interface Segregation - each listener handles specific events
     */
    private void attachEventListeners() {
        createButton.addActionListener(e -> handleCreateProfile());
        cancelButton.addActionListener(e -> handleCancel());
        
        // Enter key creates profile
        nameField.addActionListener(e -> handleCreateProfile());
        ageField.addActionListener(e -> handleCreateProfile());
        weightField.addActionListener(e -> handleCreateProfile());
        heightField.addActionListener(e -> handleCreateProfile());
        
        // Update units label when selection changes
        unitsComboBox.addActionListener(e -> updateUnitsDisplay());
    }
    
    /**
     * Handle profile creation with validation
     * Follows Single Responsibility - handles only profile creation logic
     */
    private void handleCreateProfile() {
        try {
            // Validate and extract form data
            String name = validateName();
            int age = validateAge();
            String sex = validateSex();
            double weight = validateWeight();
            double height = validateHeight();
            String units = validateUnits();
            
            // Create profile using facade
            createdProfile = profileFacade.createProfile(name, age, sex, weight, height, units);
            profileWasCreated = true;
            
            // Show success message
            statusLabel.setText("Profile created successfully!");
            statusLabel.setForeground(Color.GREEN);
            
            // Close dialog after short delay
            Timer timer = new Timer(1000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
            
        } catch (Exception e) {
            showError("Error creating profile: " + e.getMessage());
        }
    }
    
    /**
     * Validate name field
     * Follows Single Responsibility - handles only name validation
     */
    private String validateName() throws IllegalArgumentException {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name must be less than 50 characters");
        }
        return name;
    }
    
    /**
     * Validate age field
     * Follows Single Responsibility - handles only age validation
     */
    private int validateAge() throws IllegalArgumentException {
        String ageText = ageField.getText().trim();
        if (ageText.isEmpty()) {
            throw new IllegalArgumentException("Age cannot be empty");
        }
        
        try {
            int age = Integer.parseInt(ageText);
            if (age < 1 || age > 120) {
                throw new IllegalArgumentException("Age must be between 1 and 120");
            }
            return age;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a valid number");
        }
    }
    
    /**
     * Validate sex selection
     * Follows Single Responsibility - handles only sex validation
     */
    private String validateSex() throws IllegalArgumentException {
        String sex = (String) sexComboBox.getSelectedItem();
        if (sex == null || sex.isEmpty()) {
            throw new IllegalArgumentException("Please select a sex");
        }
        return sex.toLowerCase();
    }
    
    /**
     * Validate weight field
     * Follows Single Responsibility - handles only weight validation
     */
    private double validateWeight() throws IllegalArgumentException {
        String weightText = weightField.getText().trim();
        if (weightText.isEmpty()) {
            throw new IllegalArgumentException("Weight cannot be empty");
        }
        
        try {
            double weight = Double.parseDouble(weightText);
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be greater than 0");
            }
            if (weight > 1000) {
                throw new IllegalArgumentException("Weight must be less than 1000");
            }
            return weight;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Weight must be a valid number");
        }
    }
    
    /**
     * Validate height field
     * Follows Single Responsibility - handles only height validation
     */
    private double validateHeight() throws IllegalArgumentException {
        String heightText = heightField.getText().trim();
        if (heightText.isEmpty()) {
            throw new IllegalArgumentException("Height cannot be empty");
        }
        
        try {
            double height = Double.parseDouble(heightText);
            if (height <= 0) {
                throw new IllegalArgumentException("Height must be greater than 0");
            }
            if (height > 300) {
                throw new IllegalArgumentException("Height must be less than 300");
            }
            return height;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Height must be a valid number");
        }
    }
    
    /**
     * Validate units selection
     * Follows Single Responsibility - handles only units validation
     */
    private String validateUnits() throws IllegalArgumentException {
        String unitsSelection = (String) unitsComboBox.getSelectedItem();
        if (unitsSelection == null || unitsSelection.isEmpty()) {
            throw new IllegalArgumentException("Please select units");
        }
        
        return unitsSelection.toLowerCase().contains("metric") ? "metric" : "imperial";
    }
    
    /**
     * Update units display when selection changes
     * Follows Single Responsibility - handles only UI updates
     */
    private void updateUnitsDisplay() {
        String selectedUnits = (String) unitsComboBox.getSelectedItem();
        if (selectedUnits != null) {
            if (selectedUnits.contains("Metric")) {
                statusLabel.setText("Weight in kg, Height in cm");
            } else {
                statusLabel.setText("Weight in lbs, Height in inches");
            }
            statusLabel.setForeground(Color.BLUE);
        }
    }
    
    /**
     * Handle dialog cancellation
     * Follows Single Responsibility - handles only cancellation logic
     */
    private void handleCancel() {
        dispose();
    }
    
    /**
     * Show error message to user
     * Follows Single Responsibility - handles only error display
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Please correct the error and try again.");
        statusLabel.setForeground(Color.RED);
    }
    
    /**
     * Get the created profile (called by parent after dialog closes)
     * Follows Interface Segregation - provides only necessary data access
     */
    public ProfileDTO getCreatedProfile() {
        return createdProfile;
    }
    
    /**
     * Check if a profile was successfully created
     * Follows Interface Segregation - provides only necessary status information
     */
    public boolean wasProfileCreated() {
        return profileWasCreated;
    }
} 