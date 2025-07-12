package ca.nutrisci.application.dto;

/**
 * SwapGoalDTO - Data Transfer Object for food swap goal configuration
 * Part of the Application Layer
 */
public class SwapGoalDTO {
    
    // Enum for goal actions
    public enum GoalAction {
        INCREASE, DECREASE
    }
    
    private String goalTarget; // "calories", "fiber", "protein", etc.
    private GoalAction action; // INCREASE or DECREASE
    private double intensity; // 0.0 to 1.0 (low to high intensity)
    private double targetValue; // specific target value (e.g., 100 calories, 5g fiber)
    
    // Default constructor
    public SwapGoalDTO() {
        this.intensity = 0.5; // moderate intensity by default
        this.targetValue = 0.0;
        this.goalTarget = "calories";
        this.action = GoalAction.DECREASE;
    }
    
    // Constructor with nutrient, action, and target value
    public SwapGoalDTO(String goalTarget, GoalAction action, double targetValue) {
        this.goalTarget = goalTarget;
        this.action = action;
        this.targetValue = targetValue;
        this.intensity = 0.5; // moderate intensity by default
    }
    
    // Constructor with full parameters
    public SwapGoalDTO(String goalTarget, GoalAction action, double intensity, double targetValue) {
        this.goalTarget = goalTarget;
        this.action = action;
        this.intensity = Math.max(0.0, Math.min(1.0, intensity));
        this.targetValue = targetValue;
    }
    
    // Getters and Setters
    public String getGoalTarget() { return goalTarget; }
    public void setGoalTarget(String goalTarget) { 
        this.goalTarget = goalTarget; 
    }
    
    public GoalAction getAction() { return action; }
    public void setAction(GoalAction action) { 
        this.action = action; 
    }
    
    public double getIntensity() { return intensity; }
    public void setIntensity(double intensity) { 
        this.intensity = Math.max(0.0, Math.min(1.0, intensity)); 
    }
    
    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { 
        this.targetValue = targetValue; 
    }
    
    // Utility methods
    public boolean isReduceGoal() {
        return action == GoalAction.DECREASE;
    }
    
    public boolean isIncreaseGoal() {
        return action == GoalAction.INCREASE;
    }
    
    public String getIntensityLevel() {
        if (intensity < 0.33) return "Low";
        else if (intensity < 0.67) return "Moderate";
        else return "High";
    }
    
    // Backward compatibility - get goal type as string
    public String getGoalType() {
        return action.name().toLowerCase() + "_" + goalTarget;
    }
    
    // Validation
    public boolean isValid() {
        return goalTarget != null && !goalTarget.trim().isEmpty() &&
               action != null &&
               intensity >= 0.0 && intensity <= 1.0 &&
               targetValue >= 0.0;
    }
    
    // Predefined goal types
    public static SwapGoalDTO reduceCalories(double targetValue) {
        return new SwapGoalDTO("calories", GoalAction.DECREASE, targetValue);
    }
    
    public static SwapGoalDTO increaseFiber(double targetValue) {
        return new SwapGoalDTO("fiber", GoalAction.INCREASE, targetValue);
    }
    
    public static SwapGoalDTO reduceNutrient(String nutrient, double targetValue) {
        return new SwapGoalDTO(nutrient, GoalAction.DECREASE, targetValue);
    }
    
    public static SwapGoalDTO increaseNutrient(String nutrient, double targetValue) {
        return new SwapGoalDTO(nutrient, GoalAction.INCREASE, targetValue);
    }
    
    @Override
    public String toString() {
        return String.format("SwapGoalDTO{goalTarget='%s', action=%s, intensity=%.2f (%s), targetValue=%.1f}",
                goalTarget, action, intensity, getIntensityLevel(), targetValue);
    }
} 