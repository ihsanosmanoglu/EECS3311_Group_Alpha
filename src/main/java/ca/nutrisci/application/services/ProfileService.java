package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.domain.entities.Profile;
import ca.nutrisci.infrastructure.data.repositories.ProfileRepo;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ProfileService - Business logic for profile operations
 * Part of the Application Layer
 */
public class ProfileService {
    
    private ProfileRepo profileRepo;
    
    public ProfileService(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
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
        }
    }
    
    /**
     * Set active profile (deactivate others first)
     */
    public void setActiveProfile(UUID profileId) {
        if (profileId == null) return;
        
        // Deactivate all profiles first
        profileRepo.deactivateAllProfiles();
        
        // Activate the selected profile
        profileRepo.activateProfile(profileId);
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
} 