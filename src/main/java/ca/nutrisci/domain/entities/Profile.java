package ca.nutrisci.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Profile - Domain entity for user profile
 * Part of the Domain Layer
 */
public class Profile {
    
    private UUID id;
    private String name;
    private int age;
    private String sex;
    private double weight;
    private double height;
    private boolean isActive;
    private String units;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    
    // Default constructor
    public Profile() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.isActive = false;
        this.units = "metric";
    }
    
    // Constructor for new profile
    public Profile(String name, int age, String sex, double weight, double height) {
        this();
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.weight = weight;
        this.height = height;
        validateProfile();
    }
    
    // Full constructor
    public Profile(UUID id, String name, int age, String sex, double weight, double height, 
                   boolean isActive, String units, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.weight = weight;
        this.height = height;
        this.isActive = isActive;
        this.units = units;
        this.createdAt = createdAt;
        this.lastModified = LocalDateTime.now();
        validateProfile();
    }
    
    // Business Logic Methods
    
    /**
     * Calculate BMI based on current weight and height
     */
    public double calculateBMI() {
        if (!isValidMeasurements()) {
            throw new IllegalStateException("Invalid weight or height for BMI calculation");
        }
        
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
     * Get BMI category based on WHO standards
     */
    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25.0) return "Normal weight";
        else if (bmi < 30.0) return "Overweight";
        else return "Obese";
    }
    
    /**
     * Calculate daily calorie needs using Harris-Benedict Equation
     */
    public double calculateBasalMetabolicRate() {
        if (!isValidMeasurements()) {
            throw new IllegalStateException("Invalid measurements for BMR calculation");
        }
        
        double weightKg = "imperial".equals(units) ? weight * 0.453592 : weight;
        double heightCm = "imperial".equals(units) ? height * 2.54 : height;
        
        // Harris-Benedict Equation
        if ("male".equalsIgnoreCase(sex) || "m".equalsIgnoreCase(sex)) {
            return 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age);
        } else {
            return 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age);
        }
    }
    
    /**
     * Calculate daily calorie needs based on activity level
     */
    public double calculateDailyCalorieNeeds(String activityLevel) {
        double bmr = calculateBasalMetabolicRate();
        
        switch (activityLevel.toLowerCase()) {
            case "sedentary": return bmr * 1.2;
            case "light": return bmr * 1.375;
            case "moderate": return bmr * 1.55;
            case "active": return bmr * 1.725;
            case "very_active": return bmr * 1.9;
            default: return bmr * 1.375; // Default to light activity
        }
    }
    
    /**
     * Update profile measurements
     */
    public void updateMeasurements(double weight, double height) {
        if (weight <= 0 || height <= 0) {
            throw new IllegalArgumentException("Weight and height must be positive values");
        }
        this.weight = weight;
        this.height = height;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Activate this profile
     */
    public void activate() {
        this.isActive = true;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Deactivate this profile
     */
    public void deactivate() {
        this.isActive = false;
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Change units system
     */
    public void changeUnits(String newUnits) {
        if (!"metric".equals(newUnits) && !"imperial".equals(newUnits)) {
            throw new IllegalArgumentException("Units must be 'metric' or 'imperial'");
        }
        
        if (!newUnits.equals(this.units)) {
            convertMeasurements(newUnits);
            this.units = newUnits;
            this.lastModified = LocalDateTime.now();
        }
    }
    
    // Validation Methods
    
    private void validateProfile() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        if (sex == null || (!sex.equalsIgnoreCase("male") && !sex.equalsIgnoreCase("female") &&
                            !sex.equalsIgnoreCase("m") && !sex.equalsIgnoreCase("f"))) {
            throw new IllegalArgumentException("Sex must be 'male', 'female', 'M', or 'F'");
        }
        if (!isValidMeasurements()) {
            throw new IllegalArgumentException("Invalid weight or height measurements");
        }
    }
    
    private boolean isValidMeasurements() {
        if ("metric".equals(units)) {
            return weight > 0 && weight < 1000 && height > 0 && height < 300; // kg, cm
        } else {
            return weight > 0 && weight < 2200 && height > 0 && height < 120; // lbs, inches
        }
    }
    
    private void convertMeasurements(String newUnits) {
        if ("metric".equals(units) && "imperial".equals(newUnits)) {
            // Convert kg to lbs, cm to inches
            weight = weight * 2.20462;
            height = height * 0.393701;
        } else if ("imperial".equals(units) && "metric".equals(newUnits)) {
            // Convert lbs to kg, inches to cm
            weight = weight * 0.453592;
            height = height * 2.54;
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
        this.lastModified = LocalDateTime.now();
    }
    
    public int getAge() { return age; }
    public void setAge(int age) { 
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        this.age = age;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getSex() { return sex; }
    public void setSex(String sex) { 
        if (sex == null || (!sex.equalsIgnoreCase("male") && !sex.equalsIgnoreCase("female") &&
                            !sex.equalsIgnoreCase("m") && !sex.equalsIgnoreCase("f"))) {
            throw new IllegalArgumentException("Sex must be 'male', 'female', 'M', or 'F'");
        }
        this.sex = sex;
        this.lastModified = LocalDateTime.now();
    }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { 
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        this.weight = weight;
        this.lastModified = LocalDateTime.now();
    }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { 
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        this.height = height;
        this.lastModified = LocalDateTime.now();
    }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { 
        this.isActive = active;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getUnits() { return units; }
    
    public void setUnits(String units) {
        if (units != null && !units.equals(this.units)) {
            changeUnits(units);
        }
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastModified() { return lastModified; }
    
    @Override
    public String toString() {
        return String.format("Profile{id=%s, name='%s', age=%d, sex='%s', BMI=%.1f, active=%s}",
                id, name, age, sex, calculateBMI(), isActive);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Profile profile = (Profile) obj;
        return id != null ? id.equals(profile.id) : profile.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 