package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;
import ca.nutrisci.application.facades.IVisualizationFacade;
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
    private UUID currentProfileId;

    public VisualizationController(IVisualizationFacade visualizationFacade, VisualizationPanel visualizationPanel) {
        this.visualizationFacade = visualizationFacade;
        this.visualizationPanel = visualizationPanel;
    }

    public void setCurrentProfile(UUID profileId) {
        this.currentProfileId = profileId;
    }

    public UUID getCurrentProfileId() {
        return currentProfileId;
    }

    public void loadDailyIntakeChart(LocalDate from, LocalDate to) {
        System.out.println("[DEBUG] loadDailyIntakeChart: currentProfileId=" + currentProfileId + ", from=" + from + ", to=" + to);
        if (currentProfileId == null) {
            throw new IllegalStateException("No profile selected");
        }

        ChartDTO chartData = visualizationFacade.buildDailyIntakeChart(currentProfileId, from, to);
        JFreeChart chart = ChartFactory.createChart(chartData);
        visualizationPanel.updateChart(chart);
    }

    public void loadCfgAlignmentChart(LocalDate from, LocalDate to) {
        System.out.println("[DEBUG] loadCfgAlignmentChart: currentProfileId=" + currentProfileId + ", from=" + from + ", to=" + to);
        if (currentProfileId == null) {
            throw new IllegalStateException("No profile selected");
        }

        GroupedBarChartDTO chartData = visualizationFacade.buildCfgAlignmentChart(currentProfileId, from, to);
        JFreeChart chart = ChartFactory.createGroupedBarChart(chartData);
        visualizationPanel.updateChart(chart);
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

        // Get the chart data from the facade
        SwapImpactDTO chartData = visualizationFacade.buildSwapImpactChart(startDate, endDate, nutrient, chartStyle, profileId);
        if (chartData == null) {
            throw new IllegalStateException("Failed to generate swap impact chart data");
        }

        // Create and display the chart
        JFreeChart chart = ChartFactory.createSwapImpactChart(chartData);
        visualizationPanel.updateChart(chart);
    }
} 