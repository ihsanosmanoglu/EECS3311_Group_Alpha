package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.domain.entities.Profile;
import java.util.List;
import java.util.UUID;

/**
 * ProfileRepo - Repository interface for profile data access
 * Part of the Infrastructure Layer
 */
public interface ProfileRepo {
    
    // Basic CRUD operations
    Profile save(Profile profile);
    Profile findById(UUID profileId);
    void delete(UUID profileId);
    List<Profile> findAll();
    Profile update(Profile profile);
    
    // Legacy method names for backward compatibility
    void saveProfile(Profile profile);
    void deleteProfile(UUID profileId);
    List<Profile> listAllProfiles();
    
    // Profile-specific queries
    Profile findActiveProfile();
    List<Profile> findByName(String name);
    boolean existsById(UUID profileId);
    
    // Profile settings
    void updateProfileSettings(UUID profileId, String units);
    void activateProfile(UUID profileId);
    void deactivateAllProfiles();
} 