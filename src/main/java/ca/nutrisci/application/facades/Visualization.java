package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.services.ChartsService;
import ca.nutrisci.application.services.observers.ChartCacheUpdater;
import ca.nutrisci.application.services.observers.DailyTotalsCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Visualization - Facade for all chart and visualization operations
 * Part of the Application Layer - Facade Pattern
 * This is the main entry point for UI visualization operations
 */
public class Visualization implements IVisualizationFacade {
    
    private ChartsService chartsService;
    private ChartCacheUpdater chartCache;
    private DailyTotalsCalculator dailyTotalsCalculator;
    
    public Visualization(ChartsService chartsService, ChartCacheUpdater chartCache,
                        DailyTotalsCalculator dailyTotalsCalculator) {
        this.chartsService = chartsService;
        this.chartCache = chartCache;
        this.dailyTotalsCalculator = dailyTotalsCalculator;
    }
    
    /**
     * Generate daily intake chart
     */
    @Override
    public ChartDTO generateDailyIntakeChart(UUID profileId, LocalDate date, List<MealDTO> meals) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        if (meals == null) {
            throw new IllegalArgumentException("Meals list cannot be null");
        }
        
        return chartsService.createDailyIntakeChart(profileId, date, meals);
    }
    
    /**
     * Generate weekly trend chart
     */
    @Override
    public ChartDTO generateWeeklyTrendChart(UUID profileId, LocalDate startDate, List<MealDTO> weekMeals) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        
        if (weekMeals == null) {
            throw new IllegalArgumentException("Week meals list cannot be null");
        }
        
        return chartsService.createWeeklyTrendChart(profileId, startDate, weekMeals);
    }
    
    /**
     * Generate macronutrient breakdown chart
     */
    @Override
    public ChartDTO generateMacronutrientChart(UUID profileId, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return chartsService.createMacronutrientChart(profileId, date);
    }
    
    /**
     * Generate calorie goal progress chart
     */
    @Override
    public ChartDTO generateCalorieGoalChart(UUID profileId, LocalDate date, double targetCalories) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        if (targetCalories <= 0) {
            throw new IllegalArgumentException("Target calories must be positive");
        }
        
        return chartsService.createCalorieGoalChart(profileId, date, targetCalories);
    }
    
    /**
     * Generate nutrient comparison chart
     */
    @Override
    public ChartDTO generateNutrientComparisonChart(UUID profileId, LocalDate date, NutrientInfo targets) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        if (targets == null) {
            throw new IllegalArgumentException("Target nutrients cannot be null");
        }
        
        return chartsService.createNutrientComparisonChart(profileId, date, targets);
    }
    
    /**
     * Get cached chart if available
     */
    @Override
    public ChartDTO getCachedChart(UUID profileId, String chartType) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (chartType == null || chartType.trim().isEmpty()) {
            throw new IllegalArgumentException("Chart type cannot be empty");
        }
        
        return chartCache.getCachedChart(profileId, chartType.trim());
    }
    
    /**
     * Cache a chart
     */
    @Override
    public void cacheChart(UUID profileId, String chartType, ChartDTO chart) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (chartType == null || chartType.trim().isEmpty()) {
            throw new IllegalArgumentException("Chart type cannot be empty");
        }
        
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null");
        }
        
        chartCache.cacheChart(profileId, chartType.trim(), chart);
    }
    
    /**
     * Clear chart cache for profile
     */
    @Override
    public void clearChartCache(UUID profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        chartCache.clearChartsForProfile(profileId);
    }
    
    /**
     * Get chart recommendations
     */
    @Override
    public String getChartRecommendations(UUID profileId, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return chartsService.generateChartRecommendations(profileId, date);
    }
    
    /**
     * Get available chart types
     */
    @Override
    public String[] getAvailableChartTypes(UUID profileId) {
        if (profileId == null) {
            return new String[]{"daily_intake", "weekly_trends", "macronutrients", "calorie_goal", "nutrient_comparison"};
        }
        
        String[] cachedTypes = chartCache.getAvailableChartTypes(profileId);
        if (cachedTypes.length > 0) {
            return cachedTypes;
        }
        
        return new String[]{"daily_intake", "weekly_trends", "macronutrients", "calorie_goal", "nutrient_comparison"};
    }
    
    /**
     * Generate chart by type
     */
    @Override
    public ChartDTO generateChart(UUID profileId, String chartType, LocalDate date, Object... params) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (chartType == null || chartType.trim().isEmpty()) {
            throw new IllegalArgumentException("Chart type cannot be empty");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return chartsService.getChart(profileId, chartType.trim(), date, params);
    }
    
    /**
     * Get daily nutrition totals
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
     * Get nutrition summary
     */
    @Override
    public String getNutritionSummary(UUID profileId, LocalDate date) {
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
    public boolean isCalorieTargetMet(UUID profileId, LocalDate date, double targetCalories) {
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
     * Get chart cache statistics
     */
    @Override
    public String getChartCacheStats() {
        return chartCache.getCacheStats();
    }
    
    /**
     * Clear all chart cache
     */
    @Override
    public void clearAllChartCache() {
        chartCache.clearAllCache();
    }
    
    /**
     * Check if chart is cached
     */
    @Override
    public boolean isChartCached(UUID profileId, String chartType) {
        if (profileId == null || chartType == null) return false;
        return chartCache.isChartCached(profileId, chartType.trim());
    }
    
    /**
     * Get default chart for profile
     */
    @Override
    public ChartDTO getDefaultChart(UUID profileId, LocalDate date) {
        if (profileId == null) {
            throw new IllegalArgumentException("Profile ID cannot be null");
        }
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Default to daily intake chart
        NutrientInfo dailyTotals = dailyTotalsCalculator.getDailyTotals(profileId, date);
        
        ChartDTO chart = new ChartDTO("pie", "Daily Nutrition Overview - " + date);
        
        if (dailyTotals.getCalories() > 0) {
            chart.addDataPoint("Calories", dailyTotals.getCalories(), "nutrition");
            chart.addDataPoint("Protein", dailyTotals.getProtein() * 4, "nutrition"); // calories from protein
            chart.addDataPoint("Carbs", dailyTotals.getCarbs() * 4, "nutrition"); // calories from carbs
            chart.addDataPoint("Fat", dailyTotals.getFat() * 9, "nutrition"); // calories from fat
        } else {
            chart.addDataPoint("No data", 1, "empty");
        }
        
        chart.setXAxisLabel("Nutrient");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        return chart;
    }
} 