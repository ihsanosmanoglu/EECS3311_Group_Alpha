package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ProfileDTO;

import java.util.List;
import java.util.UUID;

/**
 * IProfileFacade - Interface for profile management operations
 * Part of the Application Layer - Facade Pattern
 */
public interface IProfileFacade {
    
    /**
     * Create a new profile
     */
    ProfileDTO createProfile(String name, int age, String sex, double weight, double height, String units);
    
    /**
     * Get profile by ID
     */
    ProfileDTO getProfile(UUID profileId);
    
    /**
     * Update profile information
     */
    ProfileDTO updateProfile(UUID profileId, String name, int age, String sex, double weight, double height);
    
    /**
     * Delete a profile
     */
    void deleteProfile(UUID profileId);
    
    /**
     * Get all profiles
     */
    List<ProfileDTO> getAllProfiles();
    
    /**
     * Get active profile
     */
    ProfileDTO getActiveProfile();
    
    /**
     * Set active profile
     */
    void setActiveProfile(UUID profileId);
    
    /**
     * Update profile settings
     */
    void updateSettings(UUID profileId, String units);
    
    /**
     * Calculate BMI for a profile
     */
    double calculateBMI(UUID profileId);
    
    /**
     * Get health suggestions for a profile
     */
    String getHealthSuggestions(UUID profileId);
    
    /**
     * Get profile statistics
     */
    String getProfileStats(UUID profileId);
    
    /**
     * Check if profile exists
     */
    boolean profileExists(UUID profileId);
    
    /**
     * Validate profile data
     */
    boolean validateProfile(ProfileDTO profileDTO);
} 