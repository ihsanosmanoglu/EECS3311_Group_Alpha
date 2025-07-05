package ca.nutrisci.application.services.observers;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.MealDTO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ChartCacheUpdater - Background observer for updating chart cache
 * Part of the Application Layer - Observer Pattern
 */
public class ChartCacheUpdater implements MealLogListener {
    
    // Cache for chart data: ProfileId -> ChartType -> ChartDTO
    private Map<UUID, Map<String, ChartDTO>> chartCache;
    
    public ChartCacheUpdater() {
        this.chartCache = new HashMap<>();
    }
    
    @Override
    public void onMealLogged(MealDTO meal) {
        if (meal == null || meal.getProfileId() == null) {
            return;
        }
        
        System.out.println("Updating chart cache for meal: " + meal.getMealType() + " on " + meal.getDate());
        
        UUID profileId = meal.getProfileId();
        
        // Clear relevant cached charts that need updating
        clearChartsForProfile(profileId);
        
        // Optionally, pre-generate some common charts
        generateDailyIntakeChart(profileId, meal.getDate());
    }
    
    @Override
    public void onMealUpdated(MealDTO meal) {
        // Clear cache when meals are updated
        if (meal != null && meal.getProfileId() != null) {
            clearChartsForProfile(meal.getProfileId());
        }
    }
    
    @Override
    public void onMealDeleted(String mealId) {
        // For simplicity, clear all cached charts when any meal is deleted
        // In a real system, we'd be more targeted about this
        System.out.println("Meal deleted, clearing relevant chart cache: " + mealId);
        chartCache.clear();
    }
    
    /**
     * Clear all cached charts for a profile
     */
    public void clearChartsForProfile(UUID profileId) {
        if (profileId != null) {
            chartCache.remove(profileId);
            System.out.println("Cleared chart cache for profile: " + profileId);
        }
    }
    
    /**
     * Get cached chart if available
     */
    public ChartDTO getCachedChart(UUID profileId, String chartType) {
        if (profileId == null || chartType == null) {
            return null;
        }
        
        Map<String, ChartDTO> profileCharts = chartCache.get(profileId);
        if (profileCharts == null) {
            return null;
        }
        
        return profileCharts.get(chartType);
    }
    
    /**
     * Cache a chart
     */
    public void cacheChart(UUID profileId, String chartType, ChartDTO chart) {
        if (profileId == null || chartType == null || chart == null) {
            return;
        }
        
        Map<String, ChartDTO> profileCharts = chartCache.computeIfAbsent(profileId, k -> new HashMap<>());
        profileCharts.put(chartType, chart);
        
        System.out.println("Cached chart: " + chartType + " for profile: " + profileId);
    }
    
    /**
     * Check if chart is cached
     */
    public boolean isChartCached(UUID profileId, String chartType) {
        return getCachedChart(profileId, chartType) != null;
    }
    
    /**
     * Generate and cache daily intake chart
     */
    private void generateDailyIntakeChart(UUID profileId, LocalDate date) {
        // Create a simple daily intake chart
        ChartDTO chart = new ChartDTO("pie", "Daily Intake - " + date);
        
        // Add some sample data points (in a real system, this would aggregate meal data)
        chart.addDataPoint("Breakfast", 400, "meal");
        chart.addDataPoint("Lunch", 600, "meal");
        chart.addDataPoint("Dinner", 500, "meal");
        chart.addDataPoint("Snacks", 200, "meal");
        
        chart.setXAxisLabel("Meal Type");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        // Cache it
        String chartKey = "daily_intake_" + date;
        cacheChart(profileId, chartKey, chart);
    }
    
    /**
     * Generate nutrition breakdown chart
     */
    public ChartDTO generateNutritionBreakdown(UUID profileId, MealDTO meal) {
        if (meal == null || meal.getNutrients() == null) {
            return new ChartDTO("pie", "Nutrition Breakdown");
        }
        
        ChartDTO chart = new ChartDTO("pie", "Nutrition Breakdown - " + meal.getMealType());
        
        double calories = meal.getNutrients().getCalories();
        if (calories > 0) {
            // Calculate macronutrient distribution
            double proteinCals = meal.getNutrients().getProtein() * 4;
            double carbCals = meal.getNutrients().getCarbs() * 4;
            double fatCals = meal.getNutrients().getFat() * 9;
            
            chart.addDataPoint("Protein", proteinCals, "macronutrient");
            chart.addDataPoint("Carbohydrates", carbCals, "macronutrient");
            chart.addDataPoint("Fat", fatCals, "macronutrient");
        }
        
        chart.setXAxisLabel("Nutrient");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        return chart;
    }
    
    /**
     * Clear entire cache
     */
    public void clearAllCache() {
        chartCache.clear();
        System.out.println("Cleared entire chart cache");
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        int totalProfiles = chartCache.size();
        int totalCharts = chartCache.values().stream()
                .mapToInt(Map::size)
                .sum();
        
        return String.format("Chart Cache: %d profiles, %d cached charts", totalProfiles, totalCharts);
    }
    
    /**
     * Get available chart types for a profile
     */
    public String[] getAvailableChartTypes(UUID profileId) {
        if (profileId == null) {
            return new String[0];
        }
        
        Map<String, ChartDTO> profileCharts = chartCache.get(profileId);
        if (profileCharts == null) {
            return new String[0];
        }
        
        return profileCharts.keySet().toArray(new String[0]);
    }
} 