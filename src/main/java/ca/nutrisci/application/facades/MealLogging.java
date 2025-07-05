package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.services.MealLogService;
import ca.nutrisci.application.services.observers.DailyTotalsCalculator;
import ca.nutrisci.domain.entities.Meal;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MealLogging - Facade for all meal logging operations
 * Part of the Application Layer - Facade Pattern
 * This is the main entry point for UI meal operations
 */
public class MealLogging implements IMealLogFacade {
    
    private MealLogService mealLogService;
    private MealLogRepo mealLogRepo;
    private INutritionGateway nutritionGateway;
    private DailyTotalsCalculator dailyTotalsCalculator;
    
    public MealLogging(MealLogService mealLogService, MealLogRepo mealLogRepo, 
                       INutritionGateway nutritionGateway, DailyTotalsCalculator dailyTotalsCalculator) {
        this.mealLogService = mealLogService;
        this.mealLogRepo = mealLogRepo;
        this.nutritionGateway = nutritionGateway;
        this.dailyTotalsCalculator = dailyTotalsCalculator;
    }
    
    /**
     * Log a new meal
     */
    @Override
    public MealDTO logMeal(UUID profileId, LocalDate date, String mealType, 
                          List<String> ingredients, List<Double> quantities) {
        
        // Validate inputs
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        if (mealType == null || mealType.trim().isEmpty()) {
            throw new IllegalArgumentException("Meal type cannot be empty");
        }
        
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be empty");
        }
        
        if (quantities == null || quantities.isEmpty()) {
            throw new IllegalArgumentException("Quantities cannot be empty");
        }
        
        if (ingredients.size() != quantities.size()) {
            throw new IllegalArgumentException("Ingredients and quantities must have the same count");
        }
        
        // Create meal DTO
        MealDTO mealDTO = new MealDTO(
            UUID.randomUUID(),
            profileId,
            date,
            mealType.trim().toLowerCase(),
            ingredients.stream().map(String::trim).collect(Collectors.toList()),
            quantities,
            null // nutrients will be calculated
        );
        
        // Process the meal (validate, calculate nutrients, notify observers)
        MealDTO processedMeal = mealLogService.processMealLogging(mealDTO);
        
        // Save to repository
        Meal meal = mealLogService.fromDTO(processedMeal);
        Meal savedMeal = mealLogRepo.save(meal);
        
        return mealLogService.toDTO(savedMeal);
    }
    
    /**
     * Get meal by ID
     */
    @Override
    public MealDTO getMeal(UUID mealId) {
        if (mealId == null) {
            throw new IllegalArgumentException("Meal ID cannot be null");
        }
        
        Meal meal = mealLogRepo.findById(mealId);
        if (meal == null) {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
        
        return mealLogService.toDTO(meal);
    }
    
    /**
     * Update an existing meal
     */
    @Override
    public MealDTO updateMeal(UUID mealId, List<String> ingredients, List<Double> quantities) {
        if (mealId == null) {
            throw new IllegalArgumentException("Meal ID cannot be null");
        }
        
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be empty");
        }
        
        if (quantities == null || quantities.isEmpty()) {
            throw new IllegalArgumentException("Quantities cannot be empty");
        }
        
        if (ingredients.size() != quantities.size()) {
            throw new IllegalArgumentException("Ingredients and quantities must have the same count");
        }
        
        // Get existing meal
        Meal existingMeal = mealLogRepo.findById(mealId);
        if (existingMeal == null) {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
        
        // Create updated meal DTO
        MealDTO updatedMealDTO = new MealDTO(
            mealId,
            existingMeal.getProfileId(),
            existingMeal.getDate(),
            existingMeal.getMealType(),
            ingredients.stream().map(String::trim).collect(Collectors.toList()),
            quantities,
            null // nutrients will be recalculated
        );
        
        // Calculate new nutrients
        MealDTO enrichedMeal = mealLogService.calculateNutrients(updatedMealDTO, nutritionGateway);
        
        // Update and save
        Meal updatedMeal = mealLogService.fromDTO(enrichedMeal);
        Meal savedMeal = mealLogRepo.update(updatedMeal);
        
        return mealLogService.toDTO(savedMeal);
    }
    
    /**
     * Delete a meal
     */
    @Override
    public void deleteMeal(UUID mealId) {
        if (mealId == null) {
            throw new IllegalArgumentException("Meal ID cannot be null");
        }
        
        Meal meal = mealLogRepo.findById(mealId);
        if (meal == null) {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
        
        mealLogRepo.delete(mealId);
    }
    
    /**
     * Get all meals for a profile
     */
    @Override
    public List<MealDTO> getMealsForProfile(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        List<Meal> meals = mealLogRepo.findByProfileId(profileId);
        return meals.stream()
                .map(mealLogService::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get meals for a specific date
     */
    @Override
    public List<MealDTO> getMealsForDate(UUID profileId, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        List<Meal> meals = mealLogRepo.findByProfileIdAndDate(profileId, date);
        return meals.stream()
                .map(mealLogService::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get meals for a date range
     */
    @Override
    public List<MealDTO> getMealsForDateRange(UUID profileId, LocalDate startDate, LocalDate endDate) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        List<Meal> meals = mealLogRepo.findByProfileIdAndDateRange(profileId, startDate, endDate);
        return meals.stream()
                .map(mealLogService::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get meals by type
     */
    @Override
    public List<MealDTO> getMealsByType(UUID profileId, String mealType) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (mealType == null || mealType.trim().isEmpty()) {
            throw new IllegalArgumentException("Meal type cannot be empty");
        }
        
        List<Meal> meals = mealLogRepo.findByProfileIdAndMealType(profileId, mealType.trim().toLowerCase());
        return meals.stream()
                .map(mealLogService::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate daily nutrition totals
     */
    @Override
    public NutrientInfo getDailyTotals(UUID profileId, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return dailyTotalsCalculator.getDailyTotals(profileId, date);
    }
    
    /**
     * Get meal recommendations
     */
    @Override
    public String getMealRecommendations(UUID profileId, String mealType, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (mealType == null || mealType.trim().isEmpty()) {
            throw new IllegalArgumentException("Meal type cannot be empty");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return mealLogService.getMealRecommendation(profileId, mealType.trim(), date);
    }
    
    /**
     * Get nutrition summary for a day
     */
    @Override
    public String getDailySummary(UUID profileId, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return dailyTotalsCalculator.getDailySummary(profileId, date);
    }
    
    /**
     * Check if calorie target is met
     */
    @Override
    public boolean meetsCalorieTarget(UUID profileId, LocalDate date, double targetCalories) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        if (targetCalories <= 0) {
            throw new IllegalArgumentException("Target calories must be positive");
        }
        
        return dailyTotalsCalculator.meetsCalorieTarget(profileId, date, targetCalories);
    }
    
    /**
     * Validate meal data
     */
    @Override
    public boolean validateMeal(MealDTO mealDTO) {
        return mealLogService.validateMeal(mealDTO);
    }
    
    /**
     * Get ingredient nutrition info
     */
    @Override
    public NutrientInfo getIngredientNutrition(String ingredient) {
        if (ingredient == null || ingredient.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient cannot be empty");
        }
        
        return nutritionGateway.lookupIngredient(ingredient.trim());
    }
    
    /**
     * Enrich ingredients with nutrition data
     */
    @Override
    public NutrientInfo enrichIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be empty");
        }
        
        return mealLogService.enrichWithNutrients(ingredients, nutritionGateway);
    }
} 