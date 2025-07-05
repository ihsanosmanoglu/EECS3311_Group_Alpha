package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.services.observers.ChartCacheUpdater;
import ca.nutrisci.application.services.observers.DailyTotalsCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ChartsService - Business logic for chart generation and data visualization
 * Part of the Application Layer
 */
public class ChartsService {
    
    private ChartCacheUpdater chartCache;
    private DailyTotalsCalculator dailyTotalsCalculator;
    
    public ChartsService(ChartCacheUpdater chartCache, DailyTotalsCalculator dailyTotalsCalculator) {
        this.chartCache = chartCache;
        this.dailyTotalsCalculator = dailyTotalsCalculator;
    }
    
    /**
     * Create daily intake pie chart
     */
    public ChartDTO createDailyIntakeChart(UUID profileId, LocalDate date, List<MealDTO> meals) {
        if (profileId == null || date == null || meals == null) {
            return new ChartDTO("pie", "Daily Intake");
        }
        
        // Check cache first
        String chartKey = "daily_intake_" + date;
        ChartDTO cachedChart = chartCache.getCachedChart(profileId, chartKey);
        if (cachedChart != null) {
            return cachedChart;
        }
        
        // Generate new chart
        ChartDTO chart = new ChartDTO("pie", "Daily Intake - " + date);
        
        // Aggregate calories by meal type
        double breakfastCals = 0, lunchCals = 0, dinnerCals = 0, snackCals = 0;
        
        for (MealDTO meal : meals) {
            if (meal.getNutrients() != null) {
                double calories = meal.getNutrients().getCalories();
                String mealType = meal.getMealType().toLowerCase();
                
                switch (mealType) {
                    case "breakfast":
                        breakfastCals += calories;
                        break;
                    case "lunch":
                        lunchCals += calories;
                        break;
                    case "dinner":
                        dinnerCals += calories;
                        break;
                    case "snack":
                        snackCals += calories;
                        break;
                }
            }
        }
        
        // Add data points
        if (breakfastCals > 0) chart.addDataPoint("Breakfast", breakfastCals, "meal");
        if (lunchCals > 0) chart.addDataPoint("Lunch", lunchCals, "meal");
        if (dinnerCals > 0) chart.addDataPoint("Dinner", dinnerCals, "meal");
        if (snackCals > 0) chart.addDataPoint("Snacks", snackCals, "meal");
        
        chart.setXAxisLabel("Meal Type");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        // Cache the chart
        chartCache.cacheChart(profileId, chartKey, chart);
        
        return chart;
    }
    
    /**
     * Create weekly nutrition trend chart
     */
    public ChartDTO createWeeklyTrendChart(UUID profileId, LocalDate startDate, List<MealDTO> weekMeals) {
        if (profileId == null || startDate == null) {
            return new ChartDTO("line", "Weekly Trends");
        }
        
        ChartDTO chart = new ChartDTO("line", "Weekly Nutrition Trends");
        
        // Group meals by day and calculate daily totals
        for (int day = 0; day < 7; day++) {
            LocalDate currentDate = startDate.plusDays(day);
            NutrientInfo dayTotals = dailyTotalsCalculator.getDailyTotals(profileId, currentDate);
            
            String dayLabel = currentDate.getDayOfWeek().name().substring(0, 3);
            chart.addDataPoint(dayLabel, dayTotals.getCalories(), "calories");
        }
        
        chart.setXAxisLabel("Day");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        return chart;
    }
    
    /**
     * Create macronutrient breakdown chart
     */
    public ChartDTO createMacronutrientChart(UUID profileId, LocalDate date) {
        if (profileId == null || date == null) {
            return new ChartDTO("pie", "Macronutrient Breakdown");
        }
        
        String chartKey = "macronutrients_" + date;
        ChartDTO cachedChart = chartCache.getCachedChart(profileId, chartKey);
        if (cachedChart != null) {
            return cachedChart;
        }
        
        ChartDTO chart = new ChartDTO("pie", "Macronutrient Breakdown - " + date);
        
        // Get daily totals
        NutrientInfo dailyTotals = dailyTotalsCalculator.getDailyTotals(profileId, date);
        
        // Calculate calories from each macronutrient
        double proteinCals = dailyTotals.getProtein() * 4;  // 4 calories per gram
        double carbCals = dailyTotals.getCarbs() * 4;       // 4 calories per gram
        double fatCals = dailyTotals.getFat() * 9;          // 9 calories per gram
        
        if (proteinCals > 0) chart.addDataPoint("Protein", proteinCals, "macronutrient");
        if (carbCals > 0) chart.addDataPoint("Carbohydrates", carbCals, "macronutrient");
        if (fatCals > 0) chart.addDataPoint("Fat", fatCals, "macronutrient");
        
        chart.setXAxisLabel("Macronutrient");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        // Cache the chart
        chartCache.cacheChart(profileId, chartKey, chart);
        
        return chart;
    }
    
    /**
     * Create calorie goal progress chart
     */
    public ChartDTO createCalorieGoalChart(UUID profileId, LocalDate date, double targetCalories) {
        if (profileId == null || date == null || targetCalories <= 0) {
            return new ChartDTO("bar", "Calorie Goal Progress");
        }
        
        ChartDTO chart = new ChartDTO("bar", "Calorie Goal Progress - " + date);
        
        // Get actual calories consumed
        NutrientInfo dailyTotals = dailyTotalsCalculator.getDailyTotals(profileId, date);
        double actualCalories = dailyTotals.getCalories();
        
        // Add data points
        chart.addDataPoint("Target", targetCalories, "goal");
        chart.addDataPoint("Actual", actualCalories, "actual");
        
        // Calculate remaining calories
        double remaining = targetCalories - actualCalories;
        if (remaining > 0) {
            chart.addDataPoint("Remaining", remaining, "remaining");
        } else {
            chart.addDataPoint("Over Target", Math.abs(remaining), "over");
        }
        
        chart.setXAxisLabel("Category");
        chart.setYAxisLabel("Calories");
        chart.setShowLegend(true);
        
        return chart;
    }
    
    /**
     * Create nutrient comparison chart
     */
    public ChartDTO createNutrientComparisonChart(UUID profileId, LocalDate date, NutrientInfo targets) {
        if (profileId == null || date == null || targets == null) {
            return new ChartDTO("bar", "Nutrient Comparison");
        }
        
        ChartDTO chart = new ChartDTO("bar", "Nutrient Comparison - " + date);
        
        NutrientInfo actualNutrients = dailyTotalsCalculator.getDailyTotals(profileId, date);
        
        // Compare key nutrients
        chart.addDataPoint("Protein Target", targets.getProtein(), "target");
        chart.addDataPoint("Protein Actual", actualNutrients.getProtein(), "actual");
        
        chart.addDataPoint("Fiber Target", targets.getFiber(), "target");
        chart.addDataPoint("Fiber Actual", actualNutrients.getFiber(), "actual");
        
        chart.addDataPoint("Sugar Target", targets.getSugar(), "target");
        chart.addDataPoint("Sugar Actual", actualNutrients.getSugar(), "actual");
        
        chart.setXAxisLabel("Nutrient");
        chart.setYAxisLabel("Grams");
        chart.setShowLegend(true);
        
        return chart;
    }
    
    /**
     * Generate chart recommendations based on data
     */
    public String generateChartRecommendations(UUID profileId, LocalDate date) {
        if (profileId == null || date == null) {
            return "No data available for recommendations";
        }
        
        NutrientInfo dailyTotals = dailyTotalsCalculator.getDailyTotals(profileId, date);
        
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Nutrition Recommendations for ").append(date).append(":\n");
        
        // Calorie recommendations
        if (dailyTotals.getCalories() < 1200) {
            recommendations.append("- Consider increasing calorie intake for adequate nutrition\n");
        } else if (dailyTotals.getCalories() > 2500) {
            recommendations.append("- Consider reducing calorie intake\n");
        }
        
        // Protein recommendations
        if (dailyTotals.getProtein() < 50) {
            recommendations.append("- Try to include more protein sources\n");
        }
        
        // Fiber recommendations
        if (dailyTotals.getFiber() < 25) {
            recommendations.append("- Add more fiber-rich foods like vegetables and whole grains\n");
        }
        
        // Sugar recommendations
        if (dailyTotals.getSugar() > 50) {
            recommendations.append("- Consider reducing sugar intake\n");
        }
        
        return recommendations.toString();
    }
    
    /**
     * Get chart for visualization facade
     */
    public ChartDTO getChart(UUID profileId, String chartType, LocalDate date, Object... params) {
        switch (chartType.toLowerCase()) {
            case "daily_intake":
                return createDailyIntakeChart(profileId, date, (List<MealDTO>) params[0]);
            case "macronutrients":
                return createMacronutrientChart(profileId, date);
            case "calorie_goal":
                return createCalorieGoalChart(profileId, date, (Double) params[0]);
            case "nutrient_comparison":
                return createNutrientComparisonChart(profileId, date, (NutrientInfo) params[0]);
            default:
                return new ChartDTO("bar", "Unknown Chart Type");
        }
    }
} 