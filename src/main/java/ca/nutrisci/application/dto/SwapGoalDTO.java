package ca.nutrisci.application.dto;

/**
 * SwapGoalDTO - Data Transfer Object for food swap goal configuration
 * Part of the Application Layer
 */
public class SwapGoalDTO {
    
    private String goalType; // "reduce_calories", "increase_fiber", "reduce_sodium", etc.
    private double intensity; // 0.0 to 1.0 (low to high intensity)
    private double targetValue; // specific target value (e.g., 100 calories, 5g fiber)
    private String targetNutrient; // specific nutrient to target
    
    // Default constructor
    public SwapGoalDTO() {
        this.intensity = 0.5; // moderate intensity by default
        this.targetValue = 0.0;
    }
    
    // Constructor with basic goal and target value
    public SwapGoalDTO(String goalType, double targetValue) {
        this.goalType = goalType;
        this.targetValue = targetValue;
        this.intensity = 0.5; // moderate intensity by default
        this.targetNutrient = extractNutrientFromGoalType(goalType);
    }
    
    // Constructor with goal, intensity, and target value
    public SwapGoalDTO(String goalType, double intensity, double targetValue) {
        this.goalType = goalType;
        this.intensity = Math.max(0.0, Math.min(1.0, intensity));
        this.targetValue = targetValue;
        this.targetNutrient = extractNutrientFromGoalType(goalType);
    }
    
    // Full constructor
    public SwapGoalDTO(String goalType, double intensity, double targetValue, String targetNutrient) {
        this.goalType = goalType;
        this.intensity = Math.max(0.0, Math.min(1.0, intensity));
        this.targetValue = targetValue;
        this.targetNutrient = targetNutrient;
    }
    
    // Getters and Setters
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { 
        this.goalType = goalType;
        this.targetNutrient = extractNutrientFromGoalType(goalType);
    }
    
    public double getIntensity() { return intensity; }
    public void setIntensity(double intensity) { 
        this.intensity = Math.max(0.0, Math.min(1.0, intensity)); 
    }
    
    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { 
        this.targetValue = targetValue; 
    }
    
    public String getTargetNutrient() { return targetNutrient; }
    public void setTargetNutrient(String targetNutrient) { 
        this.targetNutrient = targetNutrient; 
    }
    
    // Utility methods
    public boolean isReduceGoal() {
        return goalType != null && goalType.toLowerCase().contains("reduce");
    }
    
    public boolean isIncreaseGoal() {
        return goalType != null && goalType.toLowerCase().contains("increase");
    }
    
    public String getIntensityLevel() {
        if (intensity < 0.33) return "Low";
        else if (intensity < 0.67) return "Moderate";
        else return "High";
    }
    
    private String extractNutrientFromGoalType(String goalType) {
        if (goalType == null) return null;
        
        String lower = goalType.toLowerCase();
        if (lower.contains("calorie")) return "calories";
        if (lower.contains("fiber")) return "fiber";
        if (lower.contains("protein")) return "protein";
        if (lower.contains("sodium")) return "sodium";
        if (lower.contains("fat")) return "fat";
        if (lower.contains("carb")) return "carbs";
        if (lower.contains("sugar")) return "sugar";
        
        return "calories"; // default
    }
    
    // Validation
    public boolean isValid() {
        return goalType != null && !goalType.trim().isEmpty() &&
               intensity >= 0.0 && intensity <= 1.0 &&
               targetValue >= 0.0 &&
               targetNutrient != null && !targetNutrient.trim().isEmpty();
    }
    
    // Predefined goal types
    public static SwapGoalDTO reduceCalories(double targetValue) {
        return new SwapGoalDTO("reduce_calories", targetValue);
    }
    
    public static SwapGoalDTO increaseFiber(double targetValue) {
        return new SwapGoalDTO("increase_fiber", targetValue);
    }
    
    public static SwapGoalDTO reduceNutrient(String nutrient, double targetValue) {
        return new SwapGoalDTO("reduce_" + nutrient, targetValue);
    }
    
    public static SwapGoalDTO increaseNutrient(String nutrient, double targetValue) {
        return new SwapGoalDTO("increase_" + nutrient, targetValue);
    }
    
    @Override
    public String toString() {
        return String.format("SwapGoalDTO{goalType='%s', intensity=%.2f (%s), targetValue=%.1f, targetNutrient='%s'}",
                goalType, intensity, getIntensityLevel(), targetValue, targetNutrient);
    }
} 