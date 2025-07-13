package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;
import ca.nutrisci.domain.entities.FoodGroup;
import ca.nutrisci.domain.entities.NutrientType;
import ca.nutrisci.domain.entities.SwapHistory;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import ca.nutrisci.infrastructure.data.repositories.SwapHistoryRepo;
import ca.nutrisci.application.services.observers.ChartCacheUpdater;
import ca.nutrisci.application.services.observers.DailyTotalsCalculator;
import ca.nutrisci.application.dto.NutrientInfo;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

/**
 * Service for generating various types of charts
 */
public class ChartsService {
    private final ChartCacheUpdater chartCache;
    private final DailyTotalsCalculator dailyTotalsCalculator;
    private final MealLogRepo mealLogRepo;
    private final SwapHistoryRepo swapHistoryRepo;

    public ChartsService(MealLogRepo mealLogRepo, SwapHistoryRepo swapHistoryRepo) {
        if (mealLogRepo == null || swapHistoryRepo == null) {
            throw new IllegalArgumentException("Repository dependencies cannot be null");
        }
        this.chartCache = new ChartCacheUpdater();
        this.dailyTotalsCalculator = new DailyTotalsCalculator();
        this.mealLogRepo = mealLogRepo;
        this.swapHistoryRepo = swapHistoryRepo;
    }

    /**
     * Create top nutrients chart showing daily averages
     */
    public ChartDTO createTopNutrientsChart(UUID profileId, LocalDate from, LocalDate to) {
        System.out.println("[DEBUG] createTopNutrientsChart called with profileId=" + profileId + ", from=" + from + ", to=" + to);
        if (profileId == null || from == null || to == null) {
            return ChartDTO.createEmptyChart("Daily Nutrient Intake", ChartDTO.ChartType.PIE);
        }

        // Check cache first
        String chartKey = "top_nutrients_" + from + "_" + to;
        ChartDTO cachedChart = chartCache.getCachedChart(profileId, chartKey);
        if (cachedChart != null) {
            return cachedChart;
        }

        // Get meals in date range
        List<MealDTO> meals = mealLogRepo.getMealsByDateRange(profileId, from, to);
        System.out.println("[DEBUG] Meals found: " + meals.size());
        if (meals.isEmpty()) {
            System.out.println("[DEBUG] No meals found for this range.");
            return ChartDTO.createEmptyChart("Daily Nutrient Intake", ChartDTO.ChartType.PIE);
        }

        // Calculate daily totals for each day in range
        Map<String, Double> totalNutrients = new HashMap<>();
        long daysInRange = ChronoUnit.DAYS.between(from, to) + 1;
        int nonZeroDays = 0;

        for (MealDTO meal : meals) {
            NutrientInfo nutrients = meal.getNutrients();
            if (nutrients != null) {
                boolean hasNonZero = false;
                if (nutrients.getProtein() > 0) { addToTotal(totalNutrients, "Protein", nutrients.getProtein()); hasNonZero = true; }
                if (nutrients.getCarbs() > 0) { addToTotal(totalNutrients, "Carbohydrates", nutrients.getCarbs()); hasNonZero = true; }
                if (nutrients.getFat() > 0) { addToTotal(totalNutrients, "Fat", nutrients.getFat()); hasNonZero = true; }
                if (nutrients.getFiber() > 0) { addToTotal(totalNutrients, "Fiber", nutrients.getFiber()); hasNonZero = true; }
                if (nutrients.getSugar() > 0) { addToTotal(totalNutrients, "Sugar", nutrients.getSugar()); hasNonZero = true; }
                if (hasNonZero) nonZeroDays++;
            }
        }

        if (nonZeroDays == 0) {
            ChartDTO empty = new ChartDTO("Daily Nutrient Intake", "Nutrient", "Amount (daily average)", ChartDTO.ChartType.PIE);
            empty.setHasData(false);
            empty.setMessage("No data available for the selected date range");
            return empty;
        }

        // Calculate daily averages
        totalNutrients.replaceAll((k, v) -> v / daysInRange);

        // Sort nutrients by value and get top 5
        Map<String, Double> sortedNutrients = totalNutrients.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        // Calculate "Other" category
        double otherTotal = totalNutrients.entrySet().stream()
            .filter(e -> !sortedNutrients.containsKey(e.getKey()))
            .mapToDouble(Map.Entry::getValue)
            .sum();

        // Create chart
        ChartDTO chart = new ChartDTO(
            "Daily Nutrient Intake",
            "Nutrient",
            "Amount (daily average)",
            ChartDTO.ChartType.PIE
        );

        // Add data points (no Calories)
        sortedNutrients.forEach(chart::addDataPoint);
        if (otherTotal > 0) {
            chart.addDataPoint("Other", otherTotal);
        }

        // Cache the chart
        chartCache.cacheChart(profileId, chartKey, chart);
        return chart;
    }

    /**
     * Create a chart comparing daily food group servings with CFG recommendations
     */
    public GroupedBarChartDTO createCfgAlignmentChart(UUID profileId, LocalDate from, LocalDate to) {
        System.out.println("[DEBUG] createCfgAlignmentChart called with profileId=" + profileId + ", from=" + from + ", to=" + to);
        String chartKey = "cfg_alignment_" + from + "_" + to;
        System.out.println("[DEBUG] CFG Alignment chartKey: " + chartKey);
        GroupedBarChartDTO cachedChart = chartCache.getCachedGroupedChart(profileId, chartKey);
        if (cachedChart != null) {
            System.out.println("[DEBUG] Returning cached CFG Alignment chart for key: " + chartKey);
            return cachedChart;
        }
        List<MealDTO> meals = mealLogRepo.getMealsByDateRange(profileId, from, to);
        System.out.println("[DEBUG] Meals found: " + meals.size());
        if (meals.isEmpty()) {
            System.out.println("[DEBUG] No meals found for this range.");
            GroupedBarChartDTO empty = GroupedBarChartDTO.createEmptyChart("Canada's Food Guide Alignment");
            empty.setHasData(false);
            try { empty.getClass().getMethod("setMessage", String.class); empty.setMessage("No data available for the selected date range. Please log meals or try a different range."); } catch (Exception e) { }
            return empty;
        }
        Map<FoodGroup, Double> dailyTotals = new EnumMap<>(FoodGroup.class);
        long daysInRange = ChronoUnit.DAYS.between(from, to) + 1;
        for (MealDTO meal : meals) {
            for (Map.Entry<FoodGroup, Double> entry : meal.getFoodGroupServings().entrySet()) {
                dailyTotals.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        dailyTotals.replaceAll((k, v) -> v / daysInRange);
        GroupedBarChartDTO chart = new GroupedBarChartDTO(
            "Canada's Food Guide Alignment",
            "Food Group",
            "Daily Servings"
        );
        Map<FoodGroup, Double> recommendations = Map.of(
            FoodGroup.VEGETABLES_AND_FRUITS, 8.0,
            FoodGroup.WHOLE_GRAINS, 6.0,
            FoodGroup.PROTEIN_FOODS, 2.0
        );
        for (FoodGroup group : FoodGroup.values()) {
            if (group != FoodGroup.UNKNOWN) {
                double actual = dailyTotals.getOrDefault(group, 0.0);
                double recommended = recommendations.getOrDefault(group, 0.0);
                System.out.println("[DEBUG] FoodGroup: " + group + ", Actual: " + actual + ", Recommended: " + recommended);
                chart.addCategory(group.getDisplayName(), actual, recommended);
            }
        }
        System.out.println("[DEBUG] Caching CFG Alignment chart for key: " + chartKey);
        chartCache.cacheGroupedChart(profileId, chartKey, chart);
        return chart;
    }

    /**
     * Create a chart showing the impact of food swaps on nutrient intake
     */
    public ChartDTO createSwapImpactChart(UUID profileId, NutrientType nutrient, LocalDate from, LocalDate to) {
        System.out.println("[DEBUG] createSwapImpactChart (old) called with profileId=" + profileId + ", nutrient=" + nutrient + ", from=" + from + ", to=" + to);
        if (profileId == null || nutrient == null || from == null || to == null) {
            return ChartDTO.createEmptyChart("Swap Impact", ChartDTO.ChartType.BAR);
        }

        // Check cache first
        String chartKey = "swap_impact_" + nutrient + "_" + from + "_" + to;
        ChartDTO cachedChart = chartCache.getCachedChart(profileId, chartKey);
        if (cachedChart != null) {
            return cachedChart;
        }

        // Get swap history in date range
        List<SwapHistory> swaps = swapHistoryRepo.getSwapsByDateRange(profileId, from, to);
        System.out.println("[DEBUG] Swaps found: " + swaps.size());
        if (swaps.isEmpty()) {
            System.out.println("[DEBUG] No swaps found for this range.");
            return ChartDTO.createEmptyChart("Swap Impact", ChartDTO.ChartType.BAR);
        }

        // Create chart
        ChartDTO chart = new ChartDTO(
            "Impact of Food Swaps on " + nutrient.getDisplayName(),
            "Date",
            nutrient.getDisplayName() + " (" + nutrient.getUnit() + ")",
            ChartDTO.ChartType.BAR
        );

        // Calculate average nutrient change for each swap
        for (SwapHistory swap : swaps) {
            double beforeAmount = swap.getOriginalNutrients().getOrDefault(nutrient, 0.0);
            double afterAmount = swap.getNewNutrients().getOrDefault(nutrient, 0.0);
            double change = afterAmount - beforeAmount;
            
            chart.addDataPoint(swap.getDate().toString(), change);
        }

        // Cache the chart
        chartCache.cacheChart(profileId, chartKey, chart);
        return chart;
    }

    /**
     * Create a chart comparing nutrient intake between two time periods
     */
    public SwapImpactDTO createSwapImpactChart(LocalDate startDate, LocalDate endDate, String nutrient, String chartStyle, UUID profileId) {
        System.out.println("[DEBUG] createSwapImpactChart called with profileId=" + profileId + ", startDate=" + startDate + ", endDate=" + endDate + ", nutrient=" + nutrient + ", chartStyle=" + chartStyle);
        if (startDate == null || endDate == null || nutrient == null || chartStyle == null || profileId == null) {
            return null;
        }

        // Check cache first
        String chartKey = String.format("swap_impact_%s_%s_%s_%s_%s", 
            startDate, endDate, nutrient, chartStyle, profileId);
        SwapImpactDTO cachedChart = chartCache.getCachedSwapImpact(profileId, chartKey);
        if (cachedChart != null) {
            return cachedChart;
        }

        // Get meals for the date range
        List<MealDTO> meals = mealLogRepo.getMealsByDateRange(profileId, startDate, endDate);
        System.out.println("[DEBUG] Meals found: " + meals.size());
        if (meals.isEmpty()) {
            System.out.println("[DEBUG] No meals found for this range.");
        }

        // Initialize data map for time series
        Map<LocalDate, Double> data = new TreeMap<>();

        // Process meals
        for (MealDTO meal : meals) {
            NutrientInfo nutrients = meal.getNutrients();
            if (nutrients != null) {
                LocalDate date = meal.getDate();
                double value = getNutrientValue(nutrients, nutrient);
                data.merge(date, value, Double::sum);
            }
        }
        System.out.println("[DEBUG] Data map: " + data);

        // Create SwapImpactDTO
        SwapImpactDTO chart = new SwapImpactDTO();
        chart.setTitle("Impact of Food Swaps on " + nutrient);
        chart.setXLabel("Date");
        chart.setYLabel(nutrient + " Amount");
        chart.setSelectedNutrient(nutrient);
        chart.setChartStyle(chartStyle);
        
        // Split the data into before and after periods
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate midPoint = startDate.plusDays(totalDays / 2);
        
        Map<LocalDate, Double> beforeData = new TreeMap<>();
        Map<LocalDate, Double> afterData = new TreeMap<>();
        
        data.forEach((date, value) -> {
            if (date.isBefore(midPoint) || date.isEqual(midPoint)) {
                beforeData.put(date, value);
            } else {
                afterData.put(date, value);
            }
        });
        System.out.println("[DEBUG] Before data: " + beforeData);
        System.out.println("[DEBUG] After data: " + afterData);
        
        chart.setBeforeData(beforeData);
        chart.setAfterData(afterData);

        // Cache the chart
        chartCache.cacheSwapImpact(profileId, chartKey, chart);
        return chart;
    }

    private double getNutrientValue(NutrientInfo nutrients, String nutrient) {
        switch (nutrient.toLowerCase()) {
            case "protein":
                return nutrients.getProtein();
            case "carbohydrates":
            case "carbs":
                return nutrients.getCarbs();
            case "fat":
                return nutrients.getFat();
            case "fiber":
                return nutrients.getFiber();
            case "sugar":
                return nutrients.getSugar();
            case "calories":
                return nutrients.getCalories();
            default:
                return 0.0;
        }
    }

    private void addToTotal(Map<String, Double> totals, String nutrient, double value) {
        totals.merge(nutrient, value, Double::sum);
    }

    private void addToTotalAsNumber(Map<String, Number> totals, String nutrient, double value) {
        totals.merge(nutrient, value, (a, b) -> a.doubleValue() + b.doubleValue());
    }
} 