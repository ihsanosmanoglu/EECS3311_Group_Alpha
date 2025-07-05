package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * IVisualizationFacade - Interface for visualization operations
 * Part of the Application Layer - Facade Pattern
 */
public interface IVisualizationFacade {
    
    /**
     * Generate daily intake chart
     */
    ChartDTO generateDailyIntakeChart(UUID profileId, LocalDate date, List<MealDTO> meals);
    
    /**
     * Generate weekly trend chart
     */
    ChartDTO generateWeeklyTrendChart(UUID profileId, LocalDate startDate, List<MealDTO> weekMeals);
    
    /**
     * Generate macronutrient breakdown chart
     */
    ChartDTO generateMacronutrientChart(UUID profileId, LocalDate date);
    
    /**
     * Generate calorie goal progress chart
     */
    ChartDTO generateCalorieGoalChart(UUID profileId, LocalDate date, double targetCalories);
    
    /**
     * Generate nutrient comparison chart
     */
    ChartDTO generateNutrientComparisonChart(UUID profileId, LocalDate date, NutrientInfo targets);
    
    /**
     * Get cached chart if available
     */
    ChartDTO getCachedChart(UUID profileId, String chartType);
    
    /**
     * Cache a chart
     */
    void cacheChart(UUID profileId, String chartType, ChartDTO chart);
    
    /**
     * Clear chart cache for profile
     */
    void clearChartCache(UUID profileId);
    
    /**
     * Get chart recommendations
     */
    String getChartRecommendations(UUID profileId, LocalDate date);
    
    /**
     * Get available chart types
     */
    String[] getAvailableChartTypes(UUID profileId);
    
    /**
     * Generate chart by type
     */
    ChartDTO generateChart(UUID profileId, String chartType, LocalDate date, Object... params);
    
    /**
     * Get daily nutrition totals
     */
    NutrientInfo getDailyTotals(UUID profileId, LocalDate date);
    
    /**
     * Get nutrition summary
     */
    String getNutritionSummary(UUID profileId, LocalDate date);
    
    /**
     * Check if calorie target is met
     */
    boolean isCalorieTargetMet(UUID profileId, LocalDate date, double targetCalories);
    
    /**
     * Get chart cache statistics
     */
    String getChartCacheStats();
    
    /**
     * Clear all chart cache
     */
    void clearAllChartCache();
    
    /**
     * Check if chart is cached
     */
    boolean isChartCached(UUID profileId, String chartType);
    
    /**
     * Get default chart for profile
     */
    ChartDTO getDefaultChart(UUID profileId, LocalDate date);
} 