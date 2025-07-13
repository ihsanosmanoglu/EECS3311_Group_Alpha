package ca.nutrisci.infrastructure.data.repositories;

import ca.nutrisci.application.dto.MealDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for meal logging operations
 */
public interface MealLogRepo {
    
    /**
     * Get complete meal log history for a profile
     * @param profileId The profile ID
     * @return List of all meals for the profile
     */
    List<MealDTO> getMealLogHistory(UUID profileId);
    
    /**
     * Get meals within a time interval for a profile
     * @param profileId The profile ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of meals within the time interval
     */
    List<MealDTO> getMealsByTimeInterval(UUID profileId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all meals for a specific date
     * @param profileId The profile ID
     * @param date The date to query
     * @return List of meals for the specified date
     */
    List<MealDTO> getMealsByDate(UUID profileId, LocalDate date);
    
    /**
     * Get a single meal by its ID
     * @param mealId The meal ID
     * @return The meal DTO, or null if not found
     */
    MealDTO getSingleMealById(UUID mealId);
    
    /**
     * Add a new meal to the repository
     * @param meal The meal to add
     * @return The saved meal with generated ID
     */
    MealDTO addMeal(MealDTO meal);
    
    /**
     * Edit an existing meal
     * @param mealId The meal ID to edit
     * @param meal The updated meal data
     * @return The updated meal DTO
     */
    MealDTO editMeal(UUID mealId, MealDTO meal);
    
    /**
     * Delete a meal by ID
     * @param mealId The meal ID to delete
     */
    void deleteMeal(UUID mealId);
    
    /**
     * Check if a meal exists
     * @param mealId The meal ID to check
     * @return true if the meal exists, false otherwise
     */
    boolean mealExists(UUID mealId);
    
    /**
     * Get meals of a specific type for a profile on a specific date
     * Used to enforce business rule: only one breakfast/lunch/dinner per day
     * @param profileId The profile ID
     * @param date The date
     * @param mealType The meal type (breakfast, lunch, dinner, snack)
     * @return List of meals of the specified type
     */
    List<MealDTO> getMealsByTypeAndDate(UUID profileId, LocalDate date, String mealType);
    
    /**
     * Get meals for a profile within a date range
     * @param profileId Profile ID
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return List of meals in the date range
     */
    List<MealDTO> getMealsByDateRange(UUID profileId, LocalDate from, LocalDate to);
} 