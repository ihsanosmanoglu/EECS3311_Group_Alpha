package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;
import ca.nutrisci.application.facades.IVisualizationFacade;
import ca.nutrisci.application.facades.IMealLogFacade;
import ca.nutrisci.presentation.ui.visualization.ChartFactory;
import ca.nutrisci.presentation.ui.visualization.VisualizationPanel;
import org.jfree.chart.JFreeChart;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controller for visualization operations
 */
public class VisualizationController {
    private final IVisualizationFacade visualizationFacade;
    private final VisualizationPanel visualizationPanel;
    private final IMealLogFacade mealLogFacade;
    private UUID currentProfileId;

    public VisualizationController(IVisualizationFacade visualizationFacade, VisualizationPanel visualizationPanel, IMealLogFacade mealLogFacade) {
        this.visualizationFacade = visualizationFacade;
        this.visualizationPanel = visualizationPanel;
        this.mealLogFacade = mealLogFacade;
    }

    public void setCurrentProfile(UUID profileId) {
        this.currentProfileId = profileId;
    }

    public UUID getCurrentProfileId() {
        return currentProfileId;
    }

    public void loadDailyIntakeChart(LocalDate from, LocalDate to, int topN) {
        System.out.println("[DEBUG] loadDailyIntakeChart: currentProfileId=" + currentProfileId + ", from=" + from + ", to=" + to + ", topN=" + topN);
        if (currentProfileId == null) {
            throw new IllegalStateException("No profile selected");
        }
        ChartDTO chartData = visualizationFacade.buildDailyIntakeChart(currentProfileId, from, to, topN);
        if (!chartData.hasData() && chartData.getMessage() != null) {
            // Show message instead of chart
            visualizationPanel.updateChart(null, chartData);
            return;
        }
        JFreeChart chart = ChartFactory.createChart(chartData);
        visualizationPanel.updateChart(chart, chartData);
    }

    public void loadCfgAlignmentChart(LocalDate from, LocalDate to, String chartStyle) {
        System.out.println("[DEBUG] loadCfgAlignmentChart: currentProfileId=" + currentProfileId + ", from=" + from + ", to=" + to + ", chartStyle=" + chartStyle);
        if (currentProfileId == null) {
            throw new IllegalStateException("No profile selected");
        }
        if ("Plate View".equals(chartStyle)) {
            // Plate View logic
            java.util.List<ca.nutrisci.application.dto.MealDTO> meals = mealLogFacade.getMealsForDateRange(currentProfileId, from, to);
            java.util.Map<String, Double> userPlate = new java.util.HashMap<>();
            long daysInRange = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
            for (ca.nutrisci.application.dto.MealDTO meal : meals) {
                if (meal.getFoodGroupServings() != null) {
                    for (var entry : meal.getFoodGroupServings().entrySet()) {
                        String group = entry.getKey().toString().replace("_", " ").replace("AND", "and").toLowerCase();
                        if (group.contains("vegetable") || group.contains("fruit")) group = "Vegetables and Fruits";
                        else if (group.contains("grain")) group = "Whole Grains";
                        else if (group.contains("protein")) group = "Protein Foods";
                        else group = "Other";
                        userPlate.merge(group, entry.getValue(), Double::sum);
                    }
                }
            }
            userPlate.replaceAll((k, v) -> v / (double) daysInRange);
            // Only keep the three main groups
            java.util.Map<String, Double> userPlateFinal = new java.util.LinkedHashMap<>();
            userPlateFinal.put("Vegetables and Fruits", userPlate.getOrDefault("Vegetables and Fruits", 0.0));
            userPlateFinal.put("Whole Grains", userPlate.getOrDefault("Whole Grains", 0.0));
            userPlateFinal.put("Protein Foods", userPlate.getOrDefault("Protein Foods", 0.0));
            // If all are zero, show warning
            boolean hasData = userPlateFinal.values().stream().anyMatch(v -> v > 0.01);
            if (!hasData) {
                javax.swing.JLabel warn = new javax.swing.JLabel("No data available for selected date range. Please log meals or choose a different period.", javax.swing.SwingConstants.CENTER);
                warn.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
                javax.swing.JPanel chartContainer = this.visualizationPanel.getChartContainer();
                chartContainer.removeAll();
                chartContainer.setLayout(new java.awt.BorderLayout());
                chartContainer.add(warn, java.awt.BorderLayout.CENTER);
                chartContainer.revalidate();
                chartContainer.repaint();
                return;
            }
            // CFG plate proportions (hardcoded)
            java.util.Map<String, Double> cfgPlate = new java.util.LinkedHashMap<>();
            cfgPlate.put("Vegetables and Fruits", 50.0);
            cfgPlate.put("Whole Grains", 25.0);
            cfgPlate.put("Protein Foods", 25.0);
            javax.swing.JPanel platePanel = ca.nutrisci.presentation.ui.visualization.ChartFactory.createPlateViewCharts(userPlateFinal, cfgPlate);
            javax.swing.JPanel chartContainer = this.visualizationPanel.getChartContainer();
            chartContainer.removeAll();
            chartContainer.setLayout(new java.awt.BorderLayout());
            chartContainer.add(platePanel, java.awt.BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
            return;
        }
        GroupedBarChartDTO chartData = visualizationFacade.buildCfgAlignmentChart(currentProfileId, from, to);
        if (!chartData.hasData() && chartData.getMessage() != null) {
            // Show message instead of chart
            visualizationPanel.updateChart(null, chartData);
            return;
        }
        JFreeChart chart = ChartFactory.createGroupedBarChart(chartData);
        visualizationPanel.updateChart(chart, chartData);
    }

    /**
     * Loads and displays a swap impact chart for the specified date range, nutrient, and chart style
     * @param startDate start date of the range (inclusive)
     * @param endDate end date of the range (inclusive)
     * @param nutrient the nutrient to analyze
     * @param chartStyle the chart style to use (bar or line)
     * @param profileId ID of the profile to show data for
     */
    public void loadSwapImpactChart(LocalDate startDate, LocalDate endDate, String nutrient, String chartStyle, UUID profileId) {
        System.out.println("[DEBUG] loadSwapImpactChart: profileId=" + profileId + ", startDate=" + startDate + ", endDate=" + endDate + ", nutrient=" + nutrient + ", chartStyle=" + chartStyle);
        // Validate inputs
        if (startDate == null || endDate == null || nutrient == null || chartStyle == null || profileId == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        SwapImpactDTO chartData = visualizationFacade.buildSwapImpactChart(startDate, endDate, nutrient, chartStyle, profileId);
        if (chartData == null) {
            throw new IllegalStateException("Failed to generate swap impact chart data");
        }
        JFreeChart chart = ChartFactory.createSwapImpactChart(chartData);
        // For swap impact, just pass null for ChartDTO (no message support needed)
        visualizationPanel.updateChart(chart, null);
    }
} 