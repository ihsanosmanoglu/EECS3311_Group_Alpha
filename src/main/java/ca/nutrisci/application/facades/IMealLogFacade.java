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
     * Add a new meal to the log
     * @param meal The meal to add
     * @return The added meal with generated ID and calculated nutrients
     */
    MealDTO addMeal(MealDTO meal);
    
    /**
     * Edit an existing meal
     * @param mealId The ID of the meal to edit
     * @param updatedMeal The updated meal data
     * @return The updated meal with recalculated nutrients
     */
    MealDTO editMeal(UUID mealId, MealDTO updatedMeal);
    
    /**
     * Delete a meal from the log
     * @param mealId The ID of the meal to delete
     */
    void deleteMeal(UUID mealId);
    
    /**
     * Fetch meals within a date range for the active profile
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of meals within the date range
     */
    List<MealDTO> fetchMeals(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get meals for a specific date for the active profile
     * @param date The date to query
     * @return List of meals for the specified date
     */
    List<MealDTO> getMealsForDate(LocalDate date);
    
    /**
     * Get all meals for the active profile
     * @return List of all meals for the active profile
     */
    List<MealDTO> getAllMeals();
    
    /**
     * Get a single meal by ID
     * @param mealId The meal ID
     * @return The meal DTO, or null if not found
     */
    MealDTO getMealById(UUID mealId);
    
    /**
     * Get daily nutrition summary for a specific date
     * @param profileId The profile ID
     * @param date The date to summarize
     * @return Nutritional summary for the day
     */
    String getDailyNutritionSummary(UUID profileId, LocalDate date);
    
    /**
     * Check if a meal type already exists for a profile on a specific date
     * Used to enforce business rule: only one breakfast/lunch/dinner per day
     * @param profileId The profile ID
     * @param date The date
     * @param mealType The meal type
     * @return true if the meal type already exists for that date
     */
    boolean mealTypeExistsForDate(UUID profileId, LocalDate date, String mealType);
    
    /**
     * Get all meals for a profile
     */
    List<MealDTO> getMealsForProfile(UUID profileId);
    
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