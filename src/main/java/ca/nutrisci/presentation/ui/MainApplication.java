package ca.nutrisci.presentation.ui;

import ca.nutrisci.application.facades.IProfileFacade;
import ca.nutrisci.application.facades.ProfileManagement;
import ca.nutrisci.application.services.ProfileService;
import ca.nutrisci.infrastructure.data.repositories.FileRepoFactory;
import ca.nutrisci.infrastructure.data.repositories.IRepositoryFactory;
import ca.nutrisci.infrastructure.data.repositories.ProfileRepo;
import ca.nutrisci.presentation.controllers.ProfileController;
import ca.nutrisci.presentation.mediator.NavigationMediator;
import javax.swing.*;
import java.awt.*;

/**
 * MainApplication - The main entry point for the NutriSci Swing GUI.
 * This class initializes the main application window, sets up the UI components,
 * and wires up the navigation mediator.
 */
public class MainApplication {

    private JFrame frame;
    private NavigationMediator mediator;

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        // Ensure UI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set a modern look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MainApplication().createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Creates and displays the main application GUI.
     */
    public void createAndShowGUI() {
        // 1. Create the main frame
        frame = new JFrame("NutriSci: SwEATch to Better!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null); // Center the window

        // 2. Create the main content pane with a border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.setContentPane(mainPanel);
        
        // 3. Create the tabbed pane for navigation
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 4. Initialize the Navigation Mediator
        mediator = new NavigationMediator(tabbedPane);

        // 5. Initialize Layers and Dependencies
        // This is where we wire everything together according to our clean architecture.
        // The UI (Presentation) depends on Controllers, which depend on Facades (Application).
        IRepositoryFactory repoFactory = new FileRepoFactory("data/app");
        
        // Profile Management Dependencies
        ProfileRepo profileRepo = repoFactory.getProfileRepository();
        ProfileService profileService = new ProfileService(profileRepo);
        IProfileFacade profileFacade = new ProfileManagement(profileService, profileRepo);
        ProfileController profileController = new ProfileController(profileFacade);
        
        // 6. Create and add the different UI panels (screens)
        ProfilePanel profilePanel = new ProfilePanel(profileController);
        
        JPanel mealLogPanel = new JPanel();
        mealLogPanel.add(new JLabel("Meal Logging Screen"));
        
        JPanel swapPanel = new JPanel();
        swapPanel.add(new JLabel("Food Swap Screen"));
        
        JPanel vizPanel = new JPanel();
        vizPanel.add(new JLabel("Data Visualization Screen"));

        // 7. Register panels with the mediator
        mediator.registerPanel("Profile Management", profilePanel);
        mediator.registerPanel("Meal Log", mealLogPanel);
        mediator.registerPanel("Swap Engine", swapPanel);
        mediator.registerPanel("Visualization", vizPanel);
        
        // 8. Add a simple header
        JLabel headerLabel = new JLabel("NutriSci Dashboard", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // 9. Add a simple footer
        JLabel footerLabel = new JLabel("© 2024 NutriSci Application", JLabel.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);
        
        // Display the window
        frame.setVisible(true);
    }
} 