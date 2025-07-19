package ca.nutrisci.application.dto;

import java.util.UUID;

/**
 * SwapDTO - Data Transfer Object for food swap information
 * Part of the Application Layer
 */
public class SwapDTO {
    
    private String originalFood;
    private String replacementFood;
    private String swapReason;
    private String goalType;
    private double impactScore;
    private NutrientInfo originalNutrition;
    private NutrientInfo replacementNutrition;
    private UUID swapHistoryId;
    
    // Default constructor
    public SwapDTO() {
        this.originalNutrition = new NutrientInfo();
        this.replacementNutrition = new NutrientInfo();
        this.impactScore = 0.0;
    }
    
    // Constructor for basic swap
    public SwapDTO(String originalFood, String replacementFood, String swapReason) {
        this();
        this.originalFood = originalFood;
        this.replacementFood = replacementFood;
        this.swapReason = swapReason;
    }
    
    // Full constructor
    public SwapDTO(String originalFood, String replacementFood, String swapReason,
                   String goalType, double impactScore, NutrientInfo originalNutrition, 
                   NutrientInfo replacementNutrition) {
        this.originalFood = originalFood;
        this.replacementFood = replacementFood;
        this.swapReason = swapReason;
        this.goalType = goalType;
        this.impactScore = impactScore;
        this.originalNutrition = originalNutrition;
        this.replacementNutrition = replacementNutrition;
    }
    
    // Getters and Setters
    public String getOriginalFood() { return originalFood; }
    public void setOriginalFood(String originalFood) { 
        this.originalFood = originalFood; 
    }
    
    public String getReplacementFood() { return replacementFood; }
    public void setReplacementFood(String replacementFood) { 
        this.replacementFood = replacementFood; 
    }
    
    public String getSwapReason() { return swapReason; }
    public void setSwapReason(String swapReason) { 
        this.swapReason = swapReason; 
    }
    
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { 
        this.goalType = goalType; 
    }
    
    public double getImpactScore() { return impactScore; }
    public void setImpactScore(double impactScore) { 
        this.impactScore = impactScore; 
    }
    
    public NutrientInfo getOriginalNutrition() { return originalNutrition; }
    public void setOriginalNutrition(NutrientInfo originalNutrition) { 
        this.originalNutrition = originalNutrition; 
    }
    
    public NutrientInfo getReplacementNutrition() { return replacementNutrition; }
    public void setReplacementNutrition(NutrientInfo replacementNutrition) { 
        this.replacementNutrition = replacementNutrition; 
    }
    
    public UUID getSwapHistoryId() { return swapHistoryId; }
    public void setSwapHistoryId(UUID swapHistoryId) { 
        this.swapHistoryId = swapHistoryId; 
    }
    
    // Utility methods
    public boolean isPositiveSwap() {
        return impactScore > 0.5; // Threshold for a "good" swap
    }
    
    public double getCalorieChange() {
        if (originalNutrition == null || replacementNutrition == null) return 0.0;
        return replacementNutrition.getCalories() - originalNutrition.getCalories();
    }
    
    public double getFiberChange() {
        if (originalNutrition == null || replacementNutrition == null) return 0.0;
        return replacementNutrition.getFiber() - originalNutrition.getFiber();
    }
    
    public double getProteinChange() {
        if (originalNutrition == null || replacementNutrition == null) return 0.0;
        return replacementNutrition.getProtein() - originalNutrition.getProtein();
    }
    
    public double getCarbohydrateChange() {
        if (originalNutrition == null || replacementNutrition == null) return 0.0;
        return replacementNutrition.getCarbs() - originalNutrition.getCarbs();
    }
    
    public double getFatChange() {
        if (originalNutrition == null || replacementNutrition == null) return 0.0;
        return replacementNutrition.getFat() - originalNutrition.getFat();
    }
    
    public String getSwapSummary() {
        if (originalNutrition == null || replacementNutrition == null) {
            return String.format("Replace %s with %s", originalFood, replacementFood);
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Replace %s with %s", originalFood, replacementFood));
        
        double calorieChange = getCalorieChange();
        double fiberChange = getFiberChange();
        double proteinChange = getProteinChange();
        
        if (calorieChange != 0) {
            summary.append(String.format(" | Calories: %+.0f", calorieChange));
        }
        if (fiberChange != 0) {
            summary.append(String.format(" | Fiber: %+.1fg", fiberChange));
        }
        if (proteinChange != 0) {
            summary.append(String.format(" | Protein: %+.1fg", proteinChange));
        }
        
        return summary.toString();
    }
    
    public boolean isValid() {
        return originalFood != null && !originalFood.trim().isEmpty() &&
               replacementFood != null && !replacementFood.trim().isEmpty() &&
               goalType != null && !goalType.trim().isEmpty() &&
               !originalFood.equals(replacementFood);
    }
    
    @Override
    public String toString() {
        return String.format("SwapDTO{%s → %s, goal=%s, impact=%.2f, calories=%+.1f}",
                originalFood, replacementFood, goalType, impactScore, getCalorieChange());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SwapDTO swapDTO = (SwapDTO) obj;
        return originalFood != null ? originalFood.equals(swapDTO.originalFood) : 
               swapDTO.originalFood == null &&
               replacementFood != null ? replacementFood.equals(swapDTO.replacementFood) :
               swapDTO.replacementFood == null;
    }
    
    @Override
    public int hashCode() {
        int result = originalFood != null ? originalFood.hashCode() : 0;
        result = 31 * result + (replacementFood != null ? replacementFood.hashCode() : 0);
        return result;
    }
} 