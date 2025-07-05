package ca.nutrisci.application.dto;

/**
 * ProfileDTO - Data Transfer Object for profile information
 * Part of the Application Layer
 */
public class ProfileDTO {
    
    private String name;
    private int age;
    private String gender;
    private String activityLevel;
    
    public ProfileDTO() {}
    
    public ProfileDTO(String name, int age, String gender, String activityLevel) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    
    // Basic validation
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && 
               age > 0 && age < 150 &&
               gender != null && !gender.trim().isEmpty() &&
               activityLevel != null && !activityLevel.trim().isEmpty();
    }
} 