package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ProfileDTO;

/**
 * IProfileFacade - Interface for profile management operations
 * Part of the Application Layer - Facade Pattern
 */
public interface IProfileFacade {
    
    /**
     * Creates a new profile
     * @param profileDTO the profile data
     * @return true if successful, false otherwise
     */
    boolean createProfile(ProfileDTO profileDTO);
    
    /**
     * Validates profile data
     * @param profileDTO the profile data to validate
     * @return true if valid, false otherwise
     */
    boolean validateProfile(ProfileDTO profileDTO);
} 