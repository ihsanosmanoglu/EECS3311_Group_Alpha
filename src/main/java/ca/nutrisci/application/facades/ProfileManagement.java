package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ProfileDTO;

import ca.nutrisci.application.services.ProfileService;
import ca.nutrisci.domain.entities.Profile;
import ca.nutrisci.infrastructure.data.repositories.ProfileRepo;

import java.util.List;
import java.util.UUID;


/**
 * ProfileManagement - Facade for all profile-related operations
 * Part of the Application Layer - Facade Pattern
 * This is the main entry point for UI operations
 */
public class ProfileManagement implements IProfileFacade {
    

    private ProfileService profileService;
    private ProfileRepo profileRepo;
    
    public ProfileManagement(ProfileService profileService, ProfileRepo profileRepo) {
        this.profileService = profileService;
        this.profileRepo = profileRepo;
    }
    
    /**
     * Create a new profile
     */
    @Override
    public ProfileDTO createProfile(String name, int age, String sex, double weight, double height, String units) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        
        if (!"male".equalsIgnoreCase(sex) && !"female".equalsIgnoreCase(sex)) {
            throw new IllegalArgumentException("Sex must be 'male' or 'female'");
        }
        
        if (weight <= 0 || height <= 0) {
            throw new IllegalArgumentException("Weight and height must be positive");
        }
        
        if (!"metric".equalsIgnoreCase(units) && !"imperial".equalsIgnoreCase(units)) {
            throw new IllegalArgumentException("Units must be 'metric' or 'imperial'");
        }
        
        // Create profile DTO
        ProfileDTO profileDTO = new ProfileDTO(
            UUID.randomUUID(),
            name.trim(),
            age,
            sex.toLowerCase(),
            weight,
            height,
            false, // not active by default
            units.toLowerCase()
        );
        
        // Validate using service
        if (!profileService.validate(profileDTO)) {
            throw new IllegalArgumentException("Profile validation failed");
        }
        
        // Convert to entity and save
        Profile profile = profileService.fromDTO(profileDTO);
        Profile savedProfile = profileRepo.save(profile);
        
        // Return as DTO
        return profileService.toDTO(savedProfile);
    }
    
    /**
     * Get profile by ID
     */
    @Override
    public ProfileDTO getProfile(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        return profileService.toDTO(profile);
    }
    
    /**
     * Update profile information
     */
    @Override
    public ProfileDTO updateProfile(UUID profileId, String name, int age, String sex, double weight, double height) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        // Get existing profile
        Profile existingProfile = profileRepo.findById(profileId);
        if (existingProfile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        // Create updated profile DTO
        ProfileDTO updatedDTO = new ProfileDTO(
            profileId,
            name != null ? name.trim() : existingProfile.getName(),
            age >= 0 ? age : existingProfile.getAge(),
            sex != null ? sex.toLowerCase() : existingProfile.getSex(),
            weight > 0 ? weight : existingProfile.getWeight(),
            height > 0 ? height : existingProfile.getHeight(),
            existingProfile.isActive(),
            existingProfile.getUnits()
        );
        
        // Validate
        if (!profileService.validate(updatedDTO)) {
            throw new IllegalArgumentException("Updated profile validation failed");
        }
        
        // Update and save
        Profile updatedProfile = profileService.fromDTO(updatedDTO);
        Profile savedProfile = profileRepo.update(updatedProfile);
        
        return profileService.toDTO(savedProfile);
    }
    
    /**
     * Delete a profile
     */
    @Override
    public void deleteProfile(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        profileRepo.delete(profileId);
    }
    
    /**
     * Get all profiles
     */
    @Override
    public List<ProfileDTO> getAllProfiles() {
        List<Profile> profiles = profileRepo.findAll();
        return profileService.toDTOList(profiles);
    }
    
    /**
     * Get active profile
     */
    @Override
    public ProfileDTO getActiveProfile() {
        Profile activeProfile = profileRepo.findActiveProfile();
        if (activeProfile == null) {
            return null;
        }
        return profileService.toDTO(activeProfile);
    }
    
    /**
     * Set active profile
     */
    @Override
    public void setActiveProfile(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        profileService.setActiveProfile(profileId);
    }
    
    /**
     * Update profile settings
     */
    @Override
    public void updateSettings(UUID profileId, String units) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (!"metric".equalsIgnoreCase(units) && !"imperial".equalsIgnoreCase(units)) {
            throw new IllegalArgumentException("Units must be 'metric' or 'imperial'");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        profileService.applySettings(profileId, units.toLowerCase());
    }
    
    /**
     * Calculate BMI for a profile
     */
    @Override
    public double calculateBMI(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        ProfileDTO profileDTO = profileService.toDTO(profile);
        return profileService.calculateBMI(profileDTO);
    }
    
    /**
     * Get health suggestions for a profile
     */
    @Override
    public String getHealthSuggestions(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        ProfileDTO profileDTO = profileService.toDTO(profile);
        return profileService.getHealthSuggestion(profileDTO);
    }
    
    /**
     * Get profile statistics
     */
    @Override
    public String getProfileStats(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        Profile profile = profileRepo.findById(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }
        
        ProfileDTO profileDTO = profileService.toDTO(profile);
        double bmi = profileService.calculateBMI(profileDTO);
        
        return String.format("Profile: %s | Age: %d | BMI: %.1f | Units: %s",
                profile.getName(), profile.getAge(), bmi, profile.getUnits());
    }
    
    /**
     * Check if profile exists
     */
    @Override
    public boolean profileExists(UUID profileId) {
        if (profileId == null) return false;
        return profileRepo.findById(profileId) != null;
    }
    
    /**
     * Validate profile data
     */
    @Override
    public boolean validateProfile(ProfileDTO profileDTO) {
        return profileService.validate(profileDTO);

    }
} 