package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IProfileFacade;
import ca.nutrisci.application.facades.ProfileManagement;
import ca.nutrisci.application.services.ProfileService;
import ca.nutrisci.infrastructure.data.repositories.FileRepoFactory;
import ca.nutrisci.infrastructure.data.repositories.IRepositoryFactory;
import ca.nutrisci.infrastructure.data.repositories.JdbcRepoFactory;
import ca.nutrisci.infrastructure.data.repositories.ProfileRepo;
import ca.nutrisci.presentation.controllers.ProfileController;
import ca.nutrisci.presentation.mediator.NavigationMediator;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.facades.MealLogging;
import ca.nutrisci.application.services.MealLogService;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;
import ca.nutrisci.infrastructure.external.adapters.ExternalAdapter;
import ca.nutrisci.presentation.controllers.MealLogController;
import ca.nutrisci.presentation.ui.meallog.MealLogPanel;
import ca.nutrisci.presentation.ui.visualization.VisualizationPanel;
import javax.swing.*;
import java.awt.*;
import ca.nutrisci.application.facades.IVisualizationFacade;
import ca.nutrisci.application.facades.Visualization;
import ca.nutrisci.presentation.controllers.VisualizationController;
import ca.nutrisci.application.services.ChartsService;

/**
 * MainApplication - The main entry point for the NutriSci Swing GUI
 * 
 * PURPOSE:
 * - Manages the application startup flow with profile selection
 * - Initializes all dependencies following clean architecture principles
 * - Provides logout functionality to return to profile selection
 * - Coordinates between presentation, application, and infrastructure layers
 * 
 * SOLID PRINCIPLES APPLIED:
 * - Single Responsibility: Handles only application initialization and main UI flow
 * - Open/Closed: Extensible for additional panels and features
 * - Liskov Substitution: Uses standard Swing components that can be substituted
 * - Interface Segregation: Uses specific facade interfaces for each domain
 * - Dependency Inversion: Depends on abstractions (IRepositoryFactory, facades) not concrete implementations
 * 
 * DESIGN PATTERNS:
 * - Facade Pattern: Uses application layer facades for business logic access
 * - Mediator Pattern: Uses NavigationMediator for panel coordination
 * - Factory Pattern: Uses IRepositoryFactory for data access layer creation
 * - Observer Pattern: ProfileService notifies components of profile changes
 * 
 * APPLICATION FLOW:
 * 1. Show ProfileSelectionDialog for initial profile choice
 * 2. If profile selected, create main application with selected profile
 * 3. Main app shows ProfileDetailsPanel instead of profile management
 * 4. Logout button returns to ProfileSelectionDialog
 */
public class MainApplication {

    // Main application components
    private JFrame frame;
    private NavigationMediator mediator;
    
    // Core dependencies (Dependency Inversion)
    private IRepositoryFactory repoFactory;
    private IProfileFacade profileFacade;
    private IMealLogFacade mealLogFacade;
    
    // Current application state
    private ProfileDTO currentProfile;
    
    /**
     * Main method to launch the application
     * Follows Single Responsibility - handles only application startup
     */
    public static void main(String[] args) {
        // Ensure UI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set a modern look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MainApplication().startApplication();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Start the application with profile selection flow
     * Follows Template Method pattern - defines the application startup sequence
     */
    public void startApplication() {
        // Initialize core dependencies first
        initializeDependencies();
        
        // Show profile selection and proceed only if profile is selected
        if (showProfileSelection()) {
            createAndShowMainGUI();
        } else {
            // User cancelled or no profile selected - exit application
            System.exit(0);
        }
    }
    
    /**
     * Initialize all core dependencies following Dependency Inversion principle
     * Follows Single Responsibility - handles only dependency setup
     */
    private void initializeDependencies() {
        // Repository factory for data access layer
        repoFactory = new JdbcRepoFactory();
        
        // Profile management dependencies
        ProfileRepo profileRepo = repoFactory.getProfileRepository();
        ProfileService profileService = new ProfileService(profileRepo);
        profileFacade = new ProfileManagement(profileService, profileRepo);
        
        // Meal logging dependencies
        INutritionGateway nutritionGateway = ExternalAdapter.getInstance("Canada Nutrient File-20250622");
        mealLogFacade = new MealLogging(repoFactory, nutritionGateway);
    }
    
    /**
     * Show profile selection dialog and handle profile selection
     * Follows Single Responsibility - handles only profile selection flow
     * 
     * @return true if profile was selected, false if cancelled
     */
    private boolean showProfileSelection() {
        // Create and show profile selection dialog
        ProfileSelectionDialog selectionDialog = new ProfileSelectionDialog(null, profileFacade);
        selectionDialog.setVisible(true);
        
        // Check if profile was selected
        if (selectionDialog.wasProfileSelected()) {
            currentProfile = selectionDialog.getSelectedProfile();
            return currentProfile != null;
        }
        
        return false;
    }
    
    /**
     * Creates and displays the main application GUI
     * Follows Template Method pattern - defines the main GUI creation sequence
     */
    public void createAndShowMainGUI() {
        // 1. Create the main frame
        frame = new JFrame("NutriSci: SwEATch to Better! - " + currentProfile.getName());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null); // Center the window
        
        // Add window closing handler to ask for confirmation
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleLogout();
            }
        });

        // 2. Create the main content pane with a border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.setContentPane(mainPanel);
        
        // 3. Create the header with logout functionality
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 4. Create the tabbed pane for navigation
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 5. Initialize the Navigation Mediator
        mediator = new NavigationMediator(tabbedPane);

        // 6. Create and add the different UI panels (screens)
        createApplicationPanels();
        
        // 7. Add a simple footer
        JLabel footerLabel = new JLabel("© 2024 NutriSci Application - User: " + currentProfile.getName(), JLabel.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);
        
        // 8. Display the window
        frame.setVisible(true);
    }
    
    /**
     * Create the header panel with app title and logout button
     * Follows Single Responsibility - handles only header creation
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // App title
        JLabel titleLabel = new JLabel("NutriSci Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Logout button (top left)
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(new Color(244, 67, 54));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.addActionListener(e -> handleLogout());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(logoutButton);
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    /**
     * Create all application panels and register them with the mediator
     * Follows Single Responsibility - handles only panel creation and registration
     */
    private void createApplicationPanels() {
        // Profile Details Panel (replaces old Profile Management)
        ProfileDetailsPanel profileDetailsPanel = new ProfileDetailsPanel(profileFacade, currentProfile);
        
        // Meal Log Panel
        MealLogPanel mealLogPanel = new MealLogPanel(mealLogFacade, currentProfile);
        MealLogController mealLogController = new MealLogController(mealLogPanel);
        
        // Register MealLogPanel as a ProfileChangeListener (Observer pattern)
        if (repoFactory instanceof JdbcRepoFactory) {
            ProfileRepo profileRepo = repoFactory.getProfileRepository();
            ProfileService profileService = new ProfileService(profileRepo);
            profileService.addProfileChangeListener(mealLogPanel);
        }
        
        // Set facades on the meal log controller
        mealLogController.setMealLogFacade(mealLogFacade);
        mealLogController.setProfileFacade(profileFacade);
        
        // Initialize the controller
        mealLogController.initialize();
        
        // Placeholder panel for future feature
        JPanel swapPanel = createPlaceholderPanel("Food Swap Engine", 
            "Smart food swapping recommendations will be available here.");
        
        // Visualization panel
        VisualizationPanel vizPanel = new VisualizationPanel();
        IVisualizationFacade visualizationFacade = new Visualization(new ChartsService(repoFactory.getMealLogRepository(), repoFactory.getSwapHistoryRepository()));
        VisualizationController vizController = new VisualizationController(visualizationFacade, vizPanel, mealLogFacade);
        vizController.setCurrentProfile(currentProfile.getId());
        vizPanel.setController(vizController);
        System.out.println("[DEBUG] VisualizationPanel controller set: " + (vizPanel != null && vizController != null));

        // Register panels with the mediator
        mediator.registerPanel("Profile", profileDetailsPanel);
        mediator.registerPanel("Meal Log", mealLogPanel);
        mediator.registerPanel("Swap Engine", swapPanel);
        mediator.registerPanel("Visualization", vizPanel);
    }
    
    /**
     * Create a placeholder panel for future features
     * Follows DRY principle - reusable placeholder creation
     */
    private JPanel createPlaceholderPanel(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>", JLabel.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(Color.GRAY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Handle logout functionality - return to profile selection
     * Follows Single Responsibility - handles only logout flow
     */
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to logout and return to profile selection?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Close current window
            frame.dispose();
            
            // Clear current profile
            currentProfile = null;
            
            // Restart the application with profile selection
            SwingUtilities.invokeLater(() -> {
                if (showProfileSelection()) {
                    createAndShowMainGUI();
                } else {
                    System.exit(0);
                }
            });
        }
    }
    
    /**
     * Get the current profile (for testing or external access)
     * Follows Interface Segregation - provides only necessary data access
     */
    public ProfileDTO getCurrentProfile() {
        return currentProfile;
    }
    
    /**
     * Get the main frame (for testing or external access)
     * Follows Interface Segregation - provides only necessary component access
     */
    public JFrame getMainFrame() {
        return frame;
    }
} 