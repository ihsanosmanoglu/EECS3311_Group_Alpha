package ca.nutrisci.application.services.observers;

import ca.nutrisci.application.dto.MealDTO;

/**
 * MealLogListener - Observer interface for meal log events
 * Part of the Application Layer - Observer Pattern
 */
public interface MealLogListener {
    
    /**
     * Called when a meal is logged
     */
    void onMealLogged(MealDTO meal);
    
    /**
     * Called when a meal is updated
     */
    void onMealUpdated(MealDTO meal);
    
    /**
     * Called when a meal is deleted
     */
    void onMealDeleted(String mealId);
} 