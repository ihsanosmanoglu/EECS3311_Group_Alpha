package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;
import ca.nutrisci.domain.entities.NutrientType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Interface for visualization operations
 */
public interface IVisualizationFacade {
    ChartDTO buildDailyIntakeChart(UUID profileId, LocalDate from, LocalDate to);
    GroupedBarChartDTO buildCfgAlignmentChart(UUID profileId, LocalDate from, LocalDate to);
    ChartDTO buildSwapImpactChart(UUID profileId, NutrientType nutrient, LocalDate from, LocalDate to);
    SwapImpactDTO buildSwapImpactChart(LocalDate startDate, LocalDate endDate, String nutrient, String chartStyle, UUID profileId);
} 