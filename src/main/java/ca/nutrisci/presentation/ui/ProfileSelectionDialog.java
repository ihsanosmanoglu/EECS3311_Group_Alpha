package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IProfileFacade;
import ca.nutrisci.application.facades.ProfileManagement;
import ca.nutrisci.application.services.ProfileService;
import ca.nutrisci.infrastructure.data.repositories.IRepositoryFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.UUID;

/**
 * ProfileSelectionDialog - Initial screen for profile selection and creation
 * 
 * PURPOSE:
 * - Serves as the app's entry point for user profile management
 * - Allows users to select existing profiles or create new ones
 * - Ensures a profile is selected before accessing the main application
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only profile selection/creation UI logic
 * - Open/Closed: Extensible for additional profile selection features
 * - Liskov Substitution: Implements standard JDialog behavior
 * - Interface Segregation: Uses specific IProfileFacade interface
 * - Dependency Inversion: Depends on IProfileFacade abstraction, not concrete implementation
 * 
 * DESIGN PATTERNS:
 * - Facade Pattern: Uses ProfileManagement facade for profile operations
 * - Observer Pattern: Listens to user interactions via ActionListeners
 * - Template Method: Follows standard dialog initialization pattern
 * 
 * USAGE:
 * - Called at application startup before main UI
 * - Returns selected ProfileDTO to calling code
 * - Handles profile creation, selection, and validation
 */
public class ProfileSelectionDialog extends JDialog {
    
    // Core dependencies - injected via constructor (Dependency Inversion)
    private final IProfileFacade profileFacade;
    
    // UI Components - organized by responsibility
    private DefaultListModel<String> profileListModel;
    private JList<String> profileList;
    private JButton selectButton;
    private JButton createButton;
    private JButton exitButton;
    private JLabel instructionLabel;
    private JLabel statusLabel;
    
    // Application state
    private ProfileDTO selectedProfile;
    private List<ProfileDTO> availableProfiles;
    private boolean profileWasSelected = false;
    
    /**
     * Constructor - Initializes the profile selection dialog
     * 
     * @param parent Parent frame for modal dialog positioning
     * @param profileFacade Facade for profile management operations (Dependency Injection)
     */
    public ProfileSelectionDialog(Frame parent, IProfileFacade profileFacade) {
        super(parent, "NutriSci - Select Profile", true);
        this.profileFacade = profileFacade;
        
        initializeComponents();
        layoutComponents();
        attachEventListeners();
        loadAvailableProfiles();
        
        // Configure dialog properties
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    /**
     * Initialize all UI components with proper configuration
     * Follows Single Responsibility - each component has one clear purpose
     */
    private void initializeComponents() {
        // Header components
        instructionLabel = new JLabel("Welcome to NutriSci! Please select or create a profile to continue:");
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        statusLabel = new JLabel("Select a profile to get started");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE);
        
        // Profile list components
        profileListModel = new DefaultListModel<>();
        profileList = new JList<>(profileListModel);
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setFont(new Font("Arial", Font.PLAIN, 12));
        profileList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Action buttons with clear purposes
        selectButton = new JButton("Select Profile");
        selectButton.setFont(new Font("Arial", Font.BOLD, 12));
        selectButton.setBackground(new Color(76, 175, 80));
        selectButton.setForeground(Color.WHITE);
        selectButton.setFocusPainted(false);
        selectButton.setOpaque(true);
        selectButton.setBorderPainted(false);
        selectButton.setEnabled(false); // Initially disabled until selection made
        
        createButton = new JButton("Create New Profile");
        createButton.setFont(new Font("Arial", Font.BOLD, 12));
        createButton.setBackground(new Color(33, 150, 243));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setOpaque(true);
        createButton.setBorderPainted(false);
        
        exitButton = new JButton("Exit Application");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 12));
        exitButton.setBackground(new Color(244, 67, 54));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setOpaque(true);
        exitButton.setBorderPainted(false);
    }
    
    /**
     * Layout components using appropriate layout managers
     * Follows Open/Closed - easy to modify layout without changing component logic
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        headerPanel.add(instructionLabel, BorderLayout.CENTER);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Profile list with scroll pane
        JScrollPane scrollPane = new JScrollPane(profileList);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Profiles"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(selectButton);
        buttonPanel.add(createButton);
        buttonPanel.add(exitButton);
        
        // Footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        footerPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Add all panels to main dialog
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Attach event listeners to components
     * Follows Interface Segregation - each listener handles specific events
     */
    private void attachEventListeners() {
        // Profile list selection listener
        profileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleProfileSelection();
            }
        });
        
        // Double-click to select profile
        profileList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    handleSelectProfile();
                }
            }
        });
        
        // Button action listeners
        selectButton.addActionListener(e -> handleSelectProfile());
        createButton.addActionListener(e -> handleCreateProfile());
        exitButton.addActionListener(e -> handleExit());
        
        // Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleExit();
            }
        });
    }
    
    /**
     * Load available profiles from the data store
     * Follows Single Responsibility - handles only profile loading logic
     */
    private void loadAvailableProfiles() {
        try {
            availableProfiles = profileFacade.getAllProfiles();
            updateProfileList();
            
            if (availableProfiles.isEmpty()) {
                statusLabel.setText("No profiles found. Please create a new profile to get started.");
                statusLabel.setForeground(Color.ORANGE);
            } else {
                statusLabel.setText("Found " + availableProfiles.size() + " profile(s). Select one to continue.");
                statusLabel.setForeground(Color.BLUE);
            }
        } catch (Exception e) {
            showError("Error loading profiles: " + e.getMessage());
        }
    }
    
    /**
     * Update the profile list display
     * Follows Single Responsibility - handles only UI list updates
     */
    private void updateProfileList() {
        profileListModel.clear();
        
        for (ProfileDTO profile : availableProfiles) {
            String displayText = String.format("%s (Age: %d, %s, %.1f%s, %.1f%s)",
                profile.getName(),
                profile.getAge(),
                profile.getSex(),
                profile.getWeight(),
                profile.getUnits().equals("metric") ? "kg" : "lbs",
                profile.getHeight(),
                profile.getUnits().equals("metric") ? "cm" : "in"
            );
            profileListModel.addElement(displayText);
        }
    }
    
    /**
     * Handle profile selection from the list
     * Follows Single Responsibility - handles only selection logic
     */
    private void handleProfileSelection() {
        int selectedIndex = profileList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < availableProfiles.size()) {
            selectedProfile = availableProfiles.get(selectedIndex);
            selectButton.setEnabled(true);
            statusLabel.setText("Profile selected: " + selectedProfile.getName());
            statusLabel.setForeground(Color.GREEN);
        } else {
            selectedProfile = null;
            selectButton.setEnabled(false);
            statusLabel.setText("Please select a profile to continue.");
            statusLabel.setForeground(Color.BLUE);
        }
    }
    
    /**
     * Handle profile selection confirmation
     * Follows Single Responsibility - handles only selection confirmation
     */
    private void handleSelectProfile() {
        if (selectedProfile == null) {
            showError("Please select a profile first.");
            return;
        }
        
        try {
            // Set as active profile
            profileFacade.setActiveProfile(selectedProfile.getId());
            profileWasSelected = true;
            
            // Close dialog
            dispose();
        } catch (Exception e) {
            showError("Error selecting profile: " + e.getMessage());
        }
    }
    
    /**
     * Handle new profile creation
     * Follows Single Responsibility - handles only profile creation UI
     */
    private void handleCreateProfile() {
        ProfileCreationDialog creationDialog = new ProfileCreationDialog(this, profileFacade);
        creationDialog.setVisible(true);
        
        // Check if profile was created successfully
        ProfileDTO newProfile = creationDialog.getCreatedProfile();
        if (newProfile != null) {
            // Refresh the profile list
            loadAvailableProfiles();
            
            // Auto-select the newly created profile
            for (int i = 0; i < availableProfiles.size(); i++) {
                if (availableProfiles.get(i).getId().equals(newProfile.getId())) {
                    profileList.setSelectedIndex(i);
                    break;
                }
            }
            
            statusLabel.setText("Profile created successfully! Click 'Select Profile' to continue.");
            statusLabel.setForeground(Color.GREEN);
        }
    }
    
    /**
     * Handle application exit
     * Follows Single Responsibility - handles only exit logic
     */
    private void handleExit() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit NutriSci?",
            "Exit Application",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    /**
     * Show error message to user
     * Follows Single Responsibility - handles only error display
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error occurred. Please try again.");
        statusLabel.setForeground(Color.RED);
    }
    
    /**
     * Get the selected profile (called by parent after dialog closes)
     * Follows Interface Segregation - provides only necessary data access
     */
    public ProfileDTO getSelectedProfile() {
        return selectedProfile;
    }
    
    /**
     * Check if a profile was successfully selected
     * Follows Interface Segregation - provides only necessary status information
     */
    public boolean wasProfileSelected() {
        return profileWasSelected;
    }
} 