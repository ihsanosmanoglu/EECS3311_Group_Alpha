package ca.nutrisci.presentation.controllers;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.facades.IVisualizationFacade;
import ca.nutrisci.presentation.ui.visualization.ChartFactory;
import ca.nutrisci.presentation.ui.visualization.VisualizationPanel;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controller for nutrient visualization views
 */
public class NutrientViewController {
    private final IVisualizationFacade visualizationFacade;
    private final VisualizationPanel visualizationPanel;

    public NutrientViewController(IVisualizationFacade visualizationFacade, VisualizationPanel visualizationPanel) {
        this.visualizationFacade = visualizationFacade;
        this.visualizationPanel = visualizationPanel;
    }

    public void showDailyIntakeChart(UUID profileId, LocalDate from, LocalDate to) {
        ChartDTO chartData = visualizationFacade.buildDailyIntakeChart(profileId, from, to, 5); // Default to top 5 nutrients
        var chart = ChartFactory.createChart(chartData);
        visualizationPanel.updateChart(chart, chartData);
    }

    public void showCfgAlignmentChart(UUID profileId, LocalDate from, LocalDate to) {
        var report = visualizationFacade.buildCfgAlignmentChart(profileId, from, to);
        
        // Create a bar chart comparing actual vs target proportions
        ChartDTO chartData = new ChartDTO(
            "Food Group Alignment",
            "Food Group",
            "Proportion",
            ChartDTO.ChartType.BAR
        );

        // Add actual and target values for each food group
        for (String group : report.getCategories()) {
            double actual = report.getActuals().getOrDefault(group, 0.0);
            double target = report.getRecommendations().getOrDefault(group, 0.0);
            chartData.addDataPoint(group + " (Actual)", actual * 100);
            chartData.addDataPoint(group + " (Target)", target * 100);
        }

        var chart = ChartFactory.createChart(chartData);
        visualizationPanel.updateChart(chart, null);
    }
} 