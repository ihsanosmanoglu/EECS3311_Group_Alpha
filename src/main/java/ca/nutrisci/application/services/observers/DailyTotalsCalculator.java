package ca.nutrisci.application.services.observers;

import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DailyTotalsCalculator - Background calculation observer for daily nutrition totals
 * Part of the Application Layer - Observer Pattern
 */
public class DailyTotalsCalculator implements MealLogListener {
    
    // Cache of daily totals: ProfileId -> Date -> NutrientInfo
    private Map<UUID, Map<LocalDate, NutrientInfo>> dailyTotalsCache;
    
    public DailyTotalsCalculator() {
        this.dailyTotalsCache = new HashMap<>();
    }
    
    @Override
    public void onMealLogged(MealDTO meal) {
        if (meal == null || meal.getProfileId() == null || meal.getDate() == null) {
            return;
        }
        
        System.out.println("Calculating daily totals for meal: " + meal.getMealType() + " on " + meal.getDate());
        
        UUID profileId = meal.getProfileId();
        LocalDate date = meal.getDate();
        
        // Get or create daily totals for this profile and date
        Map<LocalDate, NutrientInfo> profileTotals = dailyTotalsCache.computeIfAbsent(profileId, k -> new HashMap<>());
        NutrientInfo currentTotals = profileTotals.getOrDefault(date, new NutrientInfo());
        
        // Add this meal's nutrients to the daily total
        if (meal.getNutrients() != null) {
            NutrientInfo updatedTotals = currentTotals.add(meal.getNutrients());
            profileTotals.put(date, updatedTotals);
            
            System.out.println("Updated daily totals - Calories: " + updatedTotals.getCalories() + 
                             ", Protein: " + updatedTotals.getProtein() + "g");
        }
    }
    
    @Override
    public void onMealUpdated(MealDTO meal) {
        // For simplicity, recalculate the whole day when a meal is updated
        if (meal != null) {
            clearDayTotals(meal.getProfileId(), meal.getDate());
            onMealLogged(meal);
        }
    }
    
    @Override
    public void onMealDeleted(String mealId) {
        // For simplicity, we'd need more info to properly handle deletions
        // In a real system, we'd need the profile ID and date
        System.out.println("Meal deleted, daily totals may need recalculation: " + mealId);
    }
    
    /**
     * Get daily totals for a specific profile and date
     */
    public NutrientInfo getDailyTotals(UUID profileId, LocalDate date) {
        if (profileId == null || date == null) {
            return new NutrientInfo();
        }
        
        Map<LocalDate, NutrientInfo> profileTotals = dailyTotalsCache.get(profileId);
        if (profileTotals == null) {
            return new NutrientInfo();
        }
        
        return profileTotals.getOrDefault(date, new NutrientInfo());
    }
    
    /**
     * Clear daily totals for a specific day (for recalculation)
     */
    public void clearDayTotals(UUID profileId, LocalDate date) {
        if (profileId == null || date == null) return;
        
        Map<LocalDate, NutrientInfo> profileTotals = dailyTotalsCache.get(profileId);
        if (profileTotals != null) {
            profileTotals.remove(date);
        }
    }
    
    /**
     * Clear all cached totals for a profile
     */
    public void clearProfileTotals(UUID profileId) {
        if (profileId != null) {
            dailyTotalsCache.remove(profileId);
        }
    }
    
    /**
     * Get all cached totals (for debugging/monitoring)
     */
    public Map<UUID, Map<LocalDate, NutrientInfo>> getAllTotals() {
        return new HashMap<>(dailyTotalsCache);
    }
    
    /**
     * Check if profile meets daily calorie target
     */
    public boolean meetsCalorieTarget(UUID profileId, LocalDate date, double targetCalories) {
        NutrientInfo dailyTotals = getDailyTotals(profileId, date);
        double actualCalories = dailyTotals.getCalories();
        
        // Within 10% of target is considered "meeting" the target
        return Math.abs(actualCalories - targetCalories) / targetCalories <= 0.1;
    }
    
    /**
     * Get nutrition summary for a day
     */
    public String getDailySummary(UUID profileId, LocalDate date) {
        NutrientInfo totals = getDailyTotals(profileId, date);
        
        return String.format("Daily Totals for %s: %.0f calories, %.1fg protein, %.1fg carbs, %.1fg fat, %.1fg fiber",
                date, totals.getCalories(), totals.getProtein(), totals.getCarbs(), 
                totals.getFat(), totals.getFiber());
    }
} 