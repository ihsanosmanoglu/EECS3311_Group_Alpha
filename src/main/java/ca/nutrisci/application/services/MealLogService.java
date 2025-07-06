package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.util.List;

/**
 * MealLogService - Simple service for meal validation and business logic
 * Follows SRP - only handles business rules and calculations
 */
public class MealLogService {
    
    // Valid meal types (DRY - centralized)
    private static final String[] VALID_MEAL_TYPES = {"breakfast", "lunch", "dinner", "snack"};
    
    /**
     * Validate a meal DTO - centralized validation (DRY)
     */
    public boolean validateMeal(MealDTO mealDTO) {
        if (mealDTO == null) return false;
        
        // Check required fields
        if (mealDTO.getProfileId() == null || 
            mealDTO.getDate() == null || 
            mealDTO.getMealType() == null || mealDTO.getMealType().trim().isEmpty()) {
            return false;
        }
        
        // Check ingredients list is not null (but can be empty)
        if (mealDTO.getIngredients() == null) {
            return false;
        }
        
        // Check quantities match ingredients (both can be empty)
        if (mealDTO.getQuantities() == null || 
            mealDTO.getIngredients().size() != mealDTO.getQuantities().size()) {
            return false;
        }
        
        // Check valid meal type (DRY - using centralized method)
        if (!isValidMealType(mealDTO.getMealType())) {
            return false;
        }
        
        // Check all quantities are positive (only if ingredients exist)
        if (!mealDTO.getIngredients().isEmpty()) {
            for (Double quantity : mealDTO.getQuantities()) {
                if (quantity == null || quantity <= 0) {
                    return false;
                }
            }
            
            // Check all ingredients are not empty (only if ingredients exist)
            for (String ingredient : mealDTO.getIngredients()) {
                if (ingredient == null || ingredient.trim().isEmpty()) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Calculate nutrients for a meal (simplified)
     */
    public MealDTO calculateNutrients(MealDTO mealDTO, INutritionGateway nutritionGateway) {
        if (mealDTO == null || nutritionGateway == null) {
            return mealDTO;
        }
        
        NutrientInfo totalNutrients = new NutrientInfo();
        
        for (int i = 0; i < mealDTO.getIngredients().size(); i++) {
            String ingredient = mealDTO.getIngredients().get(i);
            double quantity = mealDTO.getQuantities().get(i);
            
            NutrientInfo ingredientNutrients = nutritionGateway.lookupIngredient(ingredient);
            if (ingredientNutrients != null) {
                // Scale by quantity (per 100g)
                NutrientInfo scaledNutrients = ingredientNutrients.multiply(quantity / 100.0);
                totalNutrients = totalNutrients.add(scaledNutrients);
            }
        }
        
        mealDTO.setNutrients(totalNutrients);
        return mealDTO;
    }
    
    /**
     * Calculate daily totals - simple aggregation
     */
    public NutrientInfo calculateDailyTotals(List<MealDTO> meals) {
        if (meals == null || meals.isEmpty()) {
            return new NutrientInfo();
        }
        
        NutrientInfo dailyTotals = new NutrientInfo();
        for (MealDTO meal : meals) {
            if (meal.getNutrients() != null) {
                dailyTotals = dailyTotals.add(meal.getNutrients());
            }
        }
        
        return dailyTotals;
    }
    
    /**
     * Check if meal type is valid (DRY - centralized)
     */
    public static boolean isValidMealType(String mealType) {
        if (mealType == null) return false;
        
        String lowerType = mealType.toLowerCase();
        for (String validType : VALID_MEAL_TYPES) {
            if (validType.equals(lowerType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get meal summary - simple formatting
     */
    public String getMealSummary(MealDTO meal) {
        if (meal == null) return "Invalid meal";
        
        StringBuilder summary = new StringBuilder();
        summary.append(meal.getMealType().toUpperCase()).append(" (").append(meal.getDate()).append("): ");
        summary.append(String.join(", ", meal.getIngredients()));
        
        if (meal.getNutrients() != null) {
            summary.append(" - ").append(String.format("%.0f calories", meal.getNutrients().getCalories()));
        }
        
        return summary.toString();
    }
} 