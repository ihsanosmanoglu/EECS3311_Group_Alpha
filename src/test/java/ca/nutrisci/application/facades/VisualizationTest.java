package ca.nutrisci.application.facades;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.infrastructure.data.repositories.IRepositoryFactory;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VisualizationTest {
    @Mock
    private IRepositoryFactory repoFactory;
    
    @Mock
    private MealLogRepo mealLogRepo;
    
    private Visualization visualization;
    private UUID testProfileId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(repoFactory.getMealLogRepository()).thenReturn(mealLogRepo);
        visualization = new Visualization(repoFactory);
        testProfileId = UUID.randomUUID();
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 7);
    }

    @Test
    void buildDailyIntakeChart_WithValidData_ReturnsChartWithTop5Nutrients() {
        // Arrange
        MealDTO meal1 = createMealWithNutrients(100, 20, 30, 10, 5);
        MealDTO meal2 = createMealWithNutrients(200, 30, 40, 15, 8);
        when(mealLogRepo.getMealsByDateRange(testProfileId, startDate, endDate))
            .thenReturn(Arrays.asList(meal1, meal2));

        // Act
        ChartDTO chart = visualization.buildDailyIntakeChart(startDate, endDate, testProfileId);

        // Assert
        assertNotNull(chart);
        assertEquals(ChartDTO.ChartType.PIE, chart.getChartType());
        assertTrue(chart.hasData());
        
        Map<String, Double> dataPoints = chart.getDataPoints();
        assertEquals(5, dataPoints.size());
        
        // Verify the order of nutrients (should be sorted by value)
        double[] expectedDailyAverages = {
            300.0/7,  // Calories
            70.0/7,   // Carbs
            50.0/7,   // Protein
            25.0/7,   // Fat
            13.0/7    // Fiber
        };
        
        double[] actualValues = dataPoints.values().stream()
            .mapToDouble(Double::doubleValue)
            .toArray();
        
        assertArrayEquals(expectedDailyAverages, actualValues, 0.01);
    }

    @Test
    void buildDailyIntakeChart_WithNoMeals_ReturnsEmptyChart() {
        // Arrange
        when(mealLogRepo.getMealsByDateRange(testProfileId, startDate, endDate))
            .thenReturn(Arrays.asList());

        // Act
        ChartDTO chart = visualization.buildDailyIntakeChart(startDate, endDate, testProfileId);

        // Assert
        assertNotNull(chart);
        assertEquals(ChartDTO.ChartType.PIE, chart.getChartType());
        assertFalse(chart.hasData());
    }

    @Test
    void buildDailyIntakeChart_WithNullParameters_ReturnsEmptyChart() {
        // Act & Assert
        ChartDTO chart1 = visualization.buildDailyIntakeChart(null, endDate, testProfileId);
        assertFalse(chart1.hasData());

        ChartDTO chart2 = visualization.buildDailyIntakeChart(startDate, null, testProfileId);
        assertFalse(chart2.hasData());

        ChartDTO chart3 = visualization.buildDailyIntakeChart(startDate, endDate, null);
        assertFalse(chart3.hasData());
    }

    private MealDTO createMealWithNutrients(double calories, double protein, double carbs, double fat, double fiber) {
        NutrientInfo nutrients = new NutrientInfo();
        nutrients.setCalories(calories);
        nutrients.setProtein(protein);
        nutrients.setCarbs(carbs);
        nutrients.setFat(fat);
        nutrients.setFiber(fiber);

        MealDTO meal = new MealDTO();
        meal.setNutrients(nutrients);
        return meal;
    }
} 