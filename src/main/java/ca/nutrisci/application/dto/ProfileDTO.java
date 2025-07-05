package ca.nutrisci.application.dto;

import java.util.UUID;

/**
 * ProfileDTO - Data Transfer Object for profile information
 * Part of the Application Layer
 */
public class ProfileDTO {
    
    private UUID id;
    private String name;
    private int age;
    private String sex;
    private double weight;
    private double height;
    private boolean isActive;
    private String units; // "metric" or "imperial"
    
    // Default constructor
    public ProfileDTO() {
        this.id = UUID.randomUUID();
        this.isActive = false;
        this.units = "metric";
    }
    
    // Constructor for new profile
    public ProfileDTO(String name, int age, String sex, double weight, double height) {
        this();
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.weight = weight;
        this.height = height;
    }
    
    // Full constructor
    public ProfileDTO(UUID id, String name, int age, String sex, double weight, double height, 
                     boolean isActive, String units) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.weight = weight;
        this.height = height;
        this.isActive = isActive;
        this.units = units;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }
    
    // Utility methods
    public double calculateBMI() {
        if (height <= 0 || weight <= 0) return 0.0;
        
        if ("imperial".equals(units)) {
            // Convert to metric for calculation: weight in pounds, height in inches
            double weightKg = weight * 0.453592;
            double heightM = height * 0.0254;
            return weightKg / (heightM * heightM);
        } else {
            // Metric: weight in kg, height in cm
            double heightM = height / 100.0;
            return weight / (heightM * heightM);
        }
    }
    
    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25.0) return "Normal weight";
        else if (bmi < 30.0) return "Overweight";
        else return "Obese";
    }
    
    @Override
    public String toString() {
        return String.format("ProfileDTO{id=%s, name='%s', age=%d, sex='%s', BMI=%.1f}",
                id, name, age, sex, calculateBMI());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProfileDTO that = (ProfileDTO) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 