package ca.nutrisci.application.services;

import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.MealDTO;
import ca.nutrisci.application.dto.NutrientInfo;
import ca.nutrisci.application.services.observers.ChartCacheUpdater;
import ca.nutrisci.application.services.observers.DailyTotalsCalculator;
import ca.nutrisci.domain.entities.FoodGroup;
import ca.nutrisci.infrastructure.data.repositories.MealLogRepo;
import ca.nutrisci.infrastructure.data.repositories.SwapHistoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChartsServiceTest {
    @Mock
    private ChartCacheUpdater chartCache;
    @Mock
    private DailyTotalsCalculator dailyTotalsCalculator;
    @Mock
    private MealLogRepo mealLogRepo;
    @Mock
    private SwapHistoryRepo swapHistoryRepo;

    private ChartsService chartsService;
    private UUID testProfileId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chartsService = new ChartsService(chartCache, dailyTotalsCalculator, mealLogRepo, swapHistoryRepo);
        testProfileId = UUID.randomUUID();
        startDate = LocalDate.now().minusDays(6);
        endDate = LocalDate.now();
    }

    @Test
    void createCfgAlignmentChart_WithValidData_ReturnsCorrectChart() {
        // Arrange
        List<MealDTO> testMeals = createTestMeals();
        when(mealLogRepo.getMealsByDateRange(testProfileId, startDate, endDate))
                .thenReturn(testMeals);
        when(chartCache.getCachedGroupedChart(any(), any())).thenReturn(null);

        // Act
        GroupedBarChartDTO chart = chartsService.createCfgAlignmentChart(testProfileId, startDate, endDate);

        // Assert
        assertNotNull(chart);
        assertTrue(chart.hasData());
        assertEquals("Canada's Food Guide Alignment", chart.getTitle());
        assertEquals("Food Group", chart.getXAxisLabel());
        assertEquals("Daily Servings", chart.getYAxisLabel());

        // Verify food group servings
        Map<String, Double> actuals = chart.getActuals();
        assertEquals(3.0, actuals.get(FoodGroup.VEGETABLES_AND_FRUITS.getDisplayName()), 0.1);
        assertEquals(2.0, actuals.get(FoodGroup.WHOLE_GRAINS.getDisplayName()), 0.1);
        assertEquals(1.0, actuals.get(FoodGroup.PROTEIN_FOODS.getDisplayName()), 0.1);

        // Verify recommendations
        Map<String, Double> recommendations = chart.getRecommendations();
        assertEquals(8.0, recommendations.get(FoodGroup.VEGETABLES_AND_FRUITS.getDisplayName()));
        assertEquals(6.0, recommendations.get(FoodGroup.WHOLE_GRAINS.getDisplayName()));
        assertEquals(2.0, recommendations.get(FoodGroup.PROTEIN_FOODS.getDisplayName()));

        // Verify caching
        verify(chartCache).cacheGroupedChart(eq(testProfileId), anyString(), any(GroupedBarChartDTO.class));
    }

    @Test
    void createCfgAlignmentChart_WithNoData_ReturnsEmptyChart() {
        // Arrange
        when(mealLogRepo.getMealsByDateRange(testProfileId, startDate, endDate))
                .thenReturn(Collections.emptyList());

        // Act
        GroupedBarChartDTO chart = chartsService.createCfgAlignmentChart(testProfileId, startDate, endDate);

        // Assert
        assertNotNull(chart);
        assertFalse(chart.hasData());
        assertEquals("Canada's Food Guide Alignment", chart.getTitle());
    }

    @Test
    void createCfgAlignmentChart_WithNullParameters_ReturnsEmptyChart() {
        // Act & Assert
        GroupedBarChartDTO chart1 = chartsService.createCfgAlignmentChart(null, startDate, endDate);
        assertNotNull(chart1);
        assertFalse(chart1.hasData());

        GroupedBarChartDTO chart2 = chartsService.createCfgAlignmentChart(testProfileId, null, endDate);
        assertNotNull(chart2);
        assertFalse(chart2.hasData());

        GroupedBarChartDTO chart3 = chartsService.createCfgAlignmentChart(testProfileId, startDate, null);
        assertNotNull(chart3);
        assertFalse(chart3.hasData());
    }

    @Test
    void createCfgAlignmentChart_UsesCachedData_WhenAvailable() {
        // Arrange
        GroupedBarChartDTO cachedChart = new GroupedBarChartDTO("Cached Chart", "X", "Y");
        when(chartCache.getCachedGroupedChart(any(), any())).thenReturn(cachedChart);

        // Act
        GroupedBarChartDTO chart = chartsService.createCfgAlignmentChart(testProfileId, startDate, endDate);

        // Assert
        assertSame(cachedChart, chart);
        verify(mealLogRepo, never()).getMealsByDateRange(any(), any(), any());
    }

    private List<MealDTO> createTestMeals() {
        List<MealDTO> meals = new ArrayList<>();
        
        // Create a week's worth of meals
        for (int i = 0; i < 7; i++) {
            MealDTO meal = new MealDTO();
            meal.setId(UUID.randomUUID());
            meal.setProfileId(testProfileId);
            meal.setDate(startDate.plusDays(i));
            
            // Add food group servings (daily averages will be: 3 veg, 2 grains, 1 protein)
            Map<FoodGroup, Double> servings = new EnumMap<>(FoodGroup.class);
            servings.put(FoodGroup.VEGETABLES_AND_FRUITS, 3.0);
            servings.put(FoodGroup.WHOLE_GRAINS, 2.0);
            servings.put(FoodGroup.PROTEIN_FOODS, 1.0);
            meal.setFoodGroupServings(servings);

            meals.add(meal);
        }

        return meals;
    }
} 