package ca.nutrisci.application.services.observers;

import ca.nutrisci.application.dto.ProfileDTO;

/**
 * ProfileChangeListener - Observer interface for profile activation events
 * Part of the Application Layer - Observer Pattern (DD-8)
 * Follows design decision for decoupling UI refresh from profile operations
 */
public interface ProfileChangeListener {
    
    /**
     * Called when a profile is activated (becomes the active profile)
     * @param activeProfile The newly activated profile
     */
    void onProfileActivated(ProfileDTO activeProfile);
    
    /**
     * Called when a profile is deactivated
     * @param deactivatedProfile The profile that was deactivated
     */
    void onProfileDeactivated(ProfileDTO deactivatedProfile);
    
    /**
     * Called when a profile is created
     * @param newProfile The newly created profile
     */
    void onProfileCreated(ProfileDTO newProfile);
    
    /**
     * Called when a profile is updated
     * @param updatedProfile The updated profile
     */
    void onProfileUpdated(ProfileDTO updatedProfile);
    
    /**
     * Called when a profile is deleted
     * @param deletedProfileId The ID of the deleted profile
     */
    void onProfileDeleted(String deletedProfileId);
} 