package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IProfileFacade;

import java.util.List;
import java.util.UUID;

/**
 * ProfileController - Handles user interactions for the profile screen.
 * Part of the Presentation Layer - Controller
 * This class connects the Profile UI to the business logic in the facade.
 */
public class ProfileController {

    private IProfileFacade profileFacade;

    public ProfileController(IProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }

    /**
     * Creates a new user profile.
     * @return The created ProfileDTO.
     */
    public ProfileDTO createProfile(String name, int age, String sex, double weight, double height, String units) {
        try {
            return profileFacade.createProfile(name, age, sex, weight, height, units);
        } catch (IllegalArgumentException e) {
            // In a real app, show this to the user in a dialog
            System.err.println("Error creating profile: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a list of all user profiles.
     * @return A list of all ProfileDTOs.
     */
    public List<ProfileDTO> getAllProfiles() {
        return profileFacade.getAllProfiles();
    }
    
    /**
     * Sets the currently active profile.
     */
    public void setActiveProfile(UUID profileId) {
        try {
            profileFacade.setActiveProfile(profileId);
        } catch (IllegalArgumentException e) {
            System.err.println("Error setting active profile: " + e.getMessage());
        }
    }

    /**
     * Gets the currently active profile.
     * @return The active ProfileDTO, or null if none is active.
     */
    public ProfileDTO getActiveProfile() {
        return profileFacade.getActiveProfile();
    }

    /**
     * Deletes a profile.
     */
    public void deleteProfile(UUID profileId) {
        try {
            profileFacade.deleteProfile(profileId);
        } catch (IllegalArgumentException e) {
            System.err.println("Error deleting profile: " + e.getMessage());
        }
    }

    /**
     * Updates an existing profile.
     * @return The updated ProfileDTO.
     */
    public ProfileDTO updateProfile(UUID profileId, String name, int age, String sex, double weight, double height) {
        try {
            return profileFacade.updateProfile(profileId, name, age, sex, weight, height);
        } catch (IllegalArgumentException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            return null;
        }
    }
} 