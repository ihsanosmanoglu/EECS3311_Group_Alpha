package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * IMealLogFacade - Interface for meal logging operations
 * Part of the Application Layer - Facade Pattern
 */
public interface IMealLogFacade {
    
    /**
     * Log a new meal
     */
    MealDTO logMeal(UUID profileId, LocalDate date, String mealType, 
                   List<String> ingredients, List<Double> quantities);
    
    /**
     * Get meal by ID
     */
    MealDTO getMeal(UUID mealId);
    
    /**
     * Update an existing meal
     */
    MealDTO updateMeal(UUID mealId, List<String> ingredients, List<Double> quantities);
    
    /**
     * Delete a meal
     */
    void deleteMeal(UUID mealId);
    
    /**
     * Get all meals for a profile
     */
    List<MealDTO> getMealsForProfile(UUID profileId);
    
    /**
     * Get meals for a specific date
     */
    List<MealDTO> getMealsForDate(UUID profileId, LocalDate date);
    
    /**
     * Get meals for a date range
     */
    List<MealDTO> getMealsForDateRange(UUID profileId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get meals by type
     */
    List<MealDTO> getMealsByType(UUID profileId, String mealType);
    
    /**
     * Calculate daily nutrition totals
     */
    NutrientInfo getDailyTotals(UUID profileId, LocalDate date);
    
    /**
     * Get meal recommendations
     */
    String getMealRecommendations(UUID profileId, String mealType, LocalDate date);
    
    /**
     * Get nutrition summary for a day
     */
    String getDailySummary(UUID profileId, LocalDate date);
    
    /**
     * Check if calorie target is met
     */
    boolean meetsCalorieTarget(UUID profileId, LocalDate date, double targetCalories);
    
    /**
     * Validate meal data
     */
    boolean validateMeal(MealDTO mealDTO);
    
    /**
     * Get ingredient nutrition info
     */
    NutrientInfo getIngredientNutrition(String ingredient);
    
    /**
     * Enrich ingredients with nutrition data
     */
    NutrientInfo enrichIngredients(List<String> ingredients);
} 