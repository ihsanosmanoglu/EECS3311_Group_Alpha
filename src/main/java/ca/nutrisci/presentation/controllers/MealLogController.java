package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.facades.IProfileFacade;
import ca.nutrisci.presentation.ui.meallog.MealLogPanel;

import javax.swing.*;

/**
 * MealLogController - Simplified controller for meal logging UI
 * Part of the Presentation Layer - MVC Pattern
 * 
 * PURPOSE:
 * - Simplified controller that works with component-based MealLogPanel
 * - Handles initialization and facade injection
 * - Most interaction logic is now handled by MealLogMediator (DD-3)
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-3: NavigationMediator over MVC Controllers - Complex interactions handled by MealLogMediator
 * - DD-1: Three-Tier Layered - Only depends on Application layer facades
 * - DD-2: Four Domain Façades - Injects facade dependencies
 * 
 * NOTE: In the new architecture, most UI interactions are coordinated by MealLogMediator.
 * This controller now focuses on initialization and facade injection only.
 * 
 * @author NutriSci Development Team
 * @version 2.0 (Simplified for component-based architecture)
 * @since 1.0
 */
public class MealLogController {
    
    private final MealLogPanel view;
    private IMealLogFacade mealLogFacade;
    private IProfileFacade profileFacade;
    
    /**
     * Constructor - Creates controller for the meal logging view
     * 
     * @param view The meal logging panel view
     */
    public MealLogController(MealLogPanel view) {
        this.view = view;
        // No callbacks to setup in new architecture - mediator handles interactions
    }
    
    /**
     * Set the meal logging facade
     * 
     * @param mealLogFacade Facade for meal operations (DD-2: Domain Façades)
     */
    public void setMealLogFacade(IMealLogFacade mealLogFacade) {
        this.mealLogFacade = mealLogFacade;
    }
    
    /**
     * Set the profile facade
     * 
     * @param profileFacade Facade for profile operations (DD-2: Domain Façades)
     */
    public void setProfileFacade(IProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }
    
    /**
     * Initialize the controller and set up initial state
     * 
     * SIMPLIFIED INITIALIZATION:
     * - Gets active profile and passes to view
     * - View and mediator handle all other initialization
     * - No complex callback setup needed
     */
    public void initialize() {
        // Set the current profile if available
        if (profileFacade != null) {
            try {
                ProfileDTO activeProfile = profileFacade.getActiveProfile();
                if (activeProfile != null) {
                    // Update view with active profile - view handles rest via mediator
                    view.setActiveProfile(activeProfile);
                    System.out.println("✅ MealLogController initialized with active profile: " + activeProfile.getName());
                } else {
                    System.out.println("⚠️ No active profile found for meal logging");
                    // View will show appropriate message via components
                }
            } catch (Exception e) {
                System.err.println("Error loading active profile in MealLogController: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Error loading active profile: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("⚠️ ProfileFacade not available in MealLogController");
        }
    }
    
    /**
     * Handle navigation requests from the view
     * Following DD-3: NavigationMediator pattern
     * 
     * @param destination Navigation destination
     */
    public void requestNavigation(String destination) {
        // This would be handled by the main NavigationMediator
        // For now, we'll just log the request
        System.out.println("Navigation to " + destination + " requested from MealLogPanel");
    }
    
    /**
     * Refresh the meal logging view
     * Useful for external triggers to update the interface
     */
    public void refreshView() {
        if (view != null) {
            view.refreshAllComponents();
            System.out.println("✅ MealLogController refreshed view components");
        }
    }
    
    /**
     * Update the active profile in the view
     * Called when profile changes from external sources
     * 
     * @param activeProfile New active profile
     */
    public void updateActiveProfile(ProfileDTO activeProfile) {
        if (view != null) {
            view.setActiveProfile(activeProfile);
            System.out.println("✅ MealLogController updated active profile: " + 
                             (activeProfile != null ? activeProfile.getName() : "null"));
        }
    }
    
    /**
     * Get the current view
     * Useful for navigation and external access
     * 
     * @return The meal logging panel view
     */
    public MealLogPanel getView() {
        return view;
    }
    
    /**
     * Check if the controller is properly initialized
     * 
     * @return true if facades are set and view is available
     */
    public boolean isInitialized() {
        return view != null && mealLogFacade != null && profileFacade != null;
    }
    
    /**
     * Get status information for debugging
     * 
     * @return Status string with initialization information
     */
    public String getStatus() {
        return String.format("MealLogController Status: view=%s, mealLogFacade=%s, profileFacade=%s",
                           view != null ? "✓" : "✗",
                           mealLogFacade != null ? "✓" : "✗", 
                           profileFacade != null ? "✓" : "✗");
    }
} 