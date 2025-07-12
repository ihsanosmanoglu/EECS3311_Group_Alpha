package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.ISwapFacade;
import ca.nutrisci.application.facades.IProfileFacade;
import ca.nutrisci.presentation.ui.SwapPanel;

import javax.swing.*;

/**
 * SwapController - Controller for food swap UI
 * Part of the Presentation Layer - MVC Pattern
 * 
 * PURPOSE:
 * - Controller that works with SwapPanel
 * - Handles initialization and facade injection
 * - Coordinates swap operations between UI and application layer
 * 
 * DESIGN DECISIONS FOLLOWED:
 * - DD-1: Three-Tier Layered - Only depends on Application layer facades
 * - DD-2: Four Domain Façades - Injects facade dependencies
 * - Follows same pattern as MealLogController
 * 
 * @author NutriSci Development Team
 * @version 1.0
 */
public class SwapController {
    
    private final SwapPanel view;
    private ISwapFacade swapFacade;
    private IProfileFacade profileFacade;
    
    /**
     * Constructor - Creates controller for the swap view
     * 
     * @param view The swap panel view
     */
    public SwapController(SwapPanel view) {
        this.view = view;
    }
    
    /**
     * Set the swap facade
     * 
     * @param swapFacade Facade for swap operations (DD-2: Domain Façades)
     */
    public void setSwapFacade(ISwapFacade swapFacade) {
        this.swapFacade = swapFacade;
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
     */
    public void initialize() {
        // Set the current profile if available
        if (profileFacade != null) {
            try {
                ProfileDTO activeProfile = profileFacade.getActiveProfile();
                if (activeProfile != null) {
                    view.setActiveProfile(activeProfile);
                    System.out.println("✅ SwapController initialized with active profile: " + activeProfile.getName());
                } else {
                    System.out.println("⚠️ No active profile found for swap functionality");
                }
            } catch (Exception e) {
                System.err.println("Error loading active profile in SwapController: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Error loading active profile: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("⚠️ ProfileFacade not available in SwapController");
        }
    }
    
    /**
     * Handle navigation requests from the view
     * 
     * @param destination Navigation destination
     */
    public void requestNavigation(String destination) {
        System.out.println("Navigation to " + destination + " requested from SwapPanel");
    }
    
    /**
     * Refresh the swap view
     */
    public void refreshView() {
        if (view != null) {
            view.refreshPanel();
            System.out.println("✅ SwapController refreshed view");
        }
    }
    
    /**
     * Update the active profile in the view
     * 
     * @param activeProfile New active profile
     */
    public void updateActiveProfile(ProfileDTO activeProfile) {
        if (view != null) {
            view.setActiveProfile(activeProfile);
            System.out.println("✅ SwapController updated active profile: " + 
                             (activeProfile != null ? activeProfile.getName() : "null"));
        }
    }
    
    /**
     * Get the current view
     * 
     * @return The swap panel view
     */
    public SwapPanel getView() {
        return view;
    }
    
    /**
     * Check if the controller is properly initialized
     * 
     * @return true if facades are set and view is available
     */
    public boolean isInitialized() {
        return view != null && swapFacade != null && profileFacade != null;
    }
    
    /**
     * Get status information for debugging
     * 
     * @return Status string with initialization information
     */
    public String getStatus() {
        return String.format("SwapController Status: view=%s, swapFacade=%s, profileFacade=%s",
                           view != null ? "✓" : "✗",
                           swapFacade != null ? "✓" : "✗", 
                           profileFacade != null ? "✓" : "✗");
    }
} 