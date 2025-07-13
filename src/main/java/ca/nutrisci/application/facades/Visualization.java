package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;
import ca.nutrisci.application.services.ChartsService;
import ca.nutrisci.domain.entities.NutrientType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementation of IVisualizationFacade
 */
public class Visualization implements IVisualizationFacade {
    private final ChartsService chartsService;

    public Visualization(ChartsService chartsService) {
        this.chartsService = chartsService;
    }

    @Override
    public ChartDTO buildDailyIntakeChart(UUID profileId, LocalDate from, LocalDate to) {
        return chartsService.createTopNutrientsChart(profileId, from, to);
    }

    @Override
    public GroupedBarChartDTO buildCfgAlignmentChart(UUID profileId, LocalDate from, LocalDate to) {
        return chartsService.createCfgAlignmentChart(profileId, from, to);
    }

    @Override
    public ChartDTO buildSwapImpactChart(UUID profileId, NutrientType nutrient, LocalDate from, LocalDate to) {
        return chartsService.createSwapImpactChart(profileId, nutrient, from, to);
    }

    @Override
    public SwapImpactDTO buildSwapImpactChart(LocalDate startDate, LocalDate endDate, String nutrient, String chartStyle, UUID profileId) {
        if (startDate == null || endDate == null || nutrient == null || chartStyle == null || profileId == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        return chartsService.createSwapImpactChart(startDate, endDate, nutrient, chartStyle, profileId);
    }
} 