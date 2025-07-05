package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ProfileDTO;

/**
 * ProfileManagement - Concrete implementation of profile facade
 * Part of the Application Layer - Facade Pattern
 */
public class ProfileManagement implements IProfileFacade {
    
    @Override
    public boolean createProfile(ProfileDTO profileDTO) {
        // Validate first
        if (!validateProfile(profileDTO)) {
            return false;
        }
        
        // TODO: Save to repository
        // For now, just return true if validation passes
        return true;
    }
    
    @Override
    public boolean validateProfile(ProfileDTO profileDTO) {
        if (profileDTO == null) {
            return false;
        }
        
        return profileDTO.isValid();
    }
} 