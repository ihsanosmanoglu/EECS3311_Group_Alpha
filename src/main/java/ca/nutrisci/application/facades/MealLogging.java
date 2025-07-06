package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.services.MealLogService;
import ca.nutrisci.infrastructure.data.repositories.IRepositoryFactory;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import ca.nutrisci.infrastructure.data.repositories.ProfileRepo;
import ca.nutrisci.infrastructure.external.adapters.INutritionGateway;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MealLogging - Facade for meal logging operations
 * Part of the Application Layer - Facade Pattern
 * Coordinates between service layer, repositories, and external adapters
 */
public class MealLogging implements IMealLogFacade {
    
    private final MealLogService mealLogService;
    private final MealLogRepo mealLogRepo;
    private final ProfileRepo profileRepo;
    private final INutritionGateway nutritionGateway;
    
    public MealLogging(IRepositoryFactory repoFactory, INutritionGateway nutritionGateway) {
        this.mealLogService = new MealLogService(); // Simplified - no dependencies needed
        this.mealLogRepo = repoFactory.getMealLogRepository();
        this.profileRepo = repoFactory.getProfileRepository();
        this.nutritionGateway = nutritionGateway;
    }
    
    @Override
    public MealDTO addMeal(MealDTO meal) {
        // Validate the meal
        if (!mealLogService.validateMeal(meal)) {
            throw new IllegalArgumentException("Invalid meal data");
        }
        
        // Enforce business rule: only one meal per type per day (except snacks)
        if (!"snack".equalsIgnoreCase(meal.getMealType())) {
            List<MealDTO> existingMeals = mealLogRepo.getMealsByTypeAndDate(
                meal.getProfileId(), meal.getDate(), meal.getMealType());
            if (!existingMeals.isEmpty()) {
                throw new IllegalArgumentException("A " + meal.getMealType() + " meal already exists for this date");
            }
        }
        
        // Calculate nutrients using the nutrition gateway
        MealDTO mealWithNutrients = mealLogService.calculateNutrients(meal, nutritionGateway);
        
        // Save the meal
        return mealLogRepo.addMeal(mealWithNutrients);
    }
    
    @Override
    public MealDTO editMeal(UUID mealId, MealDTO updatedMeal) {
        // Check if meal exists
        if (!mealLogRepo.mealExists(mealId)) {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
        
        // Validate the updated meal
        if (!mealLogService.validateMeal(updatedMeal)) {
            throw new IllegalArgumentException("Invalid meal data");
        }
        
        // Get the existing meal to preserve the original profile ID and date if needed
        MealDTO existingMeal = mealLogRepo.getSingleMealById(mealId);
        if (existingMeal == null) {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
        
        // Enforce business rule for meal type changes
        if (!"snack".equalsIgnoreCase(updatedMeal.getMealType())) {
            List<MealDTO> existingMeals = mealLogRepo.getMealsByTypeAndDate(
                updatedMeal.getProfileId(), updatedMeal.getDate(), updatedMeal.getMealType());
            
            // Filter out the current meal being edited
            existingMeals = existingMeals.stream()
                .filter(m -> !m.getId().equals(mealId))
                .collect(Collectors.toList());
            
            if (!existingMeals.isEmpty()) {
                throw new IllegalArgumentException("A " + updatedMeal.getMealType() + " meal already exists for this date");
            }
        }
        
        // Check if meal is empty after editing - if so, delete it instead of saving
        if (updatedMeal.getIngredients().isEmpty()) {
            // Delete the meal instead of saving an empty one
            mealLogRepo.deleteMeal(mealId);
            return null; // Return null to indicate meal was deleted
        }
        
        // Recalculate nutrients
        MealDTO mealWithNutrients = mealLogService.calculateNutrients(updatedMeal, nutritionGateway);
        
        // Update the meal
        return mealLogRepo.editMeal(mealId, mealWithNutrients);
    }
    
    @Override
    public void deleteMeal(UUID mealId) {
        if (!mealLogRepo.mealExists(mealId)) {
            throw new IllegalArgumentException("Meal not found: " + mealId);
        }
        
        mealLogRepo.deleteMeal(mealId);
    }
    
    @Override
    public List<MealDTO> fetchMeals(LocalDate startDate, LocalDate endDate) {
        UUID activeProfileId = getActiveProfileId();
        if (activeProfileId == null) {
            throw new IllegalStateException("No active profile found");
        }
        
        return mealLogRepo.getMealsByTimeInterval(activeProfileId, startDate, endDate);
    }
    
    @Override
    public List<MealDTO> getMealsForDate(LocalDate date) {
        UUID activeProfileId = getActiveProfileId();
        if (activeProfileId == null) {
            throw new IllegalStateException("No active profile found");
        }
        
        return mealLogRepo.getMealsByDate(activeProfileId, date);
    }
    
    @Override
    public List<MealDTO> getAllMeals() {
        UUID activeProfileId = getActiveProfileId();
        if (activeProfileId == null) {
            throw new IllegalStateException("No active profile found");
        }
        
        return mealLogRepo.getMealLogHistory(activeProfileId);
    }
    
    @Override
    public MealDTO getMealById(UUID mealId) {
        return mealLogRepo.getSingleMealById(mealId);
    }
    
    @Override
    public String getDailyNutritionSummary(UUID profileId, LocalDate date) {
        List<MealDTO> mealsForDay = mealLogRepo.getMealsByDate(profileId, date);
        
        if (mealsForDay.isEmpty()) {
            return "No meals logged for " + date;
        }
        
        NutrientInfo dailyTotals = mealLogService.calculateDailyTotals(mealsForDay);
        
        return String.format("Daily Summary for %s:\n" +
                "Meals: %d\n" +
                "Total Calories: %.0f\n" +
                "Protein: %.1fg\n" +
                "Carbs: %.1fg\n" +
                "Fat: %.1fg\n" +
                "Fiber: %.1fg",
                date, mealsForDay.size(),
                dailyTotals.getCalories(),
                dailyTotals.getProtein(),
                dailyTotals.getCarbs(),
                dailyTotals.getFat(),
                dailyTotals.getFiber());
    }
    
    @Override
    public boolean mealTypeExistsForDate(UUID profileId, LocalDate date, String mealType) {
        List<MealDTO> existingMeals = mealLogRepo.getMealsByTypeAndDate(profileId, date, mealType);
        return !existingMeals.isEmpty();
    }
    
    @Override
    public List<MealDTO> getMealsForProfile(UUID profileId) {
        return mealLogRepo.getMealLogHistory(profileId);
    }
    
    @Override
    public String getMealRecommendations(UUID profileId, String mealType, LocalDate date) {
        // Get existing meals for the day
        List<MealDTO> existingMeals = mealLogRepo.getMealsByDate(profileId, date);
        
        if (existingMeals.isEmpty()) {
            return "Consider starting with a balanced " + mealType + " including proteins, carbs, and vegetables.";
        }
        
        // Calculate what's been consumed so far
        NutrientInfo consumedNutrients = mealLogService.calculateDailyTotals(existingMeals);
        
        // Basic recommendations based on consumed nutrients
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Recommendations for your ").append(mealType).append(":\n");
        
        if (consumedNutrients.getProtein() < 20) {
            recommendations.append("- Consider adding protein (chicken, fish, eggs, or legumes)\n");
        }
        
        if (consumedNutrients.getFiber() < 10) {
            recommendations.append("- Add more fiber with vegetables or whole grains\n");
        }
        
        if (consumedNutrients.getCalories() < 800) {
            recommendations.append("- You may need more calories for sustained energy\n");
        }
        
        return recommendations.toString();
    }
    
    /**
     * Get the active profile ID
     */
    private UUID getActiveProfileId() {
        try {
            // Get the active profile from the profile repository
            ca.nutrisci.domain.entities.Profile activeProfile = profileRepo.findActiveProfile();
            if (activeProfile != null) {
                return activeProfile.getId();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public List<MealDTO> getMealsForDateRange(UUID profileId, LocalDate startDate, LocalDate endDate) {
        return mealLogRepo.getMealsByTimeInterval(profileId, startDate, endDate);
    }
    
    @Override
    public List<MealDTO> getMealsByType(UUID profileId, String mealType) {
        List<MealDTO> allMeals = mealLogRepo.getMealLogHistory(profileId);
        return allMeals.stream()
            .filter(meal -> meal.getMealType().equalsIgnoreCase(mealType))
            .collect(Collectors.toList());
    }
    
    @Override
    public NutrientInfo getDailyTotals(UUID profileId, LocalDate date) {
        List<MealDTO> mealsForDay = mealLogRepo.getMealsByDate(profileId, date);
        return mealLogService.calculateDailyTotals(mealsForDay);
    }
    
    @Override
    public boolean validateMeal(MealDTO mealDTO) {
        return mealLogService.validateMeal(mealDTO);
    }
    
    @Override
    public NutrientInfo getIngredientNutrition(String ingredient) {
        return nutritionGateway.lookupIngredient(ingredient);
    }
    
    @Override
    public NutrientInfo enrichIngredients(List<String> ingredients) {
        // Simple implementation - directly use nutrition gateway
        NutrientInfo totalNutrients = new NutrientInfo();
        for (String ingredient : ingredients) {
            NutrientInfo ingredientNutrients = nutritionGateway.lookupIngredient(ingredient);
            if (ingredientNutrients != null) {
                totalNutrients = totalNutrients.add(ingredientNutrients);
            }
        }
        return totalNutrients;
    }
    
    /**
     * Get detailed meal summary
     */
    public String getDetailedMealSummary(UUID mealId) {
        MealDTO meal = mealLogRepo.getSingleMealById(mealId);
        if (meal == null) {
            return "Meal not found";
        }
        
        return mealLogService.getMealSummary(meal);
    }
} 