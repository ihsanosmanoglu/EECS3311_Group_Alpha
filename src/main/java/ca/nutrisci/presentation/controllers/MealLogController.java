package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.dto.ProfileDTO;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.application.facades.IProfileFacade;
import ca.nutrisci.presentation.ui.MealLogPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * MealLogController - Controller for meal logging UI interactions
 * Part of the Presentation Layer - MVC Pattern
 */
public class MealLogController {
    
    private final MealLogPanel view;
    private IMealLogFacade mealLogFacade;
    private IProfileFacade profileFacade;
    
    public MealLogController(MealLogPanel view) {
        this.view = view;
        setupCallbacks();
    }
    
    public void setMealLogFacade(IMealLogFacade mealLogFacade) {
        this.mealLogFacade = mealLogFacade;
    }
    
    public void setProfileFacade(IProfileFacade profileFacade) {
        this.profileFacade = profileFacade;
    }
    
    private void setupCallbacks() {
        view.setAddMealCallback(e -> handleAddMeal());
        view.setEditMealCallback(e -> handleEditMeal());
        view.setDeleteMealCallback(e -> handleDeleteMeal());
        view.setRefreshMealsCallback(e -> handleRefreshMeals());
        view.setGetDaySummaryCallback(e -> handleGetDaySummary());
    }
    
    public void initialize() {
        // Set the current profile ID if available
        if (profileFacade != null) {
            try {
                ProfileDTO activeProfile = profileFacade.getActiveProfile();
                if (activeProfile != null) {
                    view.setCurrentProfileId(activeProfile.getId());
                    handleRefreshMeals(); // Load meals for the active profile
                } else {
                    view.showMessage("No active profile found. Please create or select a profile first.", 
                                   "No Active Profile", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                view.showMessage("Error loading active profile: " + e.getMessage(), 
                               "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleAddMeal() {
        if (mealLogFacade == null) {
            view.showMessage("Meal logging service not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Get meal data from the view
            MealDTO mealData = view.getCurrentMealData();
            
            // Validate profile is set
            if (mealData.getProfileId() == null) {
                view.showMessage("No active profile. Please select a profile first.", 
                               "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Add the meal
            MealDTO savedMeal = mealLogFacade.addMeal(mealData);
            
            // Show success message
            view.showMessage("Meal added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the meals list and clear form
            handleRefreshMeals();
            view.clearForm();
            
            // Show nutrition summary for the meal
            if (savedMeal.getNutrients() != null) {
                showMealNutritionSummary(savedMeal);
            }
            
        } catch (IllegalArgumentException e) {
            view.showMessage("Invalid meal data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            view.showMessage("Error adding meal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleEditMeal() {
        if (mealLogFacade == null) {
            view.showMessage("Meal logging service not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        UUID selectedMealId = view.getSelectedMealId();
        if (selectedMealId == null) {
            view.showMessage("Please select a meal to edit", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Get updated meal data from the view
            MealDTO updatedMeal = view.getCurrentMealData();
            
            // Edit the meal
            MealDTO editedMeal = mealLogFacade.editMeal(selectedMealId, updatedMeal);
            
            // Show success message
            view.showMessage("Meal updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the meals list
            handleRefreshMeals();
            
            // Show updated nutrition summary
            if (editedMeal.getNutrients() != null) {
                showMealNutritionSummary(editedMeal);
            }
            
        } catch (IllegalArgumentException e) {
            view.showMessage("Invalid meal data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            view.showMessage("Error editing meal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleDeleteMeal() {
        if (mealLogFacade == null) {
            view.showMessage("Meal logging service not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        UUID selectedMealId = view.getSelectedMealId();
        if (selectedMealId == null) {
            view.showMessage("Please select a meal to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Delete the meal
            mealLogFacade.deleteMeal(selectedMealId);
            
            // Show success message
            view.showMessage("Meal deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the meals list
            handleRefreshMeals();
            
            // Clear the nutrition summary
            view.showNutritionSummary("");
            
        } catch (Exception e) {
            view.showMessage("Error deleting meal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleRefreshMeals() {
        if (mealLogFacade == null) {
            view.showMessage("Meal logging service not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Get all meals for the active profile
            List<MealDTO> meals = mealLogFacade.getAllMeals();
            
            // Update the view
            view.setMeals(meals);
            
        } catch (IllegalStateException e) {
            view.showMessage("No active profile found. Please create or select a profile first.", 
                           "No Active Profile", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            view.showMessage("Error loading meals: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleGetDaySummary() {
        if (mealLogFacade == null || profileFacade == null) {
            view.showMessage("Services not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Get the selected date from the view
            String dateStr = view.getSelectedDate();
            LocalDate date = LocalDate.parse(dateStr);
            
            // Get the active profile
            ProfileDTO activeProfile = profileFacade.getActiveProfile();
            if (activeProfile == null) {
                view.showMessage("No active profile found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get the daily nutrition summary
            String summary = mealLogFacade.getDailyNutritionSummary(activeProfile.getId(), date);
            
            // Show the summary
            view.showNutritionSummary(summary);
            
        } catch (DateTimeParseException e) {
            view.showMessage("Invalid date format. Please use YYYY-MM-DD format.", 
                           "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            view.showMessage("Error getting daily summary: " + e.getMessage(), 
                           "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showMealNutritionSummary(MealDTO meal) {
        if (meal.getNutrients() != null) {
            NutrientInfo nutrients = meal.getNutrients();
            String summary = String.format(
                "Meal: %s (%s)\n" +
                "Date: %s\n" +
                "Ingredients: %s\n\n" +
                "Nutritional Information:\n" +
                "Calories: %.0f kcal\n" +
                "Protein: %.1f g\n" +
                "Carbohydrates: %.1f g\n" +
                "Fat: %.1f g\n" +
                "Fiber: %.1f g",
                meal.getMealType().substring(0, 1).toUpperCase() + meal.getMealType().substring(1),
                meal.getId(),
                meal.getDate(),
                String.join(", ", meal.getIngredients()),
                nutrients.getCalories(),
                nutrients.getProtein(),
                nutrients.getCarbs(),
                nutrients.getFat(),
                nutrients.getFiber()
            );
            
            view.showNutritionSummary(summary);
        }
    }
    
    /**
     * Handle navigation requests from the view
     */
    public void requestNavigation(String destination) {
        // This would be handled by the NavigationMediator
        // For now, we'll just show a message
        view.showMessage("Navigation to " + destination + " requested", 
                       "Navigation", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Get meals for a specific date range
     */
    public void getMealsForDateRange(LocalDate startDate, LocalDate endDate) {
        if (mealLogFacade == null) {
            view.showMessage("Meal logging service not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            List<MealDTO> meals = mealLogFacade.fetchMeals(startDate, endDate);
            view.setMeals(meals);
        } catch (Exception e) {
            view.showMessage("Error loading meals: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Get meals for today
     */
    public void getTodaysMeals() {
        LocalDate today = LocalDate.now();
        getMealsForDateRange(today, today);
    }
    
    /**
     * Validate meal data before saving
     */
    public boolean validateMealData(MealDTO meal) {
        if (mealLogFacade == null) {
            return false;
        }
        
        return mealLogFacade.validateMeal(meal);
    }
    
    /**
     * Get nutrition information for an ingredient
     */
    public void showIngredientNutrition(String ingredient) {
        if (mealLogFacade == null) {
            view.showMessage("Meal logging service not available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            NutrientInfo nutrition = mealLogFacade.getIngredientNutrition(ingredient);
            if (nutrition != null) {
                String summary = String.format(
                    "Nutrition for %s (per 100g):\n" +
                    "Calories: %.0f kcal\n" +
                    "Protein: %.1f g\n" +
                    "Carbohydrates: %.1f g\n" +
                    "Fat: %.1f g\n" +
                    "Fiber: %.1f g",
                    ingredient,
                    nutrition.getCalories(),
                    nutrition.getProtein(),
                    nutrition.getCarbs(),
                    nutrition.getFat(),
                    nutrition.getFiber()
                );
                
                view.showNutritionSummary(summary);
            } else {
                view.showNutritionSummary("No nutrition data found for: " + ingredient);
            }
        } catch (Exception e) {
            view.showMessage("Error getting ingredient nutrition: " + e.getMessage(), 
                           "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 