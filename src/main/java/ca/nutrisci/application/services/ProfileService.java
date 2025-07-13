package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.services.observers.ProfileChangeListener;
import ca.nutrisci.domain.entities.Profile;
import ca.nutrisci.infrastructure.data.repositories.ProfileRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ProfileService - Business logic for profile operations
 * Part of the Application Layer
 */
public class ProfileService {
    
    private ProfileRepo profileRepo;
    private List<ProfileChangeListener> listeners;
    
    public ProfileService(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
        this.listeners = new ArrayList<>();
    }
    
    /**
     * Validate profile data
     */
    public boolean validate(ProfileDTO profileDTO) {
        if (profileDTO == null) return false;
        if (profileDTO.getName() == null || profileDTO.getName().trim().isEmpty()) return false;
        if (profileDTO.getAge() < 0 || profileDTO.getAge() > 150) return false;
        if (profileDTO.getWeight() <= 0) return false;
        if (profileDTO.getHeight() <= 0) return false;
        if (profileDTO.getSex() == null) return false;
        
        return true;
    }
    
    /**
     * Calculate BMI for profile
     */
    public double calculateBMI(ProfileDTO profileDTO) {
        if (!validate(profileDTO)) return 0.0;
        
        double weight = profileDTO.getWeight();
        double height = profileDTO.getHeight();
        String units = profileDTO.getUnits();
        
        if ("imperial".equals(units)) {
            // Weight in pounds, height in inches
            double weightKg = weight * 0.453592;
            double heightM = height * 0.0254;
            return weightKg / (heightM * heightM);
        } else {
            // Weight in kg, height in cm
            double heightM = height / 100.0;
            return weight / (heightM * heightM);
        }
    }
    
    /**
     * Apply settings to profile
     */
    public void applySettings(UUID profileId, String units) {
        if (profileId == null || units == null) return;
        
        if ("metric".equals(units) || "imperial".equals(units)) {
            profileRepo.updateProfileSettings(profileId, units);
            
            // Notify listeners of profile update
            Profile updatedProfile = profileRepo.findById(profileId);
            if (updatedProfile != null) {
                notifyProfileUpdated(toDTO(updatedProfile));
            }
        }
    }
    
    /**
     * Set active profile (deactivate others first)
     */
    public void setActiveProfile(UUID profileId) {
        if (profileId == null) return;
        
        // Get the old active profile for notification
        Profile oldActiveProfile = profileRepo.findActiveProfile();
        
        // The activateProfile method now handles deactivating all profiles first
        // in a single transaction to avoid conflicts
        profileRepo.activateProfile(profileId);
        
        // Get the newly activated profile
        Profile newActiveProfile = profileRepo.findById(profileId);
        
        // Notify listeners of the profile changes
        if (oldActiveProfile != null) {
            notifyProfileDeactivated(toDTO(oldActiveProfile));
        }
        if (newActiveProfile != null) {
            notifyProfileActivated(toDTO(newActiveProfile));
        }
    }
    
    /**
     * Get profile suggestions based on BMI
     */
    public String getHealthSuggestion(ProfileDTO profileDTO) {
        double bmi = calculateBMI(profileDTO);
        
        if (bmi < 18.5) {
            return "Consider increasing calorie intake with healthy foods";
        } else if (bmi < 25.0) {
            return "Great! Maintain your current healthy weight";
        } else if (bmi < 30.0) {
            return "Consider reducing calories and increasing exercise";
        } else {
            return "Consult with a healthcare provider for weight management";
        }
    }
    
    /**
     * Convert domain entity to DTO
     */
    public ProfileDTO toDTO(Profile profile) {
        if (profile == null) return null;
        
        return new ProfileDTO(
            profile.getId(),
            profile.getName(),
            profile.getAge(),
            profile.getSex(),
            profile.getWeight(),
            profile.getHeight(),
            profile.isActive(),
            profile.getUnits()
        );
    }
    
    /**
     * Convert DTO to domain entity
     */
    public Profile fromDTO(ProfileDTO dto) {
        if (dto == null) return null;
        
        return new Profile(
            dto.getId(),
            dto.getName(),
            dto.getAge(),
            dto.getSex(),
            dto.getWeight(),
            dto.getHeight(),
            dto.isActive(),
            dto.getUnits(),
            null // createdAt will be set by entity
        );
    }
    
    /**
     * Convert list of entities to DTOs
     */
    public List<ProfileDTO> toDTOList(List<Profile> profiles) {
        if (profiles == null) return null;
        
        return profiles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Observer pattern methods (DD-8)
    
    /**
     * Add listener for profile change events
     */
    public void addProfileChangeListener(ProfileChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove listener for profile change events
     */
    public void removeProfileChangeListener(ProfileChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners when a profile is activated
     */
    private void notifyProfileActivated(ProfileDTO profile) {
        for (ProfileChangeListener listener : listeners) {
            try {
                listener.onProfileActivated(profile);
            } catch (Exception e) {
                System.err.println("Error notifying profile activation listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners when a profile is deactivated
     */
    private void notifyProfileDeactivated(ProfileDTO profile) {
        for (ProfileChangeListener listener : listeners) {
            try {
                listener.onProfileDeactivated(profile);
            } catch (Exception e) {
                System.err.println("Error notifying profile deactivation listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners when a profile is created
     */
    private void notifyProfileCreated(ProfileDTO profile) {
        for (ProfileChangeListener listener : listeners) {
            try {
                listener.onProfileCreated(profile);
            } catch (Exception e) {
                System.err.println("Error notifying profile creation listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners when a profile is updated
     */
    private void notifyProfileUpdated(ProfileDTO profile) {
        for (ProfileChangeListener listener : listeners) {
            try {
                listener.onProfileUpdated(profile);
            } catch (Exception e) {
                System.err.println("Error notifying profile update listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners when a profile is deleted
     */
    private void notifyProfileDeleted(String profileId) {
        for (ProfileChangeListener listener : listeners) {
            try {
                listener.onProfileDeleted(profileId);
            } catch (Exception e) {
                System.err.println("Error notifying profile deletion listener: " + e.getMessage());
            }
        }
    }
} 